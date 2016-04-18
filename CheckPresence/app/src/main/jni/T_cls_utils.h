#pragma once

/*
* Created: 2015-02-11
* Author : Tomasz G¹ciarz
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

typedef vector<CLS> CLSV;
//typedef vector<CLS> CLSDV;					//directional clusters
typedef vector<unsigned int> CLS_piksels;	//coordinates of single cluster piksels
typedef vector<CLS_piksels> CLSV_piksels;


void T_FindAllClusters(unsigned char**img, int w, int h, unsigned char color, CLSV &clusters);
void T_ClearOneClaster(unsigned char*a, int w, int h, unsigned char color, unsigned char tlo, unsigned int index);

void take_string_piksels(unsigned short **mag, int w, int y, int x, CLS_piksels &cls_piksels);
void T_Clear_And_Copy_To_Image_All_Clusters_Pikels(unsigned char*image, unsigned short **mag, int w, int h, CLSV &clusters);
void T_Select_Not_Encapsulated_Clusters(CLSV &a_clusters, int w);

int test_how_much_neighburs_pixel(unsigned char** a, int x, int y, unsigned char color);
int T_Remove_Corner_Pixels_From_Clusters(unsigned char** outbuf, int w, int h);

#endif
