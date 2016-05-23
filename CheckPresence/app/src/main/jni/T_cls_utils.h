#pragma once

/*
* Created: 2015-02-11
* Author : Tomasz Gąciarz
*
* Copyright (C) 2011.  All rights reserved.
*/
#if !defined(DEF_T_CLUSTERS)
#define DEF_T_CLUSTERS

#include <string.h>
#include <vector>

using namespace std;
struct RGB
{
	unsigned char b;
	unsigned char g;
	unsigned char r;
};

struct CLS
{
	unsigned int minx_index;
	unsigned int maxx_index;
	unsigned int miny_index;
	unsigned int maxy_index;
	unsigned int status;
	unsigned int size;
	unsigned int offset;
};

//struct CLSD
//{
//	unsigned int minx_index;
//	unsigned int maxx_index;
//	unsigned int miny_index;
//	unsigned int maxy_index;
//	unsigned int status;
//	unsigned int size;
//	unsigned int offset;
//};


struct circle
{
	int s;		//index srodka okregu
	int r;
};

struct finger
{
	int base_first_point;			//indexy punktow na obrazie
	int base_last_point;
	int base_center_point;
	int top_point;

	int base_length;
	int length;
	int area;
	int distance_from_thumb;
	int circle_top_centre;
	int circle_top_radius;
	int circle_bottom_centre;
	int circle_bottom_radius;
};

typedef vector<CLS> CLSV;
//typedef vector<CLS> CLSDV;					//directional clusters
typedef vector<unsigned int> CLS_piksels;	//coordinates of single cluster piksels
typedef vector<CLS_piksels> CLSV_piksels;

int create_contour(unsigned char **a, unsigned char **b, int h, int w);

void T_ClearContur_and_copy_to_vector(unsigned char*a, int w, int h, unsigned char color, unsigned char tlo, unsigned int index, CLS_piksels &piksels);
int Find_Top_Extremas(CLS_piksels &p, unsigned char **a, int *e, int h, int w);
int Find_Bottom_Extremas(CLS_piksels &p, unsigned char **a, int *e, int h, int w);
int Find_Thumb_Bottom_Extremum(unsigned char **a, CLS_piksels &p, int *e, int h, int w);
int Find_Thumb_Top_Extremum(CLS_piksels &p, int *e, int h, int w);
float dist(int i, int j, int w);
int Find_Left_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w);
int Find_Right_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w);
int Find_Thumb_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w);
int Find_Hand_width(CLS_piksels &p, int *e, int h, int w, int *index, float *szerokosc_dloni);
int middle_point(int p1, int p2, int w);
void dt(unsigned *_d, unsigned char *_bimg, int _h, int _w);
void copy_top_image_part(unsigned char **a, unsigned char **b, int h, int w, unsigned char tlo, unsigned char cl);
void copy_bottom_image_part(unsigned char **a, unsigned char **b, int h, int w, unsigned char tlo, unsigned char cl);
void calc_fingers_feature(finger *f, unsigned char **img, int *ex_i, int h, int w);
//int Find_Next_Top_Extremum(CLS_piksels &p, CLSV &e, int h, int w);
//int Find_Top_Extremas(CLS_piksels &p, unsigned char **m, int *e, int h, int w);
//int Find_Bottom_Extremas(CLS_piksels &p, CLSV &e, int h, int w, int start_index, int end_index);
//int Find_Thumb_Bottom_Extremum(unsigned char **m, CLS_piksels &p, int *e, int h, int w);
//int Find_Thumb_Top_Extremum(CLS_piksels &p, int *e, int h, int w);
//int Find_Left_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w);
//int Find_Right_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w);
//int Find_Thumb_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w);
//int Find_Bottom_Extremas(CLS_piksels &p, unsigned char **a, int *e, int h, int w);
//int Find_Hand_width(CLS_piksels &p, int *e, int h, int w, int *index, float *szerokosc_dloni);
//void calc_fingers_feature(finger *fingers, unsigned char **a, int *, int, int);


void T_FindAllClusters(unsigned char**img, int w, int h, unsigned char color, CLSV &clusters);
void T_ClearOneClaster(unsigned char*a, int w, int h, unsigned char color, unsigned char tlo, unsigned int index);
int T_ClearOneClasterAndCopy(unsigned char*a, unsigned char*b, int w, int h, unsigned char color, unsigned char tlo, unsigned int index);

void take_string_piksels(unsigned short **mag, int w, int y, int x, CLS_piksels &cls_piksels);
void T_Clear_And_Copy_To_Image_All_Clusters_Pikels(unsigned char*image, unsigned short **mag, int w, int h, CLSV &clusters);
void T_Select_Not_Encapsulated_Clusters(CLSV &a_clusters, int w);

int test_how_much_neighburs_pixel(unsigned char** a, int x, int y, unsigned char color);
int T_Remove_Corner_Pixels_From_Clusters(unsigned char** outbuf, int w, int h);

#endif
