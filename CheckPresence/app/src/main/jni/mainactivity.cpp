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

#include "image_utility.h"

/**
 * @param image_out: wskaznik do tablicy int, tu zapisywany jest wyik funkcji
 * @param fileData: "plik" do przetwozenia
 * @param image_out: liczba bajtow "pliku"
 */
void PaPaMobile_HandRecognization(int* image_out, std::string fileData, int rows, int cols, int warunek, int avgR, int avgG, int avgB);

extern "C" {
JNIEXPORT jfloatArray JNICALL Java_com_app_checkpresence_OpenCVSubtraction_findHandFeatures(JNIEnv *env, jobject instance, jintArray argb_,
jint rows, jint cols)
{
    int h, w;
    int max_color;
    int i, j, ret;
    int hpos = 0;
    std::vector<float> featureVectors;

    // reprezentacja przekazanej tablicy z java do kodu natywnego
    jint *argb = (*env).GetIntArrayElements(argb_, NULL);
    char *fileData = new char[cols * rows];
    for (int i = 0; i < cols * rows; ++i) {
        UPixel temp;
        temp.argb = argb[i];
        fileData[i] = temp.chanels[1];
    }

    (*env).ReleaseIntArrayElements(argb_, argb,0);

    PGMFile pgmFile(fileData, cols * rows);

    //wczytaj obrazek
    h = rows;
    w = cols;
    max_color = 255;
    unsigned char **a = new_char_image(h, w);
    if (pgmFile.readPGMB_data(a[0], hpos, h, w, max_color) == 0)
        return NULL;

    unsigned char **b = new_char_image(h, w);

    //znajdz kontur i zwroc jego pierwszy piksel
    int index = create_contour(a, b, h, w);

    //utwórz obrazek kolorowy tylko do celow testowych (wizualizacji działania poszczegolnych funkcji)
    unsigned char **R = new_char_image(h, w);
    unsigned char **G = new_char_image(h, w);
    unsigned char **B = new_char_image(h, w);
    for (i = 0; i< h; ++i)	for (j = 0; j< w; ++j) R[i][j] = G[i][j] = B[i][j] = b[i][j];

    ret = 0;
    finger f[5];
    float szerokosc_dloni = 0;
    int szerokosc_dloni_index = 0;

    for (;;) {		//petla for tylko po to zeby wyjsc z wyszukiwania cech jesli ktorejs nie znajdziemy po drodze

        CLS_piksels cpv;	//ciagu pikseli konturu, zawiera indeksy (wspolrzedne z obrazka wejsciowego: index=y*w+x)
        //przekształcenie kontutu na ciąg pikseli
        if (index) T_ClearContur_and_copy_to_vector(b[0], w, h, 255, 0, index, cpv);
        if (cpv.size() < 2 * h) break;	//przerwij jesli napokano na klaster ktory nie jest dlonia - np. obrazek zawiera wiecj niz jeden klaster

        int ex[12]; for (int i = 0; i<12; i++) ex[i] = 0;	//tablica numerów punktow z ciągu pikseli konturu dla punktow bedacych ekstremami
        //wyznaczanie ekstremów - czubkow palców - poza kciukiem
        if ((ret += Find_Top_Extremas(cpv, a, ex, h, w)) != 4) break; //nie przechodzimy dalej jesli cos nie tak z wyszukaniem czubkow palców

        //wyznaczanie ekstremów dolnych - poza kciukiem
        if ((ret += Find_Bottom_Extremas(cpv, a, ex, h, w)) != 7) break;  	//nie przechodzimy dalej jesli cos nie tak z wyszukaniem zaglebien palców
        //draw_circles(R, G, B, h, w, cpv, ex, 7, 12, 255, 0, 0);
        //draw_circles(R, G, B, h, w, cpv, ex, 0, 7, 0, 255, 0);
        //break;

        //wyznaczanie zaglebienia pomiedzy kciukiem a wskazujacym - poza kciukiem, dodanie do bottom_extrems_vector jako tatni punkt
        if ((ret += Find_Thumb_Bottom_Extremum(a, cpv, ex, h, w)) != 8) break;

        //wyznaczanie czubka kciuka
        if ((ret += Find_Thumb_Top_Extremum(cpv, ex, h, w)) != 9) break;

        //szukanie bocznych ekstremów - pomiędzy top palca wskazujacego i bottom kciuka
        if ((ret += Find_Left_Side_Bottom_Extremum(cpv, ex, h, w)) != 10) break;

        //mały palec od prawej
        if ((ret += Find_Right_Side_Bottom_Extremum(cpv, ex, h, w)) != 11) break;

        //podstawa kciuka od dołu
        if ((ret += Find_Thumb_Side_Bottom_Extremum(cpv, ex, h, w)) != 12) break;

        //szerokosc dloni
        if ((ret += Find_Hand_width(cpv, ex, h, w, &szerokosc_dloni_index, &szerokosc_dloni)) != 13) break;

        ////"Visual Debug" "podswietl zaznaczone punkty na czerwono i wyświetl na obrazku - tylko w celach testowych
        //draw_circles(R, G, B, h, w, cpv, ex, 7, 12, 255, 0, 0);
        //draw_circles(R, G, B, h, w, cpv, ex, 0, 7, 0, 255, 0);
        #if defined (_MSC_VER)
        if (pgmFile.writePPMB_image_to_file(std::string("E:\\img_\\wynik_etap2_d.ppm"), R[0], G[0], B[0], h, w, 255) == 0) exit(1);
                //if (writePPMB_image(test_image.c_str(), R[0], G[0], B[0], h, w, 255) == 0) exit(1);
        #endif // defined (_MSC_VER)

        //oblicz wartości cech związanych z palcami
        memset(f, 0, 5 * sizeof(finger));
        int ex_i[12]; for (int i = 0; i<12; i++) ex_i[i] = cpv[ex[i]];		//wyznacz indeksy punktow extremalnych
        calc_fingers_feature(f, a, ex_i, h, w);

        break;
    }//for


    i = 0;
    if (ret == 13) {		//znaleziono wszystkie cechy, mozemy wiec stworzyc wektor cech

        float fv[30]; for (j = 0; j<30; j++) fv[j] = 0;	//rozmiar na sztywno 30 nalezy zmienic jesli mamy inna ilosc cech

        //cechy zwiazane z palcami
        for (j = 0; j<5; j++) {
            fv[i++] = f[j].length;					//długość palca
            fv[i++] = f[j].base_length;				//długość podstawy palca
            fv[i++] = f[j].area;					//powierzchnia palca

            //sprawdz, czy wspolrzedne okregow wpisanych sa miedzy punktami podstawy i czubka palca.. - glowniw w celu wyeliminowania niestarannych probek
            //gdzie widac kawalek twarzy lub nieskasowanego tla
            //sprawdzamy tylko wspolrzedne y, mozna dodac rowniez odpowiednia kontrole na x
            if (f[j].circle_top_centre / w < f[j].base_center_point / w && f[j].circle_top_centre / w > f[j].top_point / w)
                fv[i++] = f[j].circle_top_radius;		//promien największego okrągu wpisany w górnej połowie palców

            if (j>0) {		//dla palcow nie beadacych kciukiem
                if (f[j].circle_bottom_centre / w < f[j].base_center_point / w && f[j].circle_bottom_centre / w > f[j].top_point / w)
                    fv[i++] = f[j].circle_bottom_radius;//promien największego okrągu wpisany w dolnej połowie palców
                fv[i++] = f[j].distance_from_thumb;
            }
        }

        //cechy zwiazane z dlonia
        fv[i++] = f[1].base_first_point, f[4].base_last_point;
        fv[i++] = szerokosc_dloni;

        //sprawdz czy wszystkie pola w fv sa rozne od zera - powinny być jesli wszystko znaleziono jak nalezy
        if (i == 30) {
        i = 0;
        for (j = 0; j<30; j++) if (fv[j] != 0) i++;
        }


        if (i == 30) {		//jesli wszystko dopisz cechy do pliku feature_vectors.dat
            for (size_t i = 0; i < 30; ++i) {
                featureVectors.push_back(fv[i]);
            }
        }

        // przygotawanie tablicy dla plixeli obrazu po binaryzacji
        // taka tablize mozna zwrucic do kodu java
        if (i == 30) {
            jfloatArray result;
            result = (*env).NewFloatArray(30);
            if (result == NULL) {
                return NULL; /* out of memory error thrown */
            }
            // pobranie wskaznika na utworzona tablice (jint - java int = int(c++))
            jfloat *result_tab = (*env).GetFloatArrayElements(result, NULL);
            for (int i = 0; i < 30 /*featureVectors.size()*/; ++i) {
                result_tab[i] = featureVectors[i];
            }

            /* Tu pewnie znow zla dealokacja!!! pasuje porpawic!!!*/
            delete[] a;
            delete[] b;

            delete[] R;
            delete[] G;
            delete[] B;

            return result;
        }
    }

    /* Tu pewnie znow zla dealokacja!!! pasuje porpawic!!!*/
    delete[] a;
    delete[] b;

    delete[] R;
    delete[] G;
    delete[] B;

    return NULL;
}


JNIEXPORT jintArray JNICALL Java_com_app_checkpresence_OpenCVSubtraction_deleteSmallAreas(JNIEnv *env, jobject instance, jintArray argb_,
                                                                                 jint rows, jint cols) {
    // reprezentacja przekazanej tablicy z java do kodu natywnego
    jint *argb = (*env).GetIntArrayElements(argb_, NULL);

    int i, j;

    //przygotowanie czarno-bialej tablicy wyjsciowej
    unsigned char **b_out = new_char_image(rows, cols);

    // kopiowanie tablicy ARGB do tablicy tylko z kanalem R
    UPixel pixel_copier;
    for (i = 0; i< rows; ++i) {
        for (j = 0; j < cols; ++j) {
            // kopiowanie 1 kanału
            pixel_copier.argb = argb[static_cast<int>(cols * i + j)];
            if (pixel_copier.chanels[1] == 0)
                b_out[i][j] = 255;
            else
                b_out[i][j] = 0;
        }
    }

    //(*env).ReleaseIntArrayElements(argb_, argb,0);

    int w = static_cast<int>(cols);
    int h = static_cast<int>(rows);
    unsigned char tlo = 0;
    unsigned char* b1 = b_out[0];

    //"czyszczenie" obrazu, znajdz wszystkie  biale klastry i wyczyść jej jesli sa male

    int minimum = 110;//220;

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
                if (n > 3) b_out[i][j]=255;
            }

        }
    }

    /**
     * kopiowanie do tablicy przekazanej z javasd
     */
    // przygotawanie tablicy dla plixeli obrazu po binaryzacji
    // taka tablize mozna zwrucic do kodu java
    jintArray result;
    result = (*env).NewIntArray(rows*cols);
    if (result == NULL) {
    return NULL; /* out of memory error thrown */
    }
    // pobranie wskaznika na utworzona tablice (jint - java int = int(c++))
    jint *result_tab= (*env).GetIntArrayElements(result, NULL);

    int ilea = 0;
    int ileb = 0;
    for (i = 0; i< rows; ++i) {
        for (j = 0; j < cols; ++j) {
            // kopiowanie 1 kanału
            int index = cols * i + j;
            //pixel_copier.argb = argb[index];
            if (b_out[i][j] == 0) {
                result_tab[index] = 0xFFFFFFFF;
                ++ilea;
            }
            else {
                result_tab[index] = 0xFF000000;
                ++ileb;
            }
        }
    }

    delete b_out[0];// = new unsigned char[rows*cols];
    delete b_out;// = new unsigned char*[rows];

    delete b2[0];
    delete b2;

    (*env).ReleaseIntArrayElements(argb_, argb,0);

    return result;
}
JNIEXPORT jintArray JNICALL Java_com_app_checkpresence_Segmentation_myNativeCode(JNIEnv *env, jobject instance, jintArray argb_,
                                                                               jint rows, jint cols, jint warunek){

    // reprezentacja przekazanej tablicy z java do kodu natywnego
    jint *argb = (*env).GetIntArrayElements(argb_, NULL);
    unsigned char* plikDaneARGB = (unsigned char*)argb;

    // dolugosc pliku - zalezenie od etapu 2(zostanie usunieta pozniej przy downgradzie jak juz bedzie dzialac)
    int dataLength = rows * cols * 3;

    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TEST", "Testowy log z NativeCode c++. Hello console :)");

    // przygotowanie naglowka pliku(jako (w zmiennej) std::stringstream fileData)
    std::stringstream fileData;
    //fileData << "P6\n" << cols << " " << rows << "\n# eyetom.com\n" << 255 << "\n";
    //int headerLength = fileData.str().size();
    //dataLength += headerLength;

    int avgR = 0;
    int avgG = 0;
    int avgB = 0;
    for (size_t i = 0; i < rows * cols * 4; i += 4) {
        // odczytanie pixela z tablicy(obaz z kamery)
        char a = plikDaneARGB[i + 3];
        char r = plikDaneARGB[i + 2];
        char g = plikDaneARGB[i + 1];
        char b = plikDaneARGB[i + 0];

        // zapis pixela do "pliku"
        fileData.write(reinterpret_cast<char *>(&r), 1);
        fileData.write(reinterpret_cast<char *>(&g), 1);
        fileData.write(reinterpret_cast<char *>(&b), 1);

        // sumowanie pixeli w poszczegulnych kanałach
        avgR += r;
        avgG += g;
        avgB += b;
    }

    // obliczenie sredniego koloru dla kanałów RGB obazu
    avgR = avgR / (rows * cols);
    avgG = avgG / (rows * cols);
    avgB = avgB / (rows * cols);


    // przygotawanie tablicy dla plixeli obrazu po binaryzacji
    // taka tablize mozna zwrucic do kodu java
    jintArray result;
    result = (*env).NewIntArray(rows*cols);
    if (result == NULL) {
        return NULL; /* out of memory error thrown */
    }
    // pobranie wskaznika na utworzona tablice (jint - java int = int(c++))
    jint *result_tab= (*env).GetIntArrayElements(result, NULL);


    // 1'szy etap projektu
    //
    PaPaMobile_HandRecognization((int*)result_tab, fileData.str(), rows, cols, warunek, avgR, avgG, avgB);

    // dealokujemy pamiec dla tablicy przechowujacej obraz z aparatu(kolorowy)
    (*env).ReleaseIntArrayElements(argb_, argb,0);

    return result;
    }
}

void PaPaMobile_HandRecognization(int* image_out, std::string fileData, int rows, int cols, int warunek, int avgR, int avgG, int avgB) {
    //int rows, cols;
    int max_color = 255;
    int hpos = 0, i, j;
    PGMFile pgmFile(fileData.c_str(), rows * cols);
    //return "test happy 01";

    //if ((hpos = pgmFile.readPGMB_header(&rows, &cols, &max_color)) <= 0)
        //return ;//"nie udało się wczytać nagłówka";

    unsigned char **R = new_char_image(rows, cols);
    unsigned char **G = new_char_image(rows, cols);
    unsigned char **B = new_char_image(rows, cols);

    if (pgmFile.readPPMB_data(R[0], G[0], B[0], hpos, rows, cols, max_color) == 0)	   exit(1);

    //przygotowanie czarno-bialej tablicy wyjsciowej
    unsigned char **b_out = new_char_image(rows, cols);

    int avgHueHandColor = rgb2hsv(R[rows / 2][cols / 2], G[rows / 2][cols / 2], B[rows / 2][cols / 2])[0];

    //__android_log_print(ANDROID_LOG_DEBUG, "LOG_TEST", "Pętle segmentacji...");
    for (i = 0; i< rows; ++i) {
        for (j = 0; j< cols; ++j) {

            int r = R[i][j];
            int g = G[i][j];
            int b = B[i][j];
            int cond = 0;

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
            /*if (warunek == 0) { warunek = r>120 && r>g && r>b; }
            if (warunek == 1) { warunek = (r>50 && r>g && r>b) || (r>90 && r>g && r>g - 10); }
            if (warunek == 2) { warunek = (r>100 && r>g && r>b) || (r>200); }
            if (warunek == 3) { warunek = (r>65 && r>g && r>b - 10) || (i<200 && r>25 && r>g && r>b - 10); }
            if (warunek == 4) { warunek = (r>100 && r>g && r>b - 10); }
            if (warunek == 5) { warunek = (r>120 && r>g && r>b); }
            if (warunek == 6) { warunek = (r>80 && r>g && r>b) || (r>100 && r>g && r>b - 20); }*/
            //warunek = !(r > 170 && g > 170 && b > 170);

            // ramka obrazu zawsze czarna
            if ((i < 10 || i > rows - 20)
                && (j < 10 || j > cols - 10)) {
                b_out[i][j] = 0;
            }
            // sprawdzanie czy pixel nalezy do reki
            else {
                int avgRGB = (r + g + b) / 3;
                //int deviation = 10;
                if(warunek == 1) cond = (!(r > avgR && g > avgG && b > avgB) && (r > b && r > g) );
                else if(warunek == 2) cond = (r>50 && r>g && r>b) || (r>90 && r>g && r>g - 10);
                else if(warunek == 3) cond = (r > 65 && r>g && r > b - 10) || (i < 200 && r>25 && r > g && r > b - 10);
                else if(warunek == 4) cond = (r>65 && r>g && r>b-10) || (i<200 && r>25 && r>g && r>b-10);
                /*warunek = (r > b && r > g) && !((r > avgRGB - deviation && r > avgRGB + deviation)
                                              && (b > avgRGB - deviation && b > avgRGB + deviation)
                                              && (g > avgRGB - deviation && g > avgRGB + deviation));*/

                b_out[i][j] = cond ? 0 : 255;
            }
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
                if (n > 3) b_out[i][j]=255;
            }

        }
    }


#if defined (_MSC_VER)
    /* UWAGA:
	   Ten fragment działa tylko w Visual Studio
	   trzeba ustawić scieszke jezeli ma nie zapisywac pliku tam gdzie scieszka projektu*/
	std::string outfname = "E:\\img_\\output__separated.pgm";
	if (pgmFile.writePGMB_image_to_file(outfname, b_out[0], rows, cols, 255) != E_OK)
		return NULL;
	//if (writePGMB_image(outfname.c_str(), b_out[0], rows, cols, 255) == 0)	   exit(1);
#endif

    //std::string daneAfterSegmentation;
    //std::cout << daneAfterSegmentation << std::endl;
    //pgmFile.writePGMB_image_to_string(daneAfterSegmentation, b_out[0], rows, cols, 255);
    pgmFile.writePGMB_image_to_tableInt(image_out, b_out[0], rows, cols, 255);

    // to trzeba jeszcze poprawić wede alokacji pamięci
    delete[] R;
    delete[] G;
    delete[] B;

    delete b_out[0];// = new unsigned char[rows*cols];
    delete b_out;// = new unsigned char*[rows];

    delete b2[0];
    delete b2;

    /* UWAGA:
       Wynik do nastepnego etapu znajduje sie w zmiennej std::string daneAfterSegmentation
    */

    /***********************************
    Tu powinno być wywołanie funkcji odpowiedzialen za następny etap przetwarzania!
    int etap2(daneAfterSegmentation, daneAfterSegmentation.size());
    ************************************/

}