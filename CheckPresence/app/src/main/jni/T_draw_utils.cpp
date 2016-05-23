#include <math.h>
#include "T_cls_utils.h"
#include "T_draw_utils.h"


//funkcja rysujaca okręg
void draw_circle_grey(unsigned char**a, int h, int w, int px, int py, int r, char color)
{
    int x, y, d;

    if (py - r<0 || py + r>h - 1 || px - r<0 || px + r>w - 1) return;

    int x_max = (int)(r / sqrt(2.0)) + 1;
    y = r;
    d = -r;
    a[r + py][0 + px] = a[py - r][0 + px] = a[py][px + r] = a[py][px - r] = color;

    int i = 0;
    for (x = 1; x<x_max; x++)
    {
        d += 2 * x - 1;
        if (d >= 0) { y--; d -= 2 * y; }
        a[py + y][px + x] = a[py + y][px - x] = a[py - y][px + x] = a[py - y][px - x] = a[py + x][px + y] = a[py - x][px + y] = a[py - x][px - y] = a[py + x][px - y] = color;
    }
}

void draw_circle_color(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int px, int py, int r, char colorR, char colorG, char colorB)
{
    int x, y, d;

    int x_max = (int)(r / sqrt(2.0)) + 1;

    if (py - r<0 || py + r>h - 1 || px - r<0 || px + r>w - 1) return;

    y = r;
    d = -r;
    R[r + py][0 + px] = R[py - r][0 + px] = R[py][px + r] = R[py][px - r] = colorR;
    G[r + py][0 + px] = G[py - r][0 + px] = G[py][px + r] = G[py][px - r] = colorG;
    B[r + py][0 + px] = B[py - r][0 + px] = B[py][px + r] = B[py][px - r] = colorB;

    int i = 0;
    for (x = 1; x<x_max; x++)
    {
        d += 2 * x - 1;
        if (d >= 0) { y--; d -= 2 * y; }
        R[py + y][px + x] = R[py + y][px - x] = R[py - y][px + x] = R[py - y][px - x] = R[py + x][px + y] = R[py - x][px + y] = R[py - x][px - y] = R[py + x][px - y] = colorR;
        G[py + y][px + x] = G[py + y][px - x] = G[py - y][px + x] = G[py - y][px - x] = G[py + x][px + y] = G[py - x][px + y] = G[py - x][px - y] = G[py + x][px - y] = colorG;
        B[py + y][px + x] = B[py + y][px - x] = B[py - y][px + x] = B[py - y][px - x] = B[py + x][px + y] = B[py - x][px + y] = B[py - x][px - y] = B[py + x][px - y] = colorB;
    }
}

void draw_circle(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int middle_index, int r, char colorR, char colorG, char colorB)
{
    int x, y, d;
    int py = middle_index / w;
    int px = middle_index%w;

    if (r<1 || py - r<0 || py + r>h - 1 || px - r<0 || px + r>w - 1) return;

    int x_max = (int)(r / sqrt(2.0)) + 1;
    y = r;
    d = -r;
    R[r + py][0 + px] = R[py - r][0 + px] = R[py][px + r] = R[py][px - r] = colorR;
    G[r + py][0 + px] = G[py - r][0 + px] = G[py][px + r] = G[py][px - r] = colorG;
    B[r + py][0 + px] = B[py - r][0 + px] = B[py][px + r] = B[py][px - r] = colorB;

    int i = 0;
    for (x = 1; x<x_max; x++)
    {
        d += 2 * x - 1;
        if (d >= 0) { y--; d -= 2 * y; }
        R[py + y][px + x] = R[py + y][px - x] = R[py - y][px + x] = R[py - y][px - x] = R[py + x][px + y] = R[py - x][px + y] = R[py - x][px - y] = R[py + x][px - y] = colorR;
        G[py + y][px + x] = G[py + y][px - x] = G[py - y][px + x] = G[py - y][px - x] = G[py + x][px + y] = G[py - x][px + y] = G[py - x][px - y] = G[py + x][px - y] = colorG;
        B[py + y][px + x] = B[py + y][px - x] = B[py - y][px + x] = B[py - y][px - x] = B[py + x][px + y] = B[py - x][px + y] = B[py - x][px - y] = B[py + x][px - y] = colorB;
    }
}

void draw_line_grey(unsigned char**a, int h, int w, int p0, int p1, unsigned char color, int thikness)
{
    int y0 = p0 / w;
    int x0 = p0 - y0*w;
    int y1 = p1 / w;
    int x1 = p1 - y1*w;
    int hf = thikness / 2;		//half thikness

    int dx = abs(x1 - x0), sx = x0<x1 ? 1 : -1;
    int dy = abs(y1 - y0), sy = y0<y1 ? 1 : -1;
    int err = (dx>dy ? dx : -dy) / 2, e2;

    for (;;) {
        a[y0][x0] = color;
        //jesli grubsza linia obrysuj kazdy piksel w okolo danego
        if (thikness != 1 && y0>hf && x0>hf && x0<w - hf && y0<h - hf) {
            for (int i = -hf; i <= hf; i++)	for (int j = -hf; j <= hf; j++) a[y0 + i][x0 + j] = color;
        }

        if (x0 == x1 && y0 == y1) break;
        e2 = err;
        if (e2 >-dx) { err -= dy; x0 += sx; }
        if (e2 < dy) { err += dx; y0 += sy; }
    }
}

//rysuj przez cały obraz linie prostą przechodzacą przez punkt p i rownolegla do prostej przechodzacej przez punky p0 i p1
void draw_parallel_line_through_whole_image(unsigned char**a, int h, int w, int p0, int p1, int p, unsigned char color, int thikness)
{
#define getX(yy) ((yy-yp)/m + xp)
#define getY(xx) ((xx-xp)*m + yp)

    int y0 = p0 / w;
    int x0 = p0 - y0*w;
    int y1 = p1 / w;
    int x1 = p1 - y1*w;
    int yp = p / w;
    int xp = p - yp*w;
    int hf = thikness / 2;		//half thikness
    float y, x;

    int index[2];		//indexs of border pixels

    int i = 0;

    float m = (float)(y1 - y0) / (float)(x1 - x0);
    //(y-yp)/(x-xp) = m;
    //x = (y-yp)/m + xp
    //y = (x-xp)*m + yp

    if ((x = getX(0)) >= 0 && x<w) { index[i] = (int)x; i++; }
    if ((x = getX(h - 1)) >= 0 && x<w) { index[i] = (h - 1)*w + (int)x; i++; }
    if ((i<2) && (y = getY(0)) >= 0 && y<w) { index[i] = (int)y*w; i++; }
    if ((i<2) && (y = getY(w - 1)) >= 0 && y<w) { index[i] = (int)y*w + w - 1; i++; }

    if (i == 2) {
        draw_line_grey(a, h, w, index[0], index[1], color, 2);
    }
}

void draw_line(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int p0, int p1, char colorR, char colorG, char colorB)
{
    int y0 = p0 / w;
    int x0 = p0 - y0*w;
    int y1 = p1 / w;
    int x1 = p1 - y1*w;

    int dx = abs(x1 - x0), sx = x0<x1 ? 1 : -1;
    int dy = abs(y1 - y0), sy = y0<y1 ? 1 : -1;
    int err = (dx>dy ? dx : -dy) / 2, e2;

    for (;;) {
        R[y0][x0] = colorR; G[y0][x0] = colorG; B[y0][x0] = colorB;
        if (x0 == x1 && y0 == y1) break;
        e2 = err;
        if (e2 >-dx) { err -= dy; x0 += sx; }
        if (e2 < dy) { err += dx; y0 += sy; }
    }
}

void draw_line_color(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, int x0, int y0, int x1, int y1, char colorR, char colorG, char colorB)
{
    int dx = abs(x1 - x0), sx = x0<x1 ? 1 : -1;
    int dy = abs(y1 - y0), sy = y0<y1 ? 1 : -1;
    int err = (dx>dy ? dx : -dy) / 2, e2;

    for (;;) {
        R[y0][x0] = colorR; G[y0][x0] = colorG; B[y0][x0] = colorB;
        if (x0 == x1 && y0 == y1) break;
        e2 = err;
        if (e2 >-dx) { err -= dy; x0 += sx; }
        if (e2 < dy) { err += dx; y0 += sy; }
    }
}


void draw_circles(unsigned char**R, unsigned char**G, unsigned char**B, int h, int w, CLS_piksels &p, int *ex, int begin, int end, char colorR, char colorG, char colorB)
{
    unsigned char* r = R[0];
    unsigned char* g = G[0];
    unsigned char* b = B[0];

    for (int i = begin; i<end; i++) {

        if (ex[i] == 0) continue;
        int offset = p[ex[i]];		//index piksela na obrazie wejsciowym
        int py = offset / w;
        int px = offset - py*w;
        draw_circle_color(R, G, B, h, w, px, py, 5, colorR, colorG, colorB);
    }
}