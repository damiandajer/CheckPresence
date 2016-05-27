#if !defined(DEF_T_DRAW_UTILS)
#define DEF_T_DRAW_UTILS

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vector>

using namespace std;

void draw_circle_grey(unsigned char**a, int h, int w, int px, int py, int r, char color);
void draw_circle_color(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int px, int py, int r, char colorR, char colorG, char colorB);
void draw_line(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int p0, int p1, char colorR, char colorG, char colorB);
//void draw_line_grey(unsigned char**a, int w, int p0,int p1, unsigned char color);
void draw_line_grey(unsigned char**a, int h, int w, int p0, int p1, unsigned char color, int thikness = 1);
void draw_line_color(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int x0, int y0, int x1, int y1, char colorR, char colorG, char colorB);
void draw_circles(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, CLS_piksels &p, int *ex, int begin, int end, char colorR, char colorG, char colorB);
void draw_circle(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int middle_index, int r, char colorR, char colorG, char colorB);

void draw_parallel_line_through_whole_image(unsigned char**a, int h, int w, int p0, int p1, int p, unsigned char color, int thikness);
#endif