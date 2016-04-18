//
// Created by Damian on 18.04.2016.
//

#include "mainactivity.h"

#include <vector>
#include <iostream>
#include <sstream>

#include "T_img_utils.h"
#include "T_cls_utils.h"
#include "MemoryFile.h"
#include "PGMFile.h"

std::string PaPaMobile_HandRecognization(std::string fileData, size_t fileLength);

extern "C" {
JNIEXPORT jstring JNICALL Java_com_app_checkpresence_CameraView_myNativeCode(JNIEnv *env, jobject instance, jintArray argb_,
                                                                             jint dlugosc, jint rows, jint cols){

    jint *argb = (*env).GetIntArrayElements(argb_, NULL);
    char* plikDaneARGB = (char*)argb;
    /*
    std::stringstream strS;
    const char* str = env -> GetStringUTFChars(fileData, 0);
    strS << "byleco";
    std::string hand;
    hand.reserve(fileLength);
    for(int i=0; i<fileLength; i++){
        hand[i] = str[i];
    }
     */
    //PaPaMobile_HandRecognization(hand, fileLength);

    //env -> ReleaseStringUTFChars(fileData, str);

    std::stringstream fileData;
    std::stringstream testPixel;
    //fileData << "P6\n" << cols << " " << rows << "\n# eyetom.com\n" << 255 << "\n";

    for (size_t i = 0; i < dlugosc; i++) {
        char a = plikDaneARGB[i + 0];
        char r = plikDaneARGB[i + 1];
        char g = plikDaneARGB[i + 2];
        char b = plikDaneARGB[i + 3];

        if (i == 0) {
            testPixel << "ARGB: " << (int)a << " " << (int)r << " " << (int)g << " " << (int)b << '\n';
        }
/*
        if (i > dlugosc && i <= dlugosc + 4) {
            testPixel << "ARGB: " << (int)a << " " << (int)r << " " << (int)g << " " << (int)b << '\n';
        }
*/
        fileData.write(reinterpret_cast<char *>(plikDaneARGB + i), 3);
    }

    (*env).ReleaseIntArrayElements(argb_, argb,0);
    return (*env).NewStringUTF(PaPaMobile_HandRecognization(fileData.str(), dlugosc * sizeof(int)).c_str());
    //return (*env).NewStringUTF(testPixel.str().c_str());
    }
}

std::string PaPaMobile_HandRecognization(std::string fileData, size_t fileLength) {
    int rows, cols;
    int max_color;
    int hpos, i, j;
    PGMFile pgmFile(fileData.c_str(), fileLength);
    //return "test happy 01";
    //MemoryFile memoryFile;

    /*
    FILE *fp;
    size_t flen, hlen;
    char signature[3];


    if ((fp = fopen(f.c_str(), "rb")) == NULL)
        return 0;

    fseek(fp, 0, SEEK_END);
    flen = ftell(fp);	//file lenght
    fileData = new char[flen];
    fseek(fp, 0, SEEK_SET);
    fread(fileData, 1, flen, fp);
    memoryFile.setFile(fileData, flen);*/

    /**/

    //if ((hpos = readPPMB_header(f.c_str(), &rows, &cols, &max_color)) <= 0) exit(1);
    //if ((hpos = pgmFile.readPGMB_header(&rows, &cols, &max_color)) <= 0)
     //   return "nie udało się wczytać nagłówka";
    //return pgmFile.readPGMB_header(&rows, &cols, &max_color);
    hpos = 0;

    unsigned char **R = new_char_image(rows, cols);
    unsigned char **G = new_char_image(rows, cols);
    unsigned char **B = new_char_image(rows, cols);

    if (pgmFile.readPPMB_data(R[0], G[0], B[0], hpos, rows, cols, max_color) == 0)	   exit(1);

    //przygotowanie czarno-bialej tablicy wyjsciowej
    unsigned char **b_out = new_char_image(rows, cols);

    //wczytaj nr. indexu z nazy pliku
    /* to niewazne, mamy tylko 1 plik do przetworzenia!*/
    /*std::string str_index = f.substr(7, f.length() - 10);
    int index = atoi(str_index.c_str());*/

    for (i = 0; i< rows; ++i) {
        for (j = 0; j< cols; ++j) {

            int r = R[i][j];
            int g = G[i][j];
            int b = B[i][j];
            int warunek = 0;

            if (i == 141 && j == 145) {
                int stop = 1;
            }

            //warunek = (r > 65 && r>g && r > b - 10) || (i < 200 && r>25 && r > g && r > b - 10);
            //warunek = (r > 80 && r>g && r > b - 10);
            // dla ciemnego tła ten pasuje
            warunek = (r>50 && r>g && r>b) || (r>90 && r>g && r>g - 10);
            /*if (index == 93484) { warunek = r>120 && r>g && r>b; }
            if (index == 112302) {warunek = (r>50 && r>g && r>b) || (r>90 && r>g && r>g-10) ; } // W miare OK
            if (index == 112305) {warunek = (r>100 && r>g && r>b) || (r>200 )  ; }
            if (index == 112310) {warunek = (r>65 && r>g && r>b-10) || (i<200 && r>25 && r>g && r>b-10);}
            if (index == 112311) {warunek = (r>100 && r>g && r>b-10) ;}
            if (index == 112319) {warunek = ( r>120 && r>g && r>b) ;}
            if (index == 112320) {warunek = ( r>80 && r>g && r>b) || (r>100 && r>g && r>b-20) ;}*/

            b_out[i][j] = warunek ? 255 : 0;
        }
    }


    int w = cols, h = rows;
    unsigned char tlo = 0;
    unsigned char* b1 = b_out[0];

    //"czyszczenie" obrazu, znajdz wszystkie  biale klastry i wyczyść jej jesli sa male

    int minimum = 220;

    unsigned int cls_count;
    unsigned int a_xt, a_yt, a_xb, a_yb;
    int dx, dy;

    CLSV clusters;
    T_FindAllClusters(b_out, cols, rows, 255, clusters);


    //algorytm czyszczenia wymaga aby conajmniej jeden piksel brzegowy był koloru tła - zapobiega to sprawdzaniu za każdym razem czy nie wyszlismy poza granice obrazka
    for (i = 0; i<w; ++i)	b1[i] = b1[(h - 1)*w + i] = tlo;		//pierwszy i ostatni wiersz
    for (i = 0; i<h; ++i)	b1[i*w] = b1[i*w + (w - 1)] = tlo;		//pierwsza i ostatnia kolumna

    cls_count = clusters.size();
    for (int n = 0; n<cls_count; n++)
    {
        CLS a = clusters[n];

        a_xt = a.minx_index % w;
        a_yt = a.miny_index / w;
        a_xb = a.maxx_index % w;
        a_yb = a.maxy_index / w;

        dx = abs((int)(a_xb - a_xt));
        dy = abs((int)(a_yb - a_yt));

        if (dx<minimum || dy<minimum) {
            T_ClearOneClaster(b_out[0], w, h, 255, 0, a.miny_index);			//wyczysc biale jak sa mniejsze od zadanej wielkości
        };
    }

    //"czyszczenie 2" obrazu, znajdz wszystkie  czarne klastry i zapelnij je jesli sa male
    CLSV clusters2;
    T_FindAllClusters(b_out, cols, rows, 0, clusters2);

    //algorytm czyszczenia wymaga aby conajmniej jeden piksel brzegowy był koloru tła - teraz tlo jest 255
    for (i = 0; i<w; ++i)	b1[i] = b1[(h - 1)*w + i] = 255;		//pierwszy i ostatni wiersz
    for (i = 0; i<h; ++i)	b1[i*w] = b1[i*w + (w - 1)] = 255;		//pierwsza i ostatnia kolumna

    minimum = 50;

    cls_count = clusters2.size();
    for (int n = 0; n<cls_count; n++)
    {
        CLS a = clusters2[n];

        a_xt = a.minx_index % w;
        a_yt = a.miny_index / w;
        a_xb = a.maxx_index % w;
        a_yb = a.maxy_index / w;

        dx = abs((int)(a_xb - a_xt));
        dy = abs((int)(a_yb - a_yt));

        if (dx<minimum || dy<minimum) {
            T_ClearOneClaster(b_out[0], w, h, 0, 255, a.miny_index);			//wyczysc biale jak sa mniejsze od zadanej wielkości
        };
    }

    //powroc do czarnych ramek
    for (i = 0; i<w; ++i)	b1[i] = b1[(h - 1)*w + i] = tlo;		//pierwszy i ostatni wiersz
    for (i = 0; i<h; ++i)	b1[i*w] = b1[i*w + (w - 1)] = tlo;		//pierwsza i ostatnia kolumna

    //wygladzanie lini
    //tworzymy kopie robocza obrazka wejsciowego poniewaz bedziemy zmieniac wartosci pikseli
    unsigned char **b2 = new_char_image(h, w);
    unsigned char *b1_data = b_out[0];
    unsigned char *b2_data = b2[0];
    for (i = 0; i < w*h; i++)	b2_data[i] = b1_data[i];


    for (i = 1; i< rows - 1; ++i) {
        for (j = 1; j< cols - 1; ++j) {

            if (b2[i][j] == 0) {
                int n = test_how_much_neighburs_pixel(b2, i, j, 255);
                //if (n > 3) b_out[i][j]=255;
            }

        }
    }


#if defined (_MSC_VER)
    /* UWAGA:
	   Ten fragment działa tylko w Visual Studio
	   trzeba ustawić scieszke jezeli ma nie zapisywac pliku tam gdzie scieszka projektu*/
	std::string outfname = "E:\\img_\\output__separated.pgm";
	if (pgmFile.writePGMB_image_to_file(outfname, b_out[0], rows, cols, 255) != E_OK)
		return ERROR;
	//if (writePGMB_image(outfname.c_str(), b_out[0], rows, cols, 255) == 0)	   exit(1);
#endif

    std::string daneAfterSegmentation;
    //std::cout << daneAfterSegmentation << std::endl;
    pgmFile.writePGMB_image_to_string(daneAfterSegmentation, b_out[0], rows, cols, 255);

    // to trzeba jeszcze poprawić wede alokacji pamięci
    delete[] R;
    delete[] G;
    delete[] B;
    delete[] b_out;
    delete[] b2;

    /* UWAGA:
       Wynik do nastepnego etapu znajduje sie w zmiennej std::string daneAfterSegmentation
    */

    /***********************************
    Tu powinno być wywołanie funkcji odpowiedzialen za następny etap przetwarzania!
    int etap2(daneAfterSegmentation, daneAfterSegmentation.size());
    ************************************/

    return "najprawodopodniej wszystko OK! HAPPY AND READY FOR NEXT ETAP!!!! PROBABLY....";
}