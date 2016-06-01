package com.app.handfeatures;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by bijat on 26.05.2016.
 */
public class ImageUtils {
    public static int howManyNeighbours(MyImage image, int x, int y, byte color) throws Exception {
        int num = 0;

        /*for (int py = y - distance; py < y + distance + 1; ++py) {
            for (int px = x - distance; px < x + distance + 1; ++px) {
                if (py == x && px == y) continue;
                else ++num;
            }
        }*/

        if (image.pixel(x - 1, y - 1) == color) ++num;
        if (image.pixel(x,     y - 1) == color) ++num;
        if (image.pixel(x + 1, y - 1) == color) ++num;
        if (image.pixel(x - 1, y)     == color) ++num;
        if (image.pixel(x + 1, y)     == color) ++num;
        if (image.pixel(x - 1, y + 1) == color) ++num;
        if (image.pixel(x,     y + 1) == color) ++num;
        if (image.pixel(x + 1, y + 1) == color) ++num;

        return num;
    }

    public static void fillAllArea(MyImage image, byte ref_color, byte clearToColor, int maxAreaNumPixels) throws Exception {
        boolean[][] mapImage = new boolean[image.height()][image.width()];

        for (int iter_y = 0; iter_y < image.height(); ++iter_y) {
            for (int iter_x = 0; iter_x < image.width(); ++iter_x) {
                mapImage[iter_y][iter_x] = false;
            }
        }

        ArrayList<Point> processedPoint = new ArrayList<>();
        ArrayList<Point> toProcess = new ArrayList<>();

        // dla kazdego pixela w obrazia sproboj znales poczatek 'plamy'
        for (int y = 1; y < image.height() - 1; ++y) {
            for (int x = 1; x < image.width() - 1; ++x) {
                if (mapImage[y][x] == false && image.pixel(x, y) == ref_color) { // znalezione poczatek 'plamy'
                    // dodajemy 1'szy pixel do przetwarzanie, od niego zaczyna sie 'rozrost'(szukanie nastepnych)
                    toProcess.add(new Point(x, y));
                    mapImage[y][x] = true;
                    image.setPixel(x, y, clearToColor);

                    int startFrom = 0; // index ostatniege pixela z poprzedniego przebiegu

                    // dla pixeli znalezionych w ostatnim przebiegu
                    for (int i = startFrom; i < toProcess.size(); ++i) {
                        startFrom = toProcess.size();
                        int p_x = toProcess.get(i).x;
                        int p_y = toProcess.get(i).y;

                        if (p_x <= 0 || p_x >= image.width() - 1) {
                            System.out.println("if (p_x <= 0 || p_x >= image.width() - 1){");
                            continue;
                        }
                        if (p_y <= 0 || p_y >= image.height() - 1) {
                            System.out.println("if (p_y <= 0 || p_y >= image.height() - 1){");
                            continue;
                        }

                        // pixele otaczajace
                        boolean p_left = mapImage[p_y][p_x - 1] == false && image.pixel(p_x - 1, p_y) == ref_color;
                        boolean p_right = mapImage[p_y][p_x + 1] == false && image.pixel(p_x + 1, p_y) == ref_color;
                        boolean p_up = mapImage[p_y - 1][p_x] == false && image.pixel(p_x, p_y - 1) == ref_color;
                        boolean p_down = mapImage[p_y + 1][p_x] == false && image.pixel(p_x, p_y + 1) == ref_color;

                        // jezeli nie odwiedzono jeszcze pixela i ma kolor plamki
                        if (p_left) {
                            toProcess.add(new Point(p_x - 1, p_y));
                            mapImage[p_y][p_x - 1] = true;
                            image.setPixel(p_x - 1, p_y, clearToColor);
                        }
                        if (p_right) {
                            toProcess.add(new Point(p_x + 1, p_y));
                            mapImage[p_y][p_x + 1] = true;
                            image.setPixel(p_x + 1, p_y, clearToColor);
                        }
                        if (p_up) {
                            toProcess.add(new Point(p_x, p_y - 1));
                            mapImage[p_y - 1][p_x] = true;
                            image.setPixel(p_x, p_y - 1, clearToColor);
                        }
                        if (p_down) {
                            toProcess.add(new Point(p_x, p_y + 1));
                            mapImage[p_y + 1][p_x] = true;
                            image.setPixel(p_x, p_y + 1, clearToColor);
                        }
                    } // for (int i = startFrom; i < toProcess.size(); ++i)

                    // znaleziona wieksza plame nix maxAreaNumPixel - czyli moze to byc np. przestrzen miedy palcami i nie nalezy jej zapelniac
                    // pixele zostaja ustawione spowrotem, ale w mapImage info. pozostanie o odwiedzonych pixelach
                    if (startFrom > maxAreaNumPixels) {
                        for (Point p : toProcess)
                            image.setPixel(p.x, p.y, ref_color);
                    }
                    toProcess.clear();
                } //if (image.pixel(x, y) == ref_color)
            }
        }
    }

    // return number of cleared pixels in clauster
    public static int clearClaster(MyImage image, byte ref_color, byte clearColor, int x, int y, int maxNumOfAreaPixels) throws Exception {

        //std::chrono::steady_clock::time_point start = std::chrono::steady_clock::now();
        boolean pokazDebugNapis = false;
        boolean[][] mapImage = new boolean[image.height()][image.width()];

        for (int iter_y = 0; iter_y < image.height(); ++iter_y) {
            for (int iter_x = 0; iter_x < image.width(); ++iter_x) {
                mapImage[iter_y][iter_x] = false;
            }
        }

        int clearedPixels = 0;

        ArrayList<Point> processedPoint = new ArrayList<>();
        processedPoint.clear();
        ArrayList<Point> toProcess = new ArrayList<>();
        toProcess.add(new Point(x, y));

        int ile = 0;
        while (toProcess.size() > 0) {
            while (toProcess.size() > 0) {
                Point point = toProcess.get(0);
                if (pokazDebugNapis) {
                    String ss = new String();
                    ss = "x:" + point.x + ", " + "y:" + point.y + " - " + image.pixel(point.x, point.y) + " ";
                    System.out.println(ss);
                }
                toProcess.remove(0);
                if (point.x <= 0 || point.x >= image.width() - 1)
                    continue;

                if (point.y <= 0 || point.y >= image.height() - 1)
                    continue;

                if (image.pixel(point.x, point.y) == ref_color)
                    ++clearedPixels;
                image.setPixel(point.x, point.y, clearColor);
                processedPoint.add(new Point(point));
            }

            if (clearedPixels > maxNumOfAreaPixels) { // zdecydowanie za duza plama
                return image.numOfPixels();
            }

            for (Point iter : processedPoint) {
                int p_x = iter.x;
                int p_y = iter.y;
                if (p_x <= 0 || p_x >= image.width() - 1){
                    continue;
                }
                if (p_y <= 0 || p_y >= image.height() - 1){
                    continue;
                }
                byte p_left  = image.pixel(p_x - 1 , p_y);
                byte p_right = image.pixel(p_x + 1,  p_y);
                byte p_up    = image.pixel(p_x,      p_y - 1);
                byte p_down  = image.pixel(p_x,      p_y + 1);

                if (mapImage[p_y][p_x - 1] == false && p_left == ref_color) {
                    toProcess.add(new Point(p_x - 1, p_y));
                    mapImage[p_y][p_x - 1] = true;
                    if (pokazDebugNapis) {
                        String ss = new String();
                        ss = "Added to prcess L - x:" + (p_x - 1) + ", " + "y:" + p_y + " - " + image.pixel(p_x - 1, p_y) + " ";
                        System.out.println(ss);
                    }
                }
                if (mapImage[p_y][p_x + 1] == false && p_right == ref_color) {
                    toProcess.add(new Point(p_x + 1, p_y));
                    mapImage[p_y][p_x + 1] = true;
                    if (pokazDebugNapis) {
                        String ss = new String();
                        ss = "Added to prcess R - x:" + (p_x + 1) + ", " + "y:" + p_y + " - " + image.pixel(p_x + 1, p_y) + " ";
                        System.out.println(ss);
                    }
                }
                if (mapImage[p_y - 1][p_x] == false && p_up == ref_color) {
                    toProcess.add(new Point(p_x, p_y - 1));
                    mapImage[p_y - 1][p_x] = true;
                    if (pokazDebugNapis) {
                        String ss = new String();
                        ss = "Added to prcess U - x:" + p_x + ", " + "y:" + (p_y - 1) + " - " + image.pixel(p_x, p_y - 1) + " ";
                        System.out.println(ss);
                    }
                }
                if (mapImage[p_y + 1][p_x] == false && p_down == ref_color) {
                    toProcess.add(new Point(p_x, p_y + 1));
                    mapImage[p_y + 1][p_x] = true;
                    if (pokazDebugNapis) {
                        String ss = new String();
                        ss = "Added to prcess D - x:" + p_x + ", " + "y:" + (p_y + 1) + " - " + image.pixel(p_x, p_y + 1) + " ";
                        System.out.println(ss);
                    }
                }
            }
        }

        //std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
        //std::cout << "Single clearImage process was running for: "
        //<< std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count()
        //<< "ms.\n";

        return clearedPixels;
    }

     //szukanie okregu wpisanego w obszar
//podajemy obrazek typu
//	00000000000
//	01111000000
//	01000100000
//	01000100000
//	01000010000
//	00100001000
//	00011100100
//	00000011000
//obszar w ktorym ma byc wsadzone koło ograniczony jedynkami
    //void dt(unsigned *_d, unsigned char *_bimg, int _h, int _w)
    /* public static void dt(ArrayList<Integer> _d, byte[] _bimg, int _h, int _w)
    {
        ArrayList<Integer> dd;
        ArrayList<Integer> f;
        ArrayList<Integer> v;
        ArrayList<Integer> z;
        int i;
        int j;
        int k;
        int n;

        dd = new ArrayList<>(_h*_w);
        n = _h>_w ? _h : _w;
        v = new ArrayList<>(n);
        z = new ArrayList<>(n + 1);
        f = new ArrayList<>(_h);

        for (i = 0; i<_h; i++)
        {
            k = -1;
            for (j = 0; j<_w; j++)if (_bimg[i*_w + j] != 0)
            {
                int s;
                s = k<0 ? 0 : (v.get(k) + j >> 1) + 1;
                v.set(++k, new Integer(j));
                z.set(k, new Integer(s));
            }

            if (k<0)
                for (j = 0; j<_w; j++)dd.set(j*_h + i, new Integer(Integer.MAX_VALUE);
            else
            {
                int zk;
                z.set(k + 1, new Integer(_w));
                j = k = 0;
                do
                {
                    int d1;
                    int d2;
                    d1 = j - v.get(k);
                    d2 = d1*d1;
                    d1 = d1 << 1 | 1;
                    zk = z[++k];
                    for (;;)
                    {
                        dd.set(j*_h + i, new Integer(d2));
                        if (++j >= zk)break;
                        d2 += d1;
                        d1 += 2;
                    }
                } while (zk<_w);
            }
        }

        for (j = 0; j<_w; j++)
        {
            int v2 = 0;
            int q2;
            k = -1;
            for (i = q2 = 0; i<_h; i++)
            {
                int d;
                d = dd.get(j*_h + i);
                if (d<Integer.MAX_VALUE)
                {
                    int s;
                    if (k<0)s = 0;
                    else for (;;)
                    {
                        s = q2 - v2 + d - f.get(k);
                        if (s>0)
                        {
                            s = s / (i - v.get(k) << 1) + 1;
                            if (s>z.get(k))break;
                        }
                        else s = 0;
                        if (--k<0)break;
                        v2 = v.get(k) * v.get(k);
                    }
                    if (s<_h) {
                        v.set(++k, new Integer(i));
                        f.set(k, new Integer(d));
                        z.set(k, new Integer(s));
                        v2 = q2;
                    }
                }
                q2 += i << 1 | 1;
            }

            if (k<0)
            {
                //memcpy(_d, dd, _w*_h*sizeof(*_d));
                //_d = dd.clone();
                break;
            }
            else
            {
                int zk;
                z[k + 1] = _h;
                i = k = 0;
                do
                {
                    int d2;
                    int d1;
                    d1 = i - v[k];
                    d2 = d1*d1 + f[k];
                    d1 = d1 << 1 | 1;
                    zk = z[++k];
                    for (;;)
                    {
                        _d[i*_w + j] = (unsigned)d2;
                        if (++i >= zk)break;
                        d2 += d1;
                        d1 += 2;
                    }
                } while (zk<_h);
            }
        }

        delete f;
        delete z;
        delete v;
        delete dd;
    }*/

    //szukanie okregu wpisanego w obszar
//podajemy obrazek typu
//	00000000000
//	01111000000
//	01000100000
//	01000100000
//	01000010000
//	00100001000
//	00011100100
//	00000011000
//obszar w ktorym ma byc wsadzone koło ograniczony jedynkami
    public static void dt(int[]_d, byte[] _bimg, int _h, int _w)
    {
        int[] dd;
        int[] f;
        int[] v;
        int[] z;
        int i;
        int j;
        int k;
        int n;

        dd = new int[_h*_w];
        n = _h>_w ? _h : _w;
        v = new int[n];
        z = new int[n + 1];
        f = new int[_h];

        for (i = 0; i<_h; i++)
        {
            k = -1;
            for (j = 0; j<_w; j++)if (_bimg[i*_w + j] != 0)
            {
                int s;
                s = k<0 ? 0 : (v[k] + j >> 1) + 1;
                v[++k] = j;
                z[k] = s;
            }

            if (k<0)
                for (j = 0; j<_w; j++)dd[j*_h + i] = Integer.MAX_VALUE;
            else
            {
                int zk;
                z[k + 1] = _w;
                j = k = 0;
                do
                {
                    int d1;
                    int d2;
                    d1 = j - v[k];
                    d2 = d1*d1;
                    d1 = d1 << 1 | 1;
                    zk = z[++k];
                    for (;;)
                    {
                        dd[j*_h + i] = d2;
                        if (++j >= zk)break;
                        d2 += d1;
                        d1 += 2;
                    }
                } while (zk<_w);
            }
        }

        for (j = 0; j<_w; j++)
        {
            int v2 = 0;
            int q2;
            k = -1;
            for (i = q2 = 0; i<_h; i++)
            {
                int d;
                d = dd[j*_h + i];
                if (d<Integer.MAX_VALUE)
                {
                    int s;
                    if (k<0)s = 0;
                    else for (;;)
                    {
                        s = q2 - v2 + d - f[k];
                        if (s>0)
                        {
                            s = s / (i - v[k] << 1) + 1;
                            if (s>z[k])break;
                        }
                        else s = 0;
                        if (--k<0)break;
                        v2 = v[k] * v[k];
                    }
                    if (s<_h) {
                        v[++k] = i;
                        f[k] = d;
                        z[k] = s;
                        v2 = q2;
                    }
                }
                q2 += i << 1 | 1;
            }

            if (k<0)
            {
                //memcpy(_d, dd, _w*_h*sizeof(*_d));
                _d = dd.clone();
                break;
            }
            else
            {
                int zk;
                z[k + 1] = _h;
                i = k = 0;
                do
                {
                    int d2;
                    int d1;
                    d1 = i - v[k];
                    d2 = d1*d1 + f[k];
                    d1 = d1 << 1 | 1;
                    zk = z[++k];
                    for (;;)
                    {
                        _d[i*_w + j] = d2;
                        if (++i >= zk)break;
                        d2 += d1;
                        d1 += 2;
                    }
                } while (zk<_h);
            }
        }

        //delete f;
        //delete z;
        //delete v;
        //delete dd;
    }
}
