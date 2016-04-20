//
// Created by Damian on 18.04.2016.
//

#include "mainactivity.h"

#include <vector>
#include <iostream>
#include <sstream>

#include <android/log.h>

#include "T_img_utils.h"
#include "T_cls_utils.h"
#include "MemoryFile.h"
#include "PGMFile.h"

std::string PaPaMobile_HandRecognization(int* table, std::string fileData, size_t fileLength, int warunek);

extern "C" {
JNIEXPORT jintArray JNICALL Java_com_app_checkpresence_CameraView_myNativeCode(JNIEnv *env, jobject instance, jintArray argb_,
                                                                               jintArray returnedInputSegmentationFileData, jint rows, jint cols, jint warunek){

    jint *argb = (*env).GetIntArrayElements(argb_, NULL);
    unsigned char* plikDaneARGB = (unsigned char*)argb;
    int dataLength = rows * cols * 3;

    jint *argbColorOut = (*env).GetIntArrayElements(returnedInputSegmentationFileData, NULL);

    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TEST", "Testowy log z NativeCode c++. Hello console :)");

   // (*env).SetArra

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
    fileData << "P6\n" << cols << " " << rows << "\n# eyetom.com\n" << 255 << "\n";
    int headerLength = fileData.str().size();

    /*
    * koopiowanie obrazka przekazanego z java - kolorowy
    */
    //jintArray resultColor;
    //resultColor = (*env).NewIntArray(rows*cols);
    //if (resultColor == NULL) {
    //   return NULL; /* out of memory error thrown */
    //}

    /* kopiuje orginalne pixele z obraz przekazanego do segmentacji */
    //jint tableColor[rows*cols];
    /*for (size_t i = 0; i < rows*cols; i++) {
        tableColor[i] = ((int*)plikDaneARGB)[i]; // put whatever logic you want to populate the values here.
    }*/

    for (size_t i = 0; i < rows * cols * 4; i += 4) {
        char a = plikDaneARGB[i + 3];
        char r = plikDaneARGB[i + 2];
        char g = plikDaneARGB[i + 1];
        char b = plikDaneARGB[i + 0];

        int intA = a;
        int intR = r;
        int intG = g;
        int intB = b;
        int color = (intA << 24) | (intR << 16) | (intG << 8) | (intB << 0);
        //int color = 0xFFFF22FF;

        argbColorOut[i / 4] = color;

        fileData.write(reinterpret_cast<char *>(&r), 1);
        fileData.write(reinterpret_cast<char *>(&g), 1);
        fileData.write(reinterpret_cast<char *>(&b), 1);

        //if (i == 0) {
        //    testPixel << "ARGB: " << (int)r << " " << (int)g << " " << (int)b << " " << (int)a << '\n';
        //}
/*
        if (i > dlugosc && i <= dlugosc + 4) {
            testPixel << "ARGB: " << (int)a << " " << (int)r << " " << (int)g << " " << (int)b << '\n';
        }
*/
    }
    // move from the temp structure to the java structure
    //(*env).SetIntArrayRegion(resultColor, 0, rows*cols, tableColor);

    //return (*env).NewStringUTF(std::string("hello return 01").c_str());



    jintArray result;
    result = (*env).NewIntArray(rows*cols);
    if (result == NULL) {
        return NULL; /* out of memory error thrown */
    }

    jint table[rows*cols];
    if (table == nullptr) {
        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TEST", "Testowy log z NativeCode c++. Hello console :)");
        int x;
        x = 6;
    }
    for (size_t i = 0; i < rows*cols; i++) {
        table[i] = 0; // put whatever logic you want to populate the values here.
    }


    dataLength += headerLength;
    std::string wynik = PaPaMobile_HandRecognization((int*)&table[0], fileData.str(), dataLength, warunek);
    int xx = wynik.size();
    int y = 0;

    // zatapienie kolorow po separacje przez orginalne kolorowe
    /*for (int i = 0; i < rows * cols; ++i) {
        table[i] = argbColorOut[i];
    }*/

    // move from the temp structure to the java structure
    (*env).SetIntArrayRegion(result, 0, rows*cols, table);

    (*env).ReleaseIntArrayElements(argb_, argb,0);
    (*env).ReleaseIntArrayElements(returnedInputSegmentationFileData, argbColorOut, 0);

    return result;
    //return (*env).NewStringUTF(wynik.c_str());
    //return (*env).NewStringUTF(testPixel.str().c_str());
    }
}

std::string PaPaMobile_HandRecognization(int* table, std::string fileData, size_t fileLength, int warunek) {
    int rows, cols;
    int max_color;
    int hpos, i, j;
    PGMFile pgmFile(fileData.c_str(), fileLength);
    //return "test happy 01";

    //if ((hpos = readPPMB_header(f.c_str(), &rows, &cols, &max_color)) <= 0) exit(1);
    if ((hpos = pgmFile.readPGMB_header(&rows, &cols, &max_color)) <= 0)
        return "nie udało się wczytać nagłówka";
    //return pgmFile.readPGMB_header(&rows, &cols, &max_color);

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
            //warunek = (r>50 && r>g && r>b) || (r>90 && r>g && r>g - 10);
            //warunek = (r>65 && r>g && r>b-10) || (i<200 && r>25 && r>g && r>b-10);
            /*if (index == 93484) { warunek = r>120 && r>g && r>b; }
            if (index == 112302) {warunek = (r>50 && r>g && r>b) || (r>90 && r>g && r>g-10) ; } // W miare OK
            if (index == 112305) {warunek = (r>100 && r>g && r>b) || (r>200 )  ; }
            if (index == 112310) {warunek = (r>65 && r>g && r>b-10) || (i<200 && r>25 && r>g && r>b-10);}
            if (index == 112311) {warunek = (r>100 && r>g && r>b-10) ;}
            if (index == 112319) {warunek = ( r>120 && r>g && r>b) ;}
            if (index == 112320) {warunek = ( r>80 && r>g && r>b) || (r>100 && r>g && r>b-20) ;}*/
            if (warunek == 0) { warunek = r>120 && r>g && r>b; }
            if (warunek == 1) { warunek = (r>50 && r>g && r>b) || (r>90 && r>g && r>g - 10); }
            if (warunek == 2) { warunek = (r>100 && r>g && r>b) || (r>200); }
            if (warunek == 3) { warunek = (r>65 && r>g && r>b - 10) || (i<200 && r>25 && r>g && r>b - 10); }
            if (warunek == 4) { warunek = (r>100 && r>g && r>b - 10); }
            if (warunek == 5) { warunek = (r>120 && r>g && r>b); }
            if (warunek == 6) { warunek = (r>80 && r>g && r>b) || (r>100 && r>g && r>b - 20); }
            warunek = !(r > 170 && g > 170 && b > 170);

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
    //pgmFile.writePGMB_image_to_string(daneAfterSegmentation, b_out[0], rows, cols, 255);
    pgmFile.writePGMB_image_to_tableInt(table, b_out[0], rows, cols, 255);

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

    //return "najprawodopodniej wszystko OK! HAPPY AND READY FOR NEXT ETAP!!!! PROBABLY....";
    return daneAfterSegmentation;
}