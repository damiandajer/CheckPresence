package com.app.handfeatures;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by bijat on 26.05.2016.
 */

public class MyImage {
    public MyImage(Bitmap bmp/*, Bitmap bmpi, Bitmap bmpb*/) {
        m_height = bmp.getHeight();
        m_width = bmp.getWidth();
        m_numOfPixels = m_width * m_height;

        int[] data = new int[m_numOfPixels];
        bmp.getPixels(data, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
/*
        int[] datai = new int[m_numOfPixels];
        bmpi.getPixels(datai, 0, bmp.getWidth(), 0, 0, bmpi.getWidth(), bmpi.getHeight());

        int[] datab = new int[m_numOfPixels];
        bmpb.getPixels(datab, 0, bmp.getWidth(), 0, 0, bmpb.getWidth(), bmpb.getHeight());
*/
        allocate();

        int a = 0;
        int b = 0;
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                int pixel_argb = data[y*m_width + x];
                //int pixel_argbi = datai[y*m_width + x];
                //int pixel_argbb = datab[y*m_width + x];
                int[] img_chanel = new int[4];
                //int[] img_chaneli = new int[4];
                //int[] img_chanelb = new int[4];
                img_chanel[0] =  (pixel_argb >> 24) & 255;
                img_chanel[1] =  (pixel_argb >> 16) & 255;
                img_chanel[2] =  (pixel_argb >> 8)  & 255;
                img_chanel[3] =  (pixel_argb)       & 255;
                /*img_chaneli[0] =  (pixel_argbi >> 24) & 255;
                img_chaneli[1] =  (pixel_argbi >> 16) & 255;
                img_chaneli[2] =  (pixel_argbi >> 8)  & 255;
                img_chaneli[3] =  (pixel_argbi)       & 255;
                img_chanelb[0] =  (pixel_argbb >> 24) & 255;
                img_chanelb[1] =  (pixel_argbb >> 16) & 255;
                img_chanelb[2] =  (pixel_argbb >> 8)  & 255;
                img_chanelb[3] =  (pixel_argbb)       & 255;*/
                if (img_chanel[1] == 0) {
                    m_image[y][x] = Color.BG_COLOR;
                    ++b;
                }
                else {
                    m_image[y][x] = Color.EL_COLOR;
                    ++a;
                }
            }
        }
        System.out.println("EL: "+ a + ", BG: " + b + ", ALL: " + (m_height * m_width));
    }

    public MyImage(int[] data, int width, int height) {
        m_height = height;
        m_width = width;
        m_numOfPixels = m_width * m_height;
        allocate();

        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                if (data[y*m_width + x] == 0)
                    m_image[y][x] = Color.BG_COLOR;
                else
                    m_image[y][x] = Color.EL_COLOR;
            }
        }
    }

    public MyImage(byte[] data, int width, int height) {
        m_height = height;
        m_width = width;
        m_numOfPixels = m_width * m_height;
        allocate();

        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                if (data[y*m_width + x] == 0)
                    m_image[y][x] = Color.BG_COLOR;
                else
                    m_image[y][x] = Color.EL_COLOR;
            }
        }
    }

    public MyImage(int width, int height) {
        m_height = height;
        m_width = width;
        m_numOfPixels = m_width * m_height;
        allocate();

        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                    m_image[y][x] = Color.BG_COLOR;
            }
        }
    }

    public MyImage(MyImage other) {
        this.copy(other);
    }

    public void copyWithSignedDiff(MyImage other) {
        MyImage temp = new MyImage(other.m_width, other.m_height);

        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_height; ++x) {
                if (m_image[y][x] != other.m_image[y][x])
                    if (other.m_image[y][x] == Color.BG_COLOR)
                        temp.m_image[y][x] = Color.BG_COLOR_AREA;
                    else
                        temp.m_image[y][x] = Color.EL_COLOR_AREA;
                else
                    temp.m_image[y][x] = other.m_image[y][x];
            }
        }

        this.copy(temp);
    }
    public void copyWithRemoveDiff(MyImage other) {
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_height; ++x) {
                if (other.m_image[y][x] != Color.BG_COLOR && other.m_image[y][x] != Color.EL_COLOR) {
                    if (other.m_image[y][x] == Color.BG_COLOR_AREA)
                        m_image[y][x] = Color.BG_COLOR;
                    else
                        m_image[y][x] = Color.EL_COLOR;
                }
                else
                    m_image[y][x] = other.m_image[y][x];
            }
        }
    }

    public void copy(MyImage other) {
        if (m_height != other.m_height && m_width != other.m_width) {
            m_height = other.m_height;
            m_width = other.m_width;
            m_numOfPixels = m_width * m_height;
            allocate();
        }

        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_height; ++x) {
                m_image[y][x] = other.m_image[y][x];
            }
        }
    }

    public MyImage getImage(Rect rect, byte colorFillOutBoundary) {
        MyImage temp = new MyImage(rect.width(), rect.height());
        for (int y = 0; y < rect.height(); ++y) {
            for (int x = 0; x < rect.width(); ++x) {
                if ((x + rect.left) < 0 || (x + rect.left) > m_width - 1 || (y + rect.top) < 0 || (y + rect.top) > m_height - 1)
                    temp.setPixel(x, y, colorFillOutBoundary);
                else
                    temp.setPixel(x, y, m_image[y + rect.top][x + rect.left]);
            }
        }

        return temp;
    }

    public byte[] getRawImageData() {
        byte[] rawData = new byte[m_width*m_height];
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                rawData[y * m_width + x] = m_image[y][x];
            }
        }
        return rawData;
    }

    public int[] getRawImageDataAsInt() {
        int[] rawData = new int[m_width * m_height];

        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                if (m_image[y][x] == 0)
                    rawData[y * m_width + x] = 0x00000000;
                else
                    rawData[y * m_width + x] = 0xFFFFFFFF;
            }
        }

        return rawData;
    }

    public void setImageAtCenter(MyImage image) throws Exception {
        int offsetX = (m_width - image.width()) / 2;
        int offsetY = (m_height - image.height()) / 2;
        for (int y = 0; y < image.height(); ++y) {
            for (int x = 0; x < image.width(); ++x) {
                int sX = x + offsetX;
                int sY = y + offsetY;
                if (sX < 0 || sX > m_width - 1 || sY < 0 || sY > m_height - 1)
                    continue;
                else if (image.pixel(x, y) != 0)
                    m_image[sY][sX] = image.pixel(x, y);
                else
                    m_image[sY][sX] = image.pixel(x, y);
            }
        }

        //System.out.println("X("+x+") Y("+y+") w("+image.width()+") h("+image.height()+")");
        //Thread.sleep(500);
        /*Rect rect = new Rect();
        rect.left    = (m_width - image.width()) / 2;
        rect.right   = rect.left + image.width();
        rect.top     = (m_height - image.height()) / 2;
        rect.bottom  = rect.top + image.height();
        for (int y = rect.left; y < m_height && y < rect.bottom; ++y) {
            for (int x = rect.left; x < m_width && x < rect.right; ++x) {
                if ((x + rect.left) < 0 || (x + rect.left) > m_width - 1 || (y + rect.top) < 0 || (y + rect.top) > m_height - 1)
                    continue;
                else
                    m_image[y][x] = image.pixel(x - rect.left, y - rect.top);
            }
        }*/

        /*for (int y = ; y < m_height && y < image.height() + dy; ++y) {
            for ( ; x < m_width && x < image.width() + dx; ++x) {
                m_image[y][x] = image.pixel(x - dx, y - dy);
            }
        }*/
    }

    public ImageArea findTheBiggestArea(byte ref_color) throws Exception {
        boolean pokazDebugNapis = false;

        // w mapImage zapisujemy ktore pixelze sotału juz 'odwiedzone'
        boolean[][] mapImage = new boolean[m_height][m_width];
        for (int iter_y = 0; iter_y < m_height; ++iter_y) {
            for (int iter_x = 0; iter_x < m_width; ++iter_x) {
                mapImage[iter_y][iter_x] = false;
            }
        }

        // lista pozycji pixeli dla poprzedniej 'plamy' i aktualnej
        ArrayList<Point> toProcess = new ArrayList<>();
        ImageArea area = new ImageArea();

        // dla kazdego pixela w obrazia sproboj znales poczatek 'plamy'
        for (int y = 1; y < m_height - 1; ++y) {
            for (int x = 1; x < m_width - 1; ++x) {
                if (mapImage[y][x] == false && pixel(x, y) == ref_color) { // znalezione poczatek 'plamy'
                    // dodajemy 1'szy pixel do przetwarzanie, od niego zaczyna sie 'rozrost'(szukanie nastepnych)
                    toProcess.add(new Point(x, y));
                    mapImage[y][x] = true;
                    int startFrom = 0; // index ostatniege pixela z poprzedniego przebiegu

                    // dla pixeli znalezionych w ostatnim przebiegu
                    for (int i = startFrom; i < toProcess.size(); ++i) {
                        startFrom = toProcess.size();
                        int p_x = toProcess.get(i).x;
                        int p_y = toProcess.get(i).y;
                        if (p_x <= 0 || p_x >= m_width - 1){
                            System.out.println("if (p_x <= 0 || p_x >= image.width() - 1){");
                            continue;
                        }
                        if (p_y <= 0 || p_y >= m_height - 1){
                            System.out.println("if (p_y <= 0 || p_y >= image.height() - 1){");
                            continue;
                        }

                        // pixele otaczajace
                        boolean p_left  = mapImage[p_y][p_x - 1] == false && pixel(p_x - 1, p_y)     == ref_color;
                        boolean p_right = mapImage[p_y][p_x + 1] == false && pixel(p_x + 1, p_y)     == ref_color;
                        boolean p_up    = mapImage[p_y - 1][p_x] == false && pixel(p_x,     p_y - 1) == ref_color;
                        boolean p_down  = mapImage[p_y + 1][p_x] == false && pixel(p_x,     p_y + 1) == ref_color;
                        //boolean addedPixel = false; // czy dodano jakis nowy pixel
                        // jezeli nie odwiedzono jeszcze pixela i ma kolor plamki
                        if (p_left) {
                            toProcess.add(new Point(p_x - 1, p_y));
                            mapImage[p_y][p_x - 1] = true;
                            //addedPixel = true;
                        }
                        if (p_right) {
                            toProcess.add(new Point(p_x + 1, p_y));
                            mapImage[p_y][p_x + 1] = true;
                            //addedPixel = true;
                        }
                        if (p_up) {
                            toProcess.add(new Point(p_x, p_y - 1));
                            mapImage[p_y - 1][p_x] = true;
                            //addedPixel = true;
                        }
                        if (p_down) {
                            toProcess.add(new Point(p_x, p_y + 1));
                            mapImage[p_y + 1][p_x] = true;
                            //addedPixel = true;
                        }
                    } // for (int i = startFrom; i < toProcess.size(); ++i)

                    // dodatkowe dane o procesie znajdowanie najwiekszej powierzchni
                    ++area.numOfFoundAreas;
                    area.numOfFoundAllElementPixels += toProcess.size();

                    // obecna plamka jest wieksza niz poprzednia
                    if (toProcess.size() > area.points.size()) {
                        area.points = toProcess;
                        toProcess = new ArrayList<>();
                    }
                } //if (image.pixel(x, y) == ref_color)
            }
        }

        if (area.points.size() > 0) {
            area.rect.left = area.rect.right = area.points.get(0).x;
            area.rect.top = area.rect.bottom = area.points.get(0).y;
            for (Point p : area.points) {
                if (p.x < area.rect.left)    area.rect.left    = p.x;
                if (p.x > area.rect.right)   area.rect.right   = p.x;
                if (p.y < area.rect.top)     area.rect.top     = p.y;
                if (p.y > area.rect.bottom)  area.rect.bottom  = p.y;
            }

            // by rozmiar odpowiadal wymiarom obrazow, np. width wskazuje ZA ostatni pixel itd.
            ++area.rect.right;
            ++area.rect.bottom;
        }

        return area;
    }

    public void show(int noMoreThan) {
        for (int x = 0; x < m_width && x < noMoreThan; ++x)
            System.out.print("----");
        System.out.println("");

        for (int y = 0; y < m_height && y < noMoreThan; ++y) {
            String ss = new String();
            if (y < 10)
                ss = " ";
            ss = ss + y + "| ";
            for (int x = 0; x < m_width && x < noMoreThan; ++x) {
                ss = ss + m_image[y][x] + " ";
                if (m_image[y][x] >= 0 && m_image[y][x] < 10)
                    ss = ss + "  ";
                else if (m_image[y][x] < 0 || m_image[y][x] < 100)
                    ss = ss + " ";
            }
            System.out.println(ss);
        }
    }

    public int width() { return m_width; }
    public int height() { return m_height; }
    public int numOfPixels() { return m_numOfPixels; }
    public byte pixel(int x, int y) {
        //if (!((x >= 0 && x < m_width) || (y >= 0 && y < m_height)))
        //throw new Exception("MyImage.pixel(int x, int y): x(" + x + "), y(" + y + ")\n" + reportStatus());
        return m_image[y][x];
    }
    public byte pixel(Point p) {
        return m_image[p.y][p.x];
    }

    private String reportStatus() {
        String s = new String();
        s = "MyImage. m_width(" + m_width + "), m_height(" + m_height + ")";
        return s;
    }

    void setArea(ImageArea area, byte color) {
        for (Point p : area.points) {
            m_image[p.y][p.x] = color;
        }
    }

    /**
     * return number of filled pixels
     */
    public int fillArea(Point start, byte fillColor, byte borderColor) throws Exception {
        int start_x = start.x;
        int start_y = start.y;
        if (m_image[start_y][start_x] == borderColor)
            return 0;

        boolean[][] mapImage = new boolean[m_height][m_width];
        ArrayList<Point> toProcess = new ArrayList<>();

        toProcess.add(new Point(start_x, start_y));
        m_image[start_y][start_x] = fillColor;
        mapImage[start_x][start_y] = true;

        int startFrom = 0; // index ostatniege pixela z poprzedniego przebiegu

        // dla pixeli znalezionych w ostatnim przebiegu
        for (int i = startFrom; i < toProcess.size(); ++i) {
            startFrom = toProcess.size();
            int p_x = toProcess.get(i).x;
            int p_y = toProcess.get(i).y;
            if (p_x <= 0 || p_x >= m_width - 1){
                System.out.println("if (p_x <= 0 || p_x >= image.width() - 1){");
                throw new Exception("fillArea(). p_x <= 0 || p_x >= m_width - 1.");
            }
            if (p_y <= 0 || p_y >= m_height - 1){
                System.out.println("if (p_y <= 0 || p_y >= image.height() - 1){");
                throw new Exception("fillArea(). p_y <= 0 || p_y >= m_height - 1.");
            }

            // pixele otaczajace
            boolean p_left  = mapImage[p_y][p_x - 1] == false && pixel(p_x - 1, p_y)     != borderColor;
            boolean p_right = mapImage[p_y][p_x + 1] == false && pixel(p_x + 1, p_y)     != borderColor;
            boolean p_up    = mapImage[p_y - 1][p_x] == false && pixel(p_x,     p_y - 1) != borderColor;
            boolean p_down  = mapImage[p_y + 1][p_x] == false && pixel(p_x,     p_y + 1) != borderColor;
            //boolean addedPixel = false; // czy dodano jakis nowy pixel
            // jezeli nie odwiedzono jeszcze pixela i ma kolor plamki
            if (p_left) {
                toProcess.add(new Point(p_x - 1, p_y));
                m_image[p_y][p_x - 1] = fillColor;
                mapImage[p_y][p_x - 1] = true;
                //addedPixel = true;
            }
            if (p_right) {
                toProcess.add(new Point(p_x + 1, p_y));
                m_image[p_y][p_x + 1] = fillColor;
                mapImage[p_y][p_x + 1] = true;
                //addedPixel = true;
            }
            if (p_up) {
                toProcess.add(new Point(p_x, p_y - 1));
                m_image[p_y - 1][p_x] = fillColor;
                mapImage[p_y - 1][p_x] = true;
                //addedPixel = true;
            }
            if (p_down) {
                toProcess.add(new Point(p_x, p_y + 1));
                m_image[p_y + 1][p_x] = fillColor;
                mapImage[p_y + 1][p_x] = true;
                //addedPixel = true;
            }
        } // for (int i = startFrom; i < toProcess.size(); ++i)

        return toProcess.size();
    }

    public void drawLine(Point start, Point end, byte color) {
        // zmienne pomocnicze
        int d, dx, dy, ai, bi, xi, yi;
        int x = start.x, y = start.y;
        // ustalenie kierunku rysowania
        if (start.x < end.x)
        {
            xi = 1;
            dx = end.x - start.x;
        }
        else
        {
            xi = -1;
            dx = start.x - end.x;
        }
        // ustalenie kierunku rysowania
        if (start.y < end.y)
        {
            yi = 1;
            dy = end.y - start.y;
        }
        else
        {
            yi = -1;
            dy = start.y - end.y;
        }
        // pierwszy piksel
        setPixel(x, y, color);
        // oś wiodąca OX
        if (dx > dy)
        {
            ai = (dy - dx) * 2;
            bi = dy * 2;
            d = bi - dx;
            // pętla po kolejnych x
            while (x != end.x)
            {
                // test współczynnika
                if (d >= 0)
                {
                    x += xi;
                    y += yi;
                    d += ai;
                }
                else
                {
                    d += bi;
                    x += xi;
                }
                setPixel(x, y, color);
            }
        }
        // oś wiodąca OY
        else
        {
            ai = ( dx - dy ) * 2;
            bi = dx * 2;
            d = bi - dy;
            // pętla po kolejnych y
            while (y != end.y)
            {
                // test współczynnika
                if (d >= 0)
                {
                    x += xi;
                    y += yi;
                    d += ai;
                }
                else
                {
                    d += bi;
                    y += yi;
                }
                setPixel(x, y, color);
            }
        }
    }

    void drawCircle(Point p, int r, byte color)
    {
        int px = p.x;
        int py = p.y;

        int x, y, d;

        int x_max = (int)(r / Math.sqrt(2.0)) + 1;

        if (py - r < 0 || py + r > m_height - 1 || px - r < 0 || px + r > m_width - 1) return;

        y = r;
        d = -r;
        m_image[r + py][0 + px] = m_image[py - r][0 + px] = m_image[py][px + r] = m_image[py][px - r] = color;

        int i = 0;
        for (x = 1; x<x_max; x++)
        {
            d += 2 * x - 1;
            if (d >= 0) { y--; d -= 2 * y; }
            m_image[py + y][px + x] = m_image[py + y][px - x] = m_image[py - y][px + x] = m_image[py - y][px - x] = m_image[py + x][px + y] = m_image[py - x][px + y] = m_image[py - x][px - y] = m_image[py + x][px - y] = color;
        }
    }

    public void setPixel(int x, int y, byte color) {
        //byte temp = m_image[y][x];
        m_image[y][x] = color;
        //return temp;
    }

    public void setPixel(Point p, byte color) {
        //byte temp = m_image[y][x];
        m_image[p.y][p.x] = color;
        //return temp;
    }

    public void setBorderColor(byte color) {
        for (int y = 0; y < m_height; ++y) {
            m_image[y][0] = color;
            m_image[y][m_width - 1] = color;
        }
        for (int x = 0; x < m_width; ++x) {
            m_image[0][x] = color;
            m_image[m_height - 1][x] = color;
        }
    }

    public void smoothEdge(byte colorToSmooth, byte clearToColor) throws Exception {
        for (int y = 1; y < m_height - 1; ++y) {
            for (int x = 1; x < m_width - 1; ++x) {
                if (m_image[y][x] == colorToSmooth) {
                    int ileSasiadow = ImageUtils.howManyNeighbours(this, x, y, colorToSmooth);
                    if (ileSasiadow < 4) {
                        m_image[y][x] = clearToColor;
                        smoothNeighbourPixels(new Point(x, y), colorToSmooth, clearToColor);
                    }
                }
            }
        }
    }

    /*public void smoothEdgeHard(byte colorToSmooth, byte clearToColor) throws Exception {
        for (int y = 2; y < m_height - 2; ++y) {
            for (int x = 2; x < m_width - 2; ++x) {
                if (m_image[y][x] == colorToSmooth) {
                    int ileSasiadow = ImageUtils.howManyNeighbours(this, x, y, colorToSmooth, 2);
                    if (ileSasiadow >= 17) {
                        m_image[y][x] = clearToColor;
                        for (int sy = y - 1; sy < y + 1 + 1; ++sy) {
                            for (int sx = x - 1; sy < x + 1 + 1; ++sx) {
                                if (sx == x && sy == y) continue;
                                if (pixel(x, y) == colorToSmooth && !(ImageUtils.howManyNeighbours(this, x, y, colorToSmooth, 2) >= 17)) m_image[y][x] = clearToColor;;

                            }
                        }
                    }
                }
            }
        }
    }*/

    public void smoothNeighbourPixels(Point p, byte colorToSmooth, byte clearToColor) throws Exception {
        for (int y = p.y - 1; y < p.y + 1 + 1; ++y) {
            for (int x = p.x - 1; x < p.x + 1 + 1; ++x) {
                if (m_image[y][x] == colorToSmooth && ImageUtils.howManyNeighbours(this, x, y, colorToSmooth) < 4) {
                    m_image[y][x] = clearToColor;
                    smoothNeighbourPixels(new Point(x, y), colorToSmooth, clearToColor);
                }
            }
        }
    }

    /*public void smoothNeighbourPixels2(Point p, byte colorToSmooth, byte clearToColor) throws Exception {
        int startY = p.y - 2;
        if (startY < 0) startY = 0;
        int startX = p.x - 2;
        if (startX < 0) startX = 0;
        int endY = p.y + 2 + 1;
        if (endY > m_height) endY = m_height;
        int endX = p.x + 2 + 1;
        if (endX > m_width) endX = m_width;
        for (int y = startY; y < endY; ++y) {
            for (int x =startX; x < endX; ++x) {
                if (!(m_image[y][x] == colorToSmooth && ImageUtils.howManyNeighbours(this, x, y, colorToSmooth, 2) >= 17)) {
                    m_image[y][x] = clearToColor;
                    //smoothNeighbourPixels2(new Point(x, y), colorToSmooth, clearToColor);
                }
            }
        }
    }*/

    /**
     * Set border inside image
     * @param rect
     * @param color
     * @param offset - minus value = bigger border size
     */
    public void setBorder(Rect rect, byte color, int offset) {
        for (int y = rect.top + offset; y < rect.bottom - offset; ++y) {
            m_image[y][rect.left + offset] = color;
            m_image[y][rect.right - 1 - offset] = color;
        }
        for (int x = rect.left + offset; x < rect.right - offset; ++x) {
            m_image[rect.top + offset][x] = color;
            m_image[rect.bottom - 1 - offset][x] = color;
        }
    }

    public int numOfPixels(byte color) {
        int c = 0;
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                if (m_image[y][x] == color) ++c;
            }
        }

        return c;
    }


    private void allocate() {
        m_image = new byte[m_height][m_width];
    }
    private void release() {

    }

    private byte[][] m_image;
    private int m_width;
    private int m_height;
    private int m_numOfPixels;
};
