package com.app.handfeatures;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.opencv.core.Core.absdiff;

/**
 * Created by bijat on 24.05.2016.
 */

class OpenCVHelper {
    public static  Bitmap createBitmap(int width, int height){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        return Bitmap.createBitmap(width, height, conf);
    }

    public static void bitmapToMat(Bitmap inputBitmap, Mat matImage){
        try {
            org.opencv.android.Utils.bitmapToMat(inputBitmap, matImage);
        } catch(Exception e){
            Log.d("Warning", "Bitmap to Mat err: " + e.getMessage());
        }
    }

    public static Bitmap matToBitmap(Mat src, int width, int height){
        Bitmap bmp = createBitmap(width, height);
        org.opencv.android.Utils.matToBitmap(src, bmp);
        return bmp;
    }
}


final class CircleInfo {
    CircleInfo() {
        this.centre = new Point();
        this.radius = 0.0f;
    }

    CircleInfo(Point center, float radious) {
        this.centre = new Point(center);
        this.radius = radious;
    }

    CircleInfo(CircleInfo other) {
        this.centre = new Point(other.centre);
        this.radius = other.radius;
    }

    Point centre;
    float radius;
}

final class Finger
{
    static protected int THUMB = 0;
    static protected int INDEX_FINGER = 1;
    static protected int MIDDLE_FINGER = 2;
    static protected int RING_FINGER = 3;
    static protected int PINKY = 4;
    static protected int NUM_OF_FINGERS = 5;

    Finger() {
        base_first_point = new Point(0, 0);
        base_last_point = new Point(0, 0);
        base_center_point = new FPoint(0.0f, 0.0f);
        top_point = new Point(0, 0);
        circle_top_centre = new Point(0, 0);
        circle_bottom_centre = new Point(0, 0);
    }

    Point base_first_point;			// punkty na obrazie
    Point base_last_point;
    FPoint base_center_point;
    Point top_point;

    int base_first_index;			//indexy punktow na obrazie
    int base_last_index;
    //int base_center_index;
    int top_index;

    float base_length;
    float length;
    int area;
    float distance_from_thumb;
    Point circle_top_centre;
    float circle_top_radius;
    Point circle_bottom_centre;
    float circle_bottom_radius;
};

enum Direction {
    ANY, UP_LEFT, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT;
}

public class HandFeatures {
    // ==================
    // static members
    // ========================
    public static int foundedHandsFeatures = 0;

    final public static int maxAllowedAreas = 60;

    // ===================
    // public functions
    // =========================
    public HandFeatures(Bitmap input, Bitmap background) throws Exception {
        this(); // initialize common

        if (input.getWidth() != background.getWidth() || input.getHeight() != background.getHeight())
            throw new Exception("HandFeatures(Bitmap input, Bitmap backgroung). Rozmiary nie sa identyczne!");

        m_input = input;
        m_background = background;
        m_binarized = null;

        m_image = null; // it should be set in binaryzaion(), or in segmentation()
    }

    HandFeatures(MyImage image) {
        this(); // initialize common

        m_image = new MyImage(image);
    }

    /**
     * it does common thing of all constructors
     */
    private HandFeatures() {
        m_fingers = new Finger[Finger.NUM_OF_FINGERS];
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i)
            m_fingers[i] = new Finger();

        m_conturList = new ArrayList<>();
        //m_maxContourListSize = m_image.height() * 6; // przeniesonie do czesci procceHandFreature() tuz przd wykonianiem konturu

        m_lowerHandWidth = 0;
        m_upperHandWidth = 0;
    }


    /**
     * Process binaryzation on input and backround bitmaps
     * @param threshold - binaryzation factor. If difference is  bigger than threshold the pixel is
     * @param background_color set this value if treshold < difference beetwen input and background pixel
     * @param element_color set this value if treshold >= difference beetwen input and background pixel
     * @return the number of element pixels
     * @throws HandFeaturesException
     */
    public int binaryzation(int threshold, byte background_color, byte element_color) throws HandFeaturesException {
        if (m_input == null || m_background == null)
            throw new HandFeaturesException("binaryzation(). Brak obrazow wejsciowych!");

        int numOfPixels = m_input.getWidth() * m_input.getWidth();
        int numOfElementPixels = 0;

        // pobranie pixeli z bitmapy
        int[] intARGBArray = new int[numOfPixels];
        m_input.getPixels(intARGBArray, 0, m_input.getWidth(), 0, 0, m_input.getWidth(), m_input.getHeight());

        int[] bg_intARGBArray = new int[numOfPixels];
        m_background.getPixels(bg_intARGBArray, 0, m_background.getWidth(), 0, 0, m_background.getWidth(), m_background.getHeight());


        //
        byte[] diff_image = new byte[numOfPixels];
        // int treshold = 5;
        for (int i = 0; i < numOfPixels; ++i) {
            int[] img_chanel = new int[4];
            img_chanel[0] =  (intARGBArray[i] >> 24) & 255;
            img_chanel[1] =  (intARGBArray[i] >> 16) & 255;
            img_chanel[2] =  (intARGBArray[i] >> 8)  & 255;
            img_chanel[3] =  (intARGBArray[i])       & 255;

            int[] bg_img_chanel = new int[4];
            bg_img_chanel[0] =  (bg_intARGBArray[i] >> 24) & 255;
            bg_img_chanel[1] =  (bg_intARGBArray[i] >> 16)  & 255;
            bg_img_chanel[2] =  (bg_intARGBArray[i] >> 8) & 255;
            bg_img_chanel[3] =  (bg_intARGBArray[i]) & 255;

            // odleglosc koluru tla i input
            int diff = (int)Math.sqrt(
                    Math.pow(img_chanel[1] - bg_img_chanel[1], 2) +
                    Math.pow(img_chanel[2] - bg_img_chanel[2], 2) +
                    Math.pow(img_chanel[3] - bg_img_chanel[3], 2));

            if (diff < threshold) {
                diff_image[i] = background_color;
            }
            else {
                diff_image[i] = element_color;
                ++numOfElementPixels;
            }
        }

        m_image = new MyImage(diff_image, m_input.getWidth(), m_input.getHeight());

        return numOfElementPixels;
    }

    public int binarizationOpenCV(int threshold) throws HandFeaturesException {
        if (m_input == null || m_background == null)
            throw new HandFeaturesException("binaryzation(). Brak obrazow wejsciowych!");

        int width = m_input.getWidth();
        int height = m_input.getHeight();

        Mat mat_input = new Mat();
        Mat mat_background = new Mat();
        Mat mat_dff = new Mat(); // roznica obrazow

        OpenCVHelper.bitmapToMat(m_input, mat_input);
        Imgproc.cvtColor(mat_input, mat_input, Imgproc.COLOR_RGBA2RGB);
        OpenCVHelper.bitmapToMat(m_background, mat_background);
        Imgproc.cvtColor(mat_background, mat_background, Imgproc.COLOR_RGBA2RGB);
        //mat_dff = mat_background;
        absdiff(mat_input, mat_background, mat_dff);

        Mat mask = new Mat(height, width, CvType.CV_8U);
        Imgproc.cvtColor(mat_dff, mat_dff, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat_dff, mask, threshold, 255, Imgproc.THRESH_BINARY);
        m_binarized = OpenCVHelper.matToBitmap(mask, width, height);

        m_image = new MyImage(m_binarized);
        return m_image.numOfPixels(Color.EL_COLOR); // number of element pixels
    }

    /**
     * Search and separate the largest area on image
     * @return number of found areas.
     * @throws Exception
     */
    public int segmentation(Rect areaToProcess) throws Exception {
        // nie ustawiono m_image w binaryzation()
        if (m_image == null) {
            // nie ustawiono w odpowiednim konstruktorze obrazu po binaryzacji
            if (m_binarized == null) { // BLAD!
                throw new Exception("segmentation(). Brak zaintializowanych danych!");
            } else {
                // kopiowanie dancyh z zbinoryzawonago brazu do wewnetrzej struktury klasy
                int[] binarized_intARGBArray = new int[m_binarized.getWidth() * m_binarized.getHeight()];
                m_background.getPixels(binarized_intARGBArray, 0, m_binarized.getWidth(), 0, 0, m_binarized.getWidth(), m_binarized.getHeight());

                m_image = new MyImage(binarized_intARGBArray, m_binarized.getWidth(), m_binarized.getHeight());
            }
        }

        if (areaToProcess != null) {
            m_image = m_image.getImage(areaToProcess, Color.BG_COLOR);
        }

        ImageArea area = new ImageArea();
        try {
            // ustawienie border zeby dalej nie sprawdzac warunkow brzegowych pozycji pixeli
            m_image.setBorderColor(Color.BG_COLOR);

            area = m_image.findTheBiggestArea(Color.EL_COLOR);
            //System.out.println("Znaleziono " + area.numOfFoundAreas + " plam.");
            /*if (area.numOfFoundAreas > maxAreas) { // za duzo plam - obraz nie jest odpowieniej jakosci - zadnie pobrania nowego tla
                return area.numOfFoundAreas;
            }*/
            if (area.points.size() < (m_image.width() * m_image.height() * 0.35)) {
                    System.out.println("Nie ma 40%!!!");
                return 0;
            }

            //System.out.println("Plama sklada sie z " + area.points.size() + " pixeli.");
            // znaleziona plama musi pokrywac chociaz 10% obrazu
            if (area.points.size() < (m_image.width() * m_image.height() * 0.05)) {
                System.out.println("Segmentacja - plama pokrywa zbyt maly obszar obrazu(5% wymagane)!!");
                //CameraView.refreshBackground = true;
                return 0;
            }

            // add space for border and 1 pixel space around element for one big backgroud area
            area.rect.left -= 2;
            area.rect.right += 2;
            area.rect.top -= 2;
            area.rect.bottom += 2;

            //MyImage elementImage = m_image.getImage(area.rect, Color.BG_COLOR);
            MyImage elementImage = new MyImage(area);
            elementImage.setBorderColor(Color.EL_COLOR);
            ImageUtils.fillAllArea(elementImage, Color.BG_COLOR, Color.EL_COLOR, m_image.width() * 5);

            // czyszczenie czarnych wglebien(grubosc 1 pixel) w element(dlon)
            elementImage.setBorderColor(Color.EL_COLOR);
            elementImage.smoothEdge(Color.BG_COLOR, Color.EL_COLOR);
            // czyszczenie bialych wypustek z elementu
            elementImage.setBorderColor(Color.BG_COLOR);
            elementImage.smoothEdge(Color.EL_COLOR, Color.BG_COLOR);

            //m_image = new MyImage(m_image.width(), m_image.height());
            //m_image.setImageAtCenter(elementImage);
            m_image = elementImage;
            /*MyImage finalImage = new MyImage(m_image.width(), m_image.height());
            finalImage.setImageAtCenter(elementImage);*/

            /*
             * Koniec usuwania plam z obrazu
             */
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Wyjatek Etap 1!.");
            e.printStackTrace();
        }

        return area.numOfFoundAreas;
    }

    public boolean calculateFeatures() {
        boolean isGood = true;
        try {
            m_image.setBorderColor(Color.BG_COLOR); // dla pewnosic zeby przy szukaniu konturu nie myjsc poza obraz

            m_fingers[Finger.THUMB].top_point = findThumbTop(Color.EL_COLOR);
            m_fingers[Finger.THUMB].top_index = 0;
            m_maxContourListSize = m_image.height() * 8;
            takeContour(m_fingers[Finger.THUMB].top_point);

            // jezeli kontur skladanie z zbyt malej ilosci pixeli prawdopodobnie nie jest to kontur dloni
            if (m_conturList.size() < m_image.height() * 2)
                return false;

            //showNeighbours(m_fingers[Finger.THUMB].top_point); // pokazuje sasiednie pixele czubka kciuka

            //System.out.println("Eureka!. Próba znalezienia cech!");
            findFingersTopAndBase(m_fingers[Finger.THUMB].top_index);
            calculateFingersFetures();
            //System.out.println("Eureka!. Odnalezione ekstrema palcow!");

            // obliczanie wartosci cech dloni
            findOthenHandFeatures();

            //Thread.sleep(1000);
        } catch (HandFeaturesException hfe) {
            System.out.println("calculateFeatures() catch (HandFeaturesException e):" + hfe.getMessage());
            isGood = false;
        } catch (Exception e) {
            System.out.println("calculateFeatures() catch (Exception e):" + e.getMessage());
            e.printStackTrace();
            isGood = false;
        }

        ++HandFeatures.foundedHandsFeatures;
        return isGood;
    }

    public void showHandFeatures(boolean oneLine) {
        if (oneLine) {

        }
        else {
            // dlugosc podstawy palca
            System.out.print("Base: ");
            for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
                System.out.print(String.format("%.2f, ", m_fingers[i].base_length));
            }
            System.out.println("");

            // dlugosci palcow
            System.out.print("Length: ");
            for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
                System.out.print(String.format("%.2f, ", m_fingers[i].length));
            }
            System.out.println("");

            // dlugosci od kciuka do podstaw palcow
            System.out.print("Length from THUM: ");
            for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
                if (i == Finger.THUMB) // dla kciuka nie istnieje sensowna wartosc
                    continue;
                System.out.print(String.format("%.2f, ", m_fingers[i].distance_from_thumb));
            }
            System.out.print(String.format("%.2f ,", m_lowerHandWidth)); // szerokosc dloni
            System.out.println("");


        }
    }

    public HandFeaturesData getHandFeatures() {
        HandFeaturesData data = new HandFeaturesData();
        data.features[FeatureNames.ThumbBaseLength.ordinal()]  = m_fingers[Finger.THUMB].base_length;
        data.features[FeatureNames.IndexBaseLength.ordinal()]  = m_fingers[Finger.INDEX_FINGER].base_length;
        data.features[FeatureNames.MiddleBaseLength.ordinal()] = m_fingers[Finger.MIDDLE_FINGER].base_length;
        data.features[FeatureNames.RingBaseLength.ordinal()]   = m_fingers[Finger.RING_FINGER].base_length;
        data.features[FeatureNames.PinkyBaseLength.ordinal()]  = m_fingers[Finger.PINKY].base_length;

        data.features[FeatureNames.ThumbLength.ordinal()]  = m_fingers[Finger.THUMB].length;
        data.features[FeatureNames.IndexLength.ordinal()]  = m_fingers[Finger.INDEX_FINGER].length;
        data.features[FeatureNames.MiddleLength.ordinal()] = m_fingers[Finger.MIDDLE_FINGER].length;
        data.features[FeatureNames.RingLength.ordinal()]   = m_fingers[Finger.RING_FINGER].length;
        data.features[FeatureNames.PinkyLength.ordinal()]  = m_fingers[Finger.PINKY].length;

        data.features[FeatureNames.IndexDistToThumb.ordinal()]  = m_fingers[Finger.INDEX_FINGER].distance_from_thumb;
        data.features[FeatureNames.MiddleDistToThumb.ordinal()] = m_fingers[Finger.MIDDLE_FINGER].distance_from_thumb;
        data.features[FeatureNames.RingDistToThumb.ordinal()]   = m_fingers[Finger.RING_FINGER].distance_from_thumb;
        data.features[FeatureNames.PinkyDistToThumb.ordinal()]  = m_fingers[Finger.PINKY].distance_from_thumb;
        data.features[FeatureNames.LowerHandWidth.ordinal()]    = m_lowerHandWidth;
        data.features[FeatureNames.UpperHandWidth.ordinal()]    = m_upperHandWidth;

        data.features[FeatureNames.ThumbUpperCircleRadious.ordinal()]  = m_fingers[Finger.THUMB].circle_top_radius;
        data.features[FeatureNames.IndexUpperCircleRadious.ordinal()]  = m_fingers[Finger.INDEX_FINGER].circle_top_radius;
        data.features[FeatureNames.MiddleUpperCircleRadious.ordinal()] = m_fingers[Finger.MIDDLE_FINGER].circle_top_radius;
        data.features[FeatureNames.RingUpperCircleRadious.ordinal()]   = m_fingers[Finger.RING_FINGER].circle_top_radius;
        data.features[FeatureNames.PinkyUpperCircleRadious.ordinal()]  = m_fingers[Finger.PINKY].circle_top_radius;

        data.features[FeatureNames.IndexLowerCircleRadious.ordinal()]  = m_fingers[Finger.INDEX_FINGER].circle_bottom_radius;
        data.features[FeatureNames.MiddleLowerCircleRadious.ordinal()] = m_fingers[Finger.MIDDLE_FINGER].circle_bottom_radius;
        data.features[FeatureNames.RingLowerCircleRadious.ordinal()]   = m_fingers[Finger.RING_FINGER].circle_bottom_radius;
        data.features[FeatureNames.PinkyLowerCircleRadious.ordinal()]  = m_fingers[Finger.PINKY].circle_bottom_radius;

        return data;
    }

    public void drawFeatures() {
        // finger bases
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
            // podstawa placa
            m_image.drawLine(m_fingers[i].base_first_point, m_fingers[i].base_last_point, Color.RED);
            // dlugosc palca
            m_image.drawLine(m_fingers[i].base_center_point.asIntPoint(), m_fingers[i].top_point, Color.BLUE);

            if (i != Finger.THUMB) {
                // od kciuka do podstawy palca
                m_image.drawLine(m_fingers[Finger.THUMB].base_last_point, m_fingers[i].base_center_point.asIntPoint(), Color.MAGENTA);
                // dolny okrag
                m_image.drawCircle(m_fingers[i].circle_bottom_centre, (int)m_fingers[i].circle_bottom_radius, Color.GREEN);
            }

            m_image.drawCircle(m_fingers[i].top_point, 5, Color.RED); // czubek
            m_image.drawCircle(m_fingers[i].base_first_point, 5, Color.RED); // lewa podstawa
            m_image.drawCircle(m_fingers[i].base_last_point, 5, Color.RED); // prawa podstawa
            m_image.drawCircle(m_fingers[i].circle_top_centre, (int)m_fingers[i].circle_top_radius, Color.GREEN_LIGHT); // gorny okrag
        }
        // dolna szerokosc dloni
        m_image.drawLine(m_fingers[Finger.THUMB].base_last_point, m_lowerRightHandWidthPoint, Color.BLUE_LIGHT);
        // gorna szerokosc dloni
        m_image.drawLine(m_fingers[Finger.INDEX_FINGER].base_first_point, m_fingers[Finger.PINKY].base_last_point, Color.BLUE_LIGHT);
        // prawy punkt dolnej szerokosci dloni
        m_image.drawCircle(m_lowerRightHandWidthPoint, 5, Color.RED);
    }

    public Bitmap getConturBitmap(boolean drawFeatures) {
        MyImage contourImage = new MyImage(m_image.width(), m_image.height());
        for (Point p : m_conturList)
            contourImage.setPixel(p.x, p.y, Color.EL_COLOR);

        Bitmap bmp = Bitmap.createBitmap(contourImage.width(), contourImage.height(), Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(IntBuffer.wrap(contourImage.getRawImageDataAsInt()));
        return bmp;
    }

    // ===================
    // private functions
    // =========================
    private void takeContour(Point startPixel) throws Exception {
        byte targetColor = m_image.pixel(startPixel.x, startPixel.y);

        Point foundPixel = new Point(startPixel);
        Direction dir = Direction.ANY;
        m_conturList.add(new Point(foundPixel)); // pixel rozpoczynajacy jako pierwszy
        do {
            //case UP_LEFT:
            if (dir != Direction.DOWN_RIGHT && m_image.pixel(foundPixel.x - 1, foundPixel.y) != targetColor
                    && m_image.pixel(foundPixel.x - 1, foundPixel.y - 1) == targetColor) {
                m_conturList.add(new Point(foundPixel.x - 1, foundPixel.y - 1));
                dir = Direction.UP_LEFT;
            }
            // case UP:
            else if (dir != Direction.DOWN && m_image.pixel(foundPixel.x - 1, foundPixel.y - 1) != targetColor
                    && m_image.pixel(foundPixel.x, foundPixel.y - 1) == targetColor) {
                m_conturList.add(new Point(foundPixel.x, foundPixel.y - 1));
                dir = Direction.UP;
            }
            //case UP_RIGHT:
            else if (dir != Direction.DOWN_LEFT && m_image.pixel(foundPixel.x, foundPixel.y - 1) != targetColor
                    && m_image.pixel(foundPixel.x + 1, foundPixel.y - 1) == targetColor) {
                m_conturList.add(new Point(foundPixel.x + 1, foundPixel.y - 1));
                dir = Direction.UP_RIGHT;
            }
            //case RIGHT:
            else if (dir != Direction.LEFT && m_image.pixel(foundPixel.x + 1, foundPixel.y - 1) != targetColor
                    && m_image.pixel(foundPixel.x + 1, foundPixel.y) == targetColor) {
                m_conturList.add(new Point(foundPixel.x + 1, foundPixel.y));
                dir = Direction.RIGHT;
            }
            //case DOWN_RIGHT:
            else if (dir != Direction.UP_LEFT && m_image.pixel(foundPixel.x + 1, foundPixel.y) != targetColor
                    && m_image.pixel(foundPixel.x + 1, foundPixel.y + 1) == targetColor) {
                m_conturList.add(new Point(foundPixel.x + 1, foundPixel.y + 1));
                dir = Direction.DOWN_RIGHT;
            }
            //case DOWN:
            else if (dir != Direction.UP && m_image.pixel(foundPixel.x + 1, foundPixel.y + 1) != targetColor
                    && m_image.pixel(foundPixel.x, foundPixel.y + 1) == targetColor) {
                m_conturList.add(new Point(foundPixel.x, foundPixel.y + 1));
                dir = Direction.DOWN;
            }
            //case DOWN_LEFT:
            else if (dir != Direction.UP_RIGHT && m_image.pixel(foundPixel.x, foundPixel.y + 1) != targetColor
                    && m_image.pixel(foundPixel.x - 1, foundPixel.y + 1) == targetColor) {
                m_conturList.add(new Point(foundPixel.x - 1, foundPixel.y + 1));
                dir = Direction.DOWN_LEFT;
            }
            //case LEFT:
            else if (dir != Direction.RIGHT && m_image.pixel(foundPixel.x - 1, foundPixel.y + 1) != targetColor
                    && m_image.pixel(foundPixel.x - 1, foundPixel.y) == targetColor) {
                m_conturList.add(new Point(foundPixel.x - 1, foundPixel.y));
                dir = Direction.LEFT;
            }
            // nie znaleziono nastepnego pixela konturu - kontur nie jest zamkniety
            else
                throw new HandFeaturesException("Kontur nie został zamkniety. Nie znaleziono kolejnego pixela!");

            // zapamietanie ostatnio znalezionego pixela
            foundPixel = m_conturList.get(m_conturList.size() - 1);
            if (m_conturList.size() > m_maxContourListSize)
                throw new HandFeaturesException("Contour List is too much! Podczas tworzenie kontutu utknieto w wystajacym fragmecie elementu."
                        + "Prawdobodonie element nie zostal poparwnie wygladzony");
            //showNeighbours(foundPixel);
            //System.out.print("DIR: " + dir.name());

        } while(!foundPixel.equals(startPixel.x, startPixel.y));

        // 1'szy i ostatni punkt sa takie same, usuwamy ostatni
        m_conturList.remove(m_conturList.size() - 1);
    }


    private void calculateFingersFetures() throws HandFeaturesException {
        // srodek podstawy palcow i dlugosc podstawy palca
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
            float x = (m_fingers[i].base_first_point.x + m_fingers[i].base_last_point.x) / 2.0f;
            float y = (m_fingers[i].base_first_point.y + m_fingers[i].base_last_point.y) / 2.0f;
            m_fingers[i].base_center_point = new FPoint(x, y);
            float len = Utils.distance(m_fingers[i].base_first_point, m_fingers[i].base_last_point);
            m_fingers[i].base_length = len;
        }

        // dlugosci palcow
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
            float len = Utils.distance(m_fingers[i].base_center_point.asIntPoint(), m_fingers[i].top_point);
            m_fingers[i].length = len;
        }

        Point thumb_base_last = m_fingers[Finger.THUMB].base_last_point;
        // dlugosci od kciuka do podstaw palcow
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
            if (i == Finger.THUMB) // dla kciuka nie liczymy tej wartosci
                continue;
            float len = Utils.distance(thumb_base_last, m_fingers[i].base_center_point.asIntPoint());
            m_fingers[i].distance_from_thumb = len;
        }

        // znajduje punk po prawej stronie dloni w poziomie od punktu thumb_base_last.y
        Point p = m_conturList.get(findContourPixelY(m_fingers[Finger.PINKY].top_index, m_conturList.size(), m_fingers[Finger.THUMB].base_last_point.y));
        m_lowerHandWidth = p.x - m_fingers[Finger.THUMB].base_last_point.x;
        m_lowerRightHandWidthPoint = new Point(p);
        if (m_lowerHandWidth < 0)
            throw new HandFeaturesException("findOthenHandFeatures(). Szerokosc dolna dloni nie moze być ujemna!");

        // znajduje szerokosc dloni od podstawy palsa wskazujacego do podstawy malego
        m_upperHandWidth = m_fingers[Finger.PINKY].base_last_point.x - m_fingers[Finger.INDEX_FINGER].base_first_point.x;
        if (m_upperHandWidth < 0)
            throw new HandFeaturesException("findOthenHandFeatures(). Szerokosc gorna dloni nie moze być ujemna!");


    }


    private Point findThumbTop(byte color) throws Exception {
        for (int x = 0; x < m_image.width() / 2; ++x) {
            for (int y = m_image.height() / 3; y < m_image.height(); ++y) {
                if (m_image.pixel(x, y) == color)
                    return new Point(x, y);
            }
        }

        throw new HandFeaturesException("findThumbTop(). Nie znaleziono w lewe czesci obrazu zadnego pixela!");
    }

    private void findFingersTopAndBase(int thumb_top_index) throws Exception {
        int findSizeRange = m_conturList.size() / 6;
        int rangeSize = 10;
        int index;

        // szukanie punktu przebiecie miedzy top thumb a top index finger
        index = findTopExtremum(thumb_top_index, thumb_top_index + findSizeRange, rangeSize);
        index = findBottomExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.THUMB].base_last_index = index;
        m_fingers[Finger.THUMB].base_last_point = m_conturList.get(m_fingers[Finger.THUMB].base_last_index);
        //System.out.println("Odnalezione base last thumb(" + m_fingers[Finger.THUMB].base_last_point.x + "," + m_fingers[Finger.THUMB].base_last_point.y +"). Index: " + index);;

        // pierwsza podstawa kciuka
        int thumb_base_index = findThumbBaseFirst(m_fingers[Finger.THUMB].base_last_index);
        m_fingers[Finger.THUMB].base_first_index = thumb_base_index;
        m_fingers[Finger.THUMB].base_first_point = m_conturList.get(m_fingers[Finger.THUMB].base_first_index);
        //System.out.println("Odnalezione base first thumb(" + m_fingers[Finger.THUMB].base_first_point.x + "," + m_fingers[Finger.THUMB].base_first_point.y +"). Index: " + thumb_base_index);

        // szukanie top index finger
        index = findTopExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.INDEX_FINGER].top_index = index;
        m_fingers[Finger.INDEX_FINGER].top_point = m_conturList.get(m_fingers[Finger.INDEX_FINGER].top_index);
        //System.out.println("Odnalezione top index finger(" + m_fingers[Finger.INDEX_FINGER].top_point.x + "," + m_fingers[Finger.INDEX_FINGER].top_point.y + "). Index: " + index);

        // podstawa wspolna index and middle finger
        index = findBottomExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.INDEX_FINGER].base_last_index = index;
        m_fingers[Finger.INDEX_FINGER].base_last_point = m_conturList.get(m_fingers[Finger.INDEX_FINGER].base_last_index);
        m_fingers[Finger.MIDDLE_FINGER].base_first_index = index;
        m_fingers[Finger.MIDDLE_FINGER].base_first_point = m_conturList.get(m_fingers[Finger.MIDDLE_FINGER].base_first_index);
        //System.out.println("Odnalezione base lase index | first base middle finger(" + m_fingers[Finger.INDEX_FINGER].base_last_point.x + "," + m_fingers[Finger.INDEX_FINGER].base_last_point.y +"). Index: " + index);


        int index_base_first_index = findIndexBaseFirst(m_fingers[Finger.INDEX_FINGER].top_index, m_fingers[Finger.THUMB].base_last_index, m_fingers[Finger.INDEX_FINGER].base_last_index);
        m_fingers[Finger.INDEX_FINGER].base_first_index = index_base_first_index;
        m_fingers[Finger.INDEX_FINGER].base_first_point = m_conturList.get(m_fingers[Finger.INDEX_FINGER].base_first_index);
        //System.out.println("Odnalezione base first index finger(" + m_fingers[Finger.INDEX_FINGER].base_first_point.x + "," + m_fingers[Finger.INDEX_FINGER].base_first_point.y +"). Index: " + index_base_first_index);


        // top middle finger
        index = findTopExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.MIDDLE_FINGER].top_index = index;
        m_fingers[Finger.MIDDLE_FINGER].top_point = m_conturList.get(m_fingers[Finger.MIDDLE_FINGER].top_index);
        //System.out.println("Odnalezione top middle finger(" + m_fingers[Finger.MIDDLE_FINGER].top_point.x + "," + m_fingers[Finger.MIDDLE_FINGER].top_point.y +")");


        // podstawa wspolna middle and ring finger
        index = findBottomExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.MIDDLE_FINGER].base_last_index = index;
        m_fingers[Finger.MIDDLE_FINGER].base_last_point = m_conturList.get(m_fingers[Finger.MIDDLE_FINGER].base_last_index);
        m_fingers[Finger.RING_FINGER].base_first_index = index;
        m_fingers[Finger.RING_FINGER].base_first_point = m_conturList.get(m_fingers[Finger.RING_FINGER].base_first_index);
        //System.out.println("Odnalezione base last middle finger(" + m_fingers[Finger.MIDDLE_FINGER].base_last_point.x + "," + m_fingers[Finger.MIDDLE_FINGER].base_last_point.y +")");


        // top ring finger
        index = findTopExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.RING_FINGER].top_index = index;
        m_fingers[Finger.RING_FINGER].top_point = m_conturList.get(m_fingers[Finger.RING_FINGER].top_index);
        //System.out.println("Odnalezione top ring finger(" + m_fingers[Finger.RING_FINGER].top_point.x + "," + m_fingers[Finger.RING_FINGER].top_point.y +")");


        // podstawa wspolna ring and PINKY finger
        index = findBottomExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.RING_FINGER].base_last_index = index;
        m_fingers[Finger.RING_FINGER].base_last_point = m_conturList.get(m_fingers[Finger.RING_FINGER].base_last_index);
        m_fingers[Finger.PINKY].base_first_index = index;
        m_fingers[Finger.PINKY].base_first_point = m_conturList.get(m_fingers[Finger.PINKY].base_first_index);
        //System.out.println("Odnalezione base last ring finger(" + m_fingers[Finger.RING_FINGER].base_last_point.x + "," + m_fingers[Finger.RING_FINGER].base_last_point.y +")");


        // top pinky finger
        index = findTopExtremum(index, index + findSizeRange, rangeSize);
        m_fingers[Finger.PINKY].top_index = index;
        m_fingers[Finger.PINKY].top_point = m_conturList.get(m_fingers[Finger.PINKY].top_index);
        //System.out.println("Odnalezione top pinky finger(" + m_fingers[Finger.PINKY].top_point.x + "," + m_fingers[Finger.PINKY].top_point.y +")");


        // podstawa zewnetrzna pinky finger base last
        index = findPinkyBaseLast(m_fingers[Finger.PINKY].top_index, m_fingers[Finger.PINKY].base_first_index);
        m_fingers[Finger.PINKY].base_last_index = index;
        m_fingers[Finger.PINKY].base_last_point = m_conturList.get(m_fingers[Finger.PINKY].base_last_index);
        //System.out.println("Odnalezione base last pinky finger(" + m_fingers[Finger.PINKY].base_last_point.x + "," + m_fingers[Finger.PINKY].base_last_point.y +")");

    }

    private void findOthenHandFeatures() throws Exception {
        // z ilu pixeli sklada sie palec
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
            // odciecie palcy linia aby stanowily one osobna powierzchnie, bo mozna bylo policzyc ich pixele
            m_image.drawLine(m_fingers[i].base_first_point, m_fingers[i].base_last_point, Color.BG_COLOR);
            Point startFillPoint = new Point(m_fingers[i].base_center_point.asIntPoint());
            if (i == Finger.THUMB) { // dla kciuka przeson punk w lewo
                startFillPoint.y -= 3; // wyzej o 3 pixele
                startFillPoint.x -= 3; // w lewo
            }
            else
                startFillPoint.y -= 3; // wyzej o 3 pixele
            int num = m_image.fillArea(startFillPoint, Color.GREY_DARK, Color.BG_COLOR);
            m_fingers[i].area = num;
        }

        Point baseFingerForThumbCircle = new Point();
        baseFingerForThumbCircle.x = (int) ((m_fingers[Finger.THUMB].base_center_point.x * 5 + m_fingers[Finger.THUMB].top_point.x) / 6);
        baseFingerForThumbCircle.y = (int) ((m_fingers[Finger.THUMB].base_center_point.y * 5 + m_fingers[Finger.THUMB].top_point.y) / 6);
        CircleInfo thumbCircleInfo = findMaxCircle(
                m_fingers[Finger.THUMB].top_point,
                baseFingerForThumbCircle, //m_fingers[Finger.THUMB].base_center_point.asIntPoint(),
                m_conturList.size() - 2,
                m_fingers[Finger.THUMB].base_first_index,
                m_fingers[Finger.THUMB].top_index + 1,
                m_fingers[Finger.THUMB].base_last_index);
        m_fingers[Finger.THUMB].circle_top_centre = thumbCircleInfo.centre;
        m_fingers[Finger.THUMB].circle_top_radius = thumbCircleInfo.radius;
        //System.out.println("Finger(" + Finger.THUMB + ") circle(" + thumbCircleInfo.centre.x + ", " + thumbCircleInfo.centre.x + ") r: " + String.format("%.2f", thumbCircleInfo.radius));

        // gorne i dolne okregi palcow
        for (int i = 0; i < Finger.NUM_OF_FINGERS; ++i) {
            if (i == Finger.THUMB) //dla kcuka wprowadzane sa specjalnie dla niego inne parametry do obliczen
                continue;

            // gorny okrag palca
            int leftEndIndex = (m_fingers[i].base_first_index + m_fingers[i].top_index) / 2;
            int rightEndIndex = (m_fingers[i].base_last_index + m_fingers[i].top_index) / 2;
            Point baseFingerForUpperCircle = new Point();
            baseFingerForUpperCircle.x = (int) ((m_fingers[i].base_center_point.x + m_fingers[i].top_point.x) / 2);
            baseFingerForUpperCircle.y = (int) ((m_fingers[i].base_center_point.y + m_fingers[i].top_point.y) / 2);
            CircleInfo upperCircleInfo = findMaxCircle(
                    m_fingers[i].top_point,
                    baseFingerForUpperCircle,//m_fingers[i].base_center_point.asIntPoint(),
                    m_fingers[i].top_index - 1,
                    leftEndIndex,
                    m_fingers[i].top_index + 1,
                    rightEndIndex);

            m_fingers[i].circle_top_centre = upperCircleInfo.centre;
            m_fingers[i].circle_top_radius = upperCircleInfo.radius;
            //System.out.println("Finger(" + i + ") circle(" + upperCircleInfo.centre.x + ", " + upperCircleInfo.centre.x + ") r: " + String.format("%.2f", upperCircleInfo.radius));

            // dolny okrag palca
            int leftStartIndex = leftEndIndex;
            int rightStartIndex = rightEndIndex;
            Point baseFingerForLowerCircle = new Point();
            baseFingerForLowerCircle.x = (int) ((m_fingers[i].base_center_point.x * 3 + m_fingers[i].top_point.x) / 4);
            baseFingerForLowerCircle.y = (int) ((m_fingers[i].base_center_point.y * 3 + m_fingers[i].top_point.y) / 4);
            CircleInfo lowerCircleInfo = findMaxCircle(
                    baseFingerForUpperCircle,
                    baseFingerForLowerCircle,
                    leftStartIndex,
                    m_fingers[i].base_first_index,
                    rightStartIndex,
                    m_fingers[i].base_last_index);

            m_fingers[i].circle_bottom_centre = lowerCircleInfo.centre;
            m_fingers[i].circle_bottom_radius = lowerCircleInfo.radius;
            //System.out.println("Finger(" + i + ") circle(" + lowerCircleInfo.centre.x + ", " + lowerCircleInfo.centre.x + ") r: " + String.format("%.2f", lowerCircleInfo.radius));

        }
    }

    private CircleInfo findMaxCircle(Point top, Point base, int startLeftIndex, int endLeftIndex, int startRightIndex, int endRightIndex) throws HandFeaturesException {
        int maxLeftIndex = -1;
        int maxRightIndex = -1;
        float maxDist = -1.0f;

        // zmienne pomocnicze
        int d, dx, dy, ai, bi, xi, yi;
        int x = base.x, y = base.y;
        int leftIndex;
        int rightIndex;
        float tempDist;

        // ustalenie kierunku rysowania
        if (base.x < top.x)
        {
            xi = 1;
            dx = top.x - base.x;
        }
        else
        {
            xi = -1;
            dx = base.x - top.x;
        }
        // ustalenie kierunku rysowania
        if (base.y < top.y)
        {
            yi = 1;
            dy = top.y - base.y;
        }
        else
        {
            yi = -1;
            dy = base.y - top.y;
        }
        // pierwszy piksel
        // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
        try {
            leftIndex = findClosesContourIndex(top, new Point(x, y), endLeftIndex, startLeftIndex);
            rightIndex = findClosesContourIndex(top, new Point(x, y), startRightIndex, endRightIndex);
            tempDist = distance(leftIndex, rightIndex);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                maxLeftIndex = leftIndex;
                maxRightIndex = rightIndex;
            }
        } catch (HandFeaturesException e) {
        }

        // oś wiodąca OX
        if (dx > dy)
        {
            ai = (dy - dx) * 2;
            bi = dy * 2;
            d = bi - dx;
            // pętla po kolejnych x
            while (x != top.x)
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
                // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
                try {
                    leftIndex = findClosesContourIndex(top, new Point(x, y), endLeftIndex, startLeftIndex);
                    rightIndex = findClosesContourIndex(top, new Point(x, y), startRightIndex, endRightIndex);
                    tempDist = distance(leftIndex, rightIndex);
                    if (tempDist > maxDist) {
                        maxDist = tempDist;
                        maxLeftIndex = leftIndex;
                        maxRightIndex = rightIndex;
                    }
                } catch (HandFeaturesException e) {
                }
            }
        }
        // oś wiodąca OY
        else
        {
            ai = ( dx - dy ) * 2;
            bi = dx * 2;
            d = bi - dy;
            // pętla po kolejnych y
            while (y != top.y)
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
                // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
                // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
                try {
                    leftIndex = findClosesContourIndex(top, new Point(x, y), endLeftIndex, startLeftIndex);
                    rightIndex = findClosesContourIndex(top, new Point(x, y), startRightIndex, endRightIndex);
                    tempDist = distance(leftIndex, rightIndex);
                    if (tempDist > maxDist) {
                        maxDist = tempDist;
                        maxLeftIndex = leftIndex;
                        maxRightIndex = rightIndex;
                    }
                } catch (HandFeaturesException e) {
                }
            }
        }

        if (maxLeftIndex == -1 || maxRightIndex == -1 || maxDist < 1.0f)
            throw new HandFeaturesException("fintMaxCircle(). Nie znaleziono okregu!");

        CircleInfo info = new CircleInfo();
        info.centre.x = (m_conturList.get(maxLeftIndex).x + m_conturList.get(maxRightIndex).x) / 2;
        info.centre.y = (m_conturList.get(maxLeftIndex).y + m_conturList.get(maxRightIndex).y) / 2;
        info.radius = Utils.distance(info.centre, m_conturList.get(maxLeftIndex));
        return info;
    }

    private float distance(int index1, int index2) {
        Point p1 = m_conturList.get(index1);
        Point p2 = m_conturList.get(index2);
        return (float)Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    /*private int findClosesContourIndex(Point top, Point base, int startIndex, int endIndex) throws HandFeaturesException {//, int rightTopIndex, int rightBaseIndex) {
        int xdif = base.x - top.x;
        int ydif = base.y - top.y;
        FPoint lp = new FPoint(base.x - ydif / 2, base.y + xdif);
        FPoint rp = new FPoint(base.x + ydif / 2, base.y - xdif);

        //m_image.drawLine(lp.asIntPoint(), rp.asIntPoint(), Color.RED);

        float dx = (rp.x - lp.x);
        float dy = (rp.y - lp.y);
        float a = dx / dy;
        float b = 1.0f;
        float c = -(lp.y - (a * lp.x));

        float minDist = Float.MAX_VALUE;
        int index = -1;
        for (int i = startIndex; i < endIndex; ++i) {
            float tempDist = Utils.distance(new FPoint(m_conturList.get(i)), a, b, c);
            if (tempDist < minDist) {
                minDist = tempDist;
                index = i;
            }
        }

        if (index == -1 || minDist > 2.0f) {
            throw new HandFeaturesException("findClosesContourIndex(). Nie znaleziono bliskiego pixela!. Index("+index+"), minDist("+minDist+")");
        }

        return index;
    }*/

    // 0-_---->parrarelToTopBase
    // |\/-angle
    // | \
    // |  0 midToPoint
    // |
    private int findClosesContourIndex(Point top, Point base, int startIndex, int endIndex) throws HandFeaturesException {//, int rightTopIndex, int rightBaseIndex) {
        if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = startIndex;
        }

        int xdif = base.x - top.x;
        int ydif = base.y - top.y;
        // dwa punkty prostopadla do top - base
        FPoint lp = new FPoint(base.x - ydif / 2, base.y + xdif);
        FPoint rp = new FPoint(base.x + ydif / 2, base.y - xdif);
        FPoint mid = new FPoint((lp.x + rp.x) / 2, (lp.y + rp.y) / 2);

        FPoint parallelToBaseTop = new FPoint(rp.x - mid.x, rp.y - mid.y);
        //m_image.drawLine(lp.asIntPoint(), rp.asIntPoint(), Color.RED);

        float minAndle = Float.MAX_VALUE;
        int index = -1;
        for (int i = startIndex; i < endIndex; ++i) {
            FPoint midToPoint = new FPoint(m_conturList.get(i).x - mid.x, m_conturList.get(i).y - mid.y);
            float tempAngle = Utils.angle(parallelToBaseTop, midToPoint);
            if (tempAngle > Utils.PI_2)
                tempAngle = Utils.PI - tempAngle;
            if (tempAngle < minAndle) {
                minAndle = tempAngle;
                index = i;
            }
        }

        if (index == -1 || minAndle > 0.03f) {
            throw new HandFeaturesException("findClosesContourIndex(). Nie znaleziono odpowiednio bliskiego pixela!. Index("+index+"), minAmgle("+minAndle+")");
        }

        return index;
    }

    private int findMostLeft(int startIndex, int endIndex) {
        int x = Integer.MAX_VALUE;
        for (int i = startIndex + 1; i < endIndex; ++i) {
            if (m_conturList.get(i).x < x)
                x = m_conturList.get(i).x;
        }

        return x;
    }

    private int findMostRight(int startIndex, int endIndex) {
        int x = -1;
        for (int i = startIndex + 1; i < endIndex; ++i) {
            if (m_conturList.get(i).x > x)
                x = m_conturList.get(i).x;
        }

        return x;
    }

    private int findMostBottom(int startIndex, int endIndex) {
        int y = -1;
        for (int i = startIndex + 1; i < endIndex; ++i) {
            if (m_conturList.get(i).y > y)
                y = m_conturList.get(i).y;
        }

        return y;
    }

    private int findMostTop(int startIndex, int endIndex) {
        int y = Integer.MAX_VALUE;
        for (int i = startIndex + 1; i < endIndex; ++i) {
            if (m_conturList.get(i).y < y)
                y = m_conturList.get(i).y;
        }

        return y;
    }


    /*
    private CircleInfo findMaxCircle(FPoint base_finger, Point top_finger, int startLeft, int startRight, int endLeft, int endRight) throws HandFeaturesException {
        int leftIndex = startLeft;
        int rightIndex = startRight;

        int maxLeftIndex = startLeft;
        int maxRightIndex = startRight;
        float maxCircleRadius = 0.0f;

        float factor_a = Utils.parrarelLineEquationFacotrA(base_finger.asIntPoint(), top_finger);

        for (int i = startLeft; i > endLeft; --i) {
            FPoint lineEquation = Utils.findLineEquation(factor_a, m_conturList.get(i));

            // znajdze pixel lezacy bardzo blisko wyznaczonej linji
            float distToLine = Float.MAX_VALUE / 2;
            int counter = 0;
            for (int j = startRight; j < endRight; ++j) {
                float tempDistToLine = Utils.DistancePointToLine(m_conturList.get(j), lineEquation.x, lineEquation.y);
                if (tempDistToLine > 5.0f) continue;
                if (tempDistToLine < distToLine) {
                    distToLine = tempDistToLine;
                    leftIndex = i;
                    rightIndex = j;
                    counter = 0; // zlenaziono punk lezacy blizej
                }

                ++counter;
                // jezeli dla kilku pixeli nie znaleziono lezacego blizej pixeca - przerwij petle - japrawdopodobniej juz znaleziono ten najbliszy
                if (counter > 5)
                    break;
            }

            float tempRadousDist = Utils.distance(m_conturList.get(leftIndex), m_conturList.get(rightIndex));
            if (tempRadousDist > maxCircleRadius) {
                maxCircleRadius = tempRadousDist;
                maxLeftIndex = leftIndex;
                maxRightIndex = rightIndex;
            }
        }

        if (maxLeftIndex == startLeft || maxRightIndex == startRight || maxCircleRadius == 0.0f)
            throw new HandFeaturesException("findMaxCircle(). Blad!");

        Point centre = new Point();
        centre.x = (m_conturList.get(maxLeftIndex).x + m_conturList.get(maxRightIndex).x) / 2;
        centre.y = (m_conturList.get(maxLeftIndex).y + m_conturList.get(maxRightIndex).y) / 2;
        return new CircleInfo(centre, maxCircleRadius / 2.0f);
    }
     */

    /*private CircleInfo findMaxCircle(Point base_finger, Point top_finger, int startLeft, int startRight, int endLeft, int endRight) throws HandFeaturesException {
        int leftIndex = -1;
        int rightIndex = -1;
        int maxLeftIndex = -1;
        int maxRightIndex = -1;
        LineEquationFactors parallelLef;
        float maxDist = 0.0f;

        // zmienne pomocnicze
        int d, dx, dy, ai, bi, xi, yi;
        int x = base_finger.x, y = base_finger.y;

        LineEquationFactors lefBaseToTop = new LineEquationFactors(base_finger, top_finger);

        // ustalenie kierunku rysowania
        if (base_finger.x < top_finger.x)
        {
            xi = 1;
            dx = top_finger.x - base_finger.x;
        }
        else
        {
            xi = -1;
            dx = base_finger.x - top_finger.x;
        }
        // ustalenie kierunku rysowania
        if (base_finger.y < top_finger.y)
        {
            yi = 1;
            dy = top_finger.y - base_finger.y;
        }
        else
        {
            yi = -1;
            dy = base_finger.y - top_finger.y;
        }
        // pierwszy piksel
        // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
        parallelLef = new LineEquationFactors(new FPoint(x, y), new FPoint(x + lefBaseToTop.a, y + lefBaseToTop.b));
        try {
            //if (leftIndex != -1 && m_conturList.get(leftIndex).y < base_finger.y)
            leftIndex = findClosestContourPointToLine(startLeft, endLeft, parallelLef);
            rightIndex = findClosestContourPointToLine(startRight, endRight, parallelLef);
            float tempDist = Utils.distance(m_conturList.get(leftIndex), m_conturList.get(rightIndex));
            if (tempDist > maxDist) {
                maxDist = tempDist;
                maxLeftIndex = leftIndex;
                maxRightIndex = rightIndex;
            }
        } catch(HandFeaturesException e) {}

        // oś wiodąca OX
        if (dx > dy)
        {
            ai = (dy - dx) * 2;
            bi = dy * 2;
            d = bi - dx;
            // pętla po kolejnych x
            while (x != top_finger.x)
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
                // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
                parallelLef = new LineEquationFactors(new FPoint(x, y), new FPoint(x + lefBaseToTop.a, y + lefBaseToTop.b));
                try {
                    leftIndex = findClosestContourPointToLine(startLeft, endLeft, parallelLef);
                    rightIndex = findClosestContourPointToLine(startRight, endRight, parallelLef);
                    float tempDist = Utils.distance(m_conturList.get(leftIndex), m_conturList.get(rightIndex));
                    if (tempDist > maxDist) {
                        maxDist = tempDist;
                        maxLeftIndex = leftIndex;
                        maxRightIndex = rightIndex;
                    }
                } catch(HandFeaturesException e) {}
            }
        }
        // oś wiodąca OY
        else
        {
            ai = ( dx - dy ) * 2;
            bi = dx * 2;
            d = bi - dy;
            // pętla po kolejnych y
            while (y != top_finger.y)
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
                // szukamy lini prostopadlej do kierunku palca (lefBaseToTop)
                parallelLef = new LineEquationFactors(new FPoint(x, y), new FPoint(x + lefBaseToTop.a, y + lefBaseToTop.b));
                try {
                    leftIndex = findClosestContourPointToLine(startLeft, endLeft, parallelLef);
                    rightIndex = findClosestContourPointToLine(startRight, endRight, parallelLef);
                    float tempDist = Utils.distance(m_conturList.get(leftIndex), m_conturList.get(rightIndex));
                    if (tempDist > maxDist) {
                        maxDist = tempDist;
                        maxLeftIndex = leftIndex;
                        maxRightIndex = rightIndex;
                    }
                } catch(HandFeaturesException e) {}
            }
        }

        if (maxLeftIndex == -1 || maxRightIndex == -1 || maxDist == 0.0f)
            throw new HandFeaturesException("fintMaxCircle(). Nie znaleziono okregu!");

        CircleInfo info = new CircleInfo();
        info.centre.x = (m_conturList.get(maxLeftIndex).x + m_conturList.get(maxRightIndex).x) / 2;
        info.centre.y = (m_conturList.get(maxLeftIndex).y + m_conturList.get(maxRightIndex).y) / 2;
        info.radius = Utils.distance(info.centre, m_conturList.get(maxLeftIndex));
        return info;
    }*/

    private int findClosestContourPointToLine(int startIndex, int endIndex, LineEquationFactors lef) throws HandFeaturesException {
        int leftIndex = (startIndex < endIndex ? startIndex : endIndex);
        int rightIndex = (startIndex < endIndex ? endIndex : startIndex);
        int midIndex = -1;

        float midDist;
        float leftDist;
        float rightDist;
        while (Math.abs(leftIndex - rightIndex) > 1) {
            midIndex = (leftIndex + rightIndex) / 2;
            leftDist = Utils.distance(m_conturList.get(leftIndex), lef);
            midDist = Utils.distance(m_conturList.get(midIndex), lef);
            rightDist = Utils.distance(m_conturList.get(rightIndex), lef);
            if (rightDist > leftDist)
                rightIndex = midIndex;
            else
                leftIndex = midIndex;
        }

        if (midIndex == -1)
            throw new HandFeaturesException("findClosestContourPointToLine(" + startIndex + ", " + endIndex + ", " + lef + "). Nie znaleziono punktu na konurze!");

        return midIndex;
    }

    private int findThumbBaseFirst(int thumb_base_last_index) throws Exception {
        int index = 0;
        double minDist = Double.MAX_VALUE / 2.0;

        double tempDist;
        for (int i = m_conturList.size() - 1; i > (m_conturList.size() * (7.0/8.0)); --i) {
            tempDist = Utils.distance(m_conturList.get(thumb_base_last_index), m_conturList.get(i));
            if (tempDist < minDist) {
                minDist = tempDist;
                index = i;
            }

            if (tempDist - minDist > 5.0) // znaleziono juz minDist i zaczelismy sie oddalac
                break;
        }

        if (index == 0)
            throw new HandFeaturesException("findThumbBaseFirst(). Nie znaleziono thumb_base_first!");

        return index;
    }

    private int findIndexBaseFirst(int index_top_index, int thumb_base_last_index, int index_base_last_index) throws Exception {
        int index = index_top_index;
        double minDist = Double.MAX_VALUE / 2.0;

        double tempDist;
        for (int i = index_top_index - 1; i > thumb_base_last_index; --i) { // od top index do thumb base last
            tempDist = Utils.distance(m_conturList.get(index_base_last_index), m_conturList.get(i));
            if (tempDist < minDist) {
                minDist = tempDist;
                index = i;
            }
        }

        if (index == 0)
            throw new HandFeaturesException("findIndexBaseFirst(). Nie znaleziono index_base_first!");

        return index;
    }

    private int findPinkyBaseLast(int pinky_top_index, int pinky_base_first_index) throws Exception {
        int index = pinky_top_index;
        double minDist = Double.MAX_VALUE;

        double tempDist;
        for (int i = pinky_top_index; i < m_conturList.size() && i < pinky_top_index + (m_conturList.size() / 8); ++i) {
            tempDist = Utils.distance(m_conturList.get(pinky_base_first_index), m_conturList.get(i));
            if (tempDist < minDist) {
                minDist = tempDist;
                index = i;
            }

            if (tempDist - minDist > 5.0) // znaleziono juz minDist i zaczelismy sie oddalac
                break;
        }

        if (index == pinky_top_index)
            throw new HandFeaturesException("findPinkyBaseLast(). Nie znaleziono pinky_base_last!");

        return index;
    }

    private int findTopExtremum(int startIndex, int endIndex, int serieSize) throws Exception {
        int counterSeriePixels = 0;
        int index = startIndex; // tu przechowamy znaleziony index

        for (int i = startIndex; i < endIndex; i = i + serieSize) {
            //showContourList(i, 6);
            counterSeriePixels = 0;
            for (int j = 0; j < serieSize; ++j)
                if (m_conturList.get(i).y < m_conturList.get(i + j + 1).y) ++counterSeriePixels;

            // i = odpowiada dokladnie pixelowi za ktorym nastepne zaczynaja byc coraz nizej
            if (counterSeriePixels == serieSize) {
                // znaleziona dolna lewa czesc palca
                index = i;
                break;
            }
            // jezeli znalezionu jakies pixele ktore sa poniezej to spr. dokladnie gdzie jest poczatek
            else if (counterSeriePixels > 0 && counterSeriePixels < serieSize) {
                for (int k = i + 1; k < i + serieSize - 1; ++k) {
                    counterSeriePixels = 0;
                    for (int j = 0; j < serieSize; ++j)
                        if (m_conturList.get(k).y < m_conturList.get(k + j + 1).y) ++counterSeriePixels;

                    if (counterSeriePixels == serieSize) {
                        // znaleziona dolna lewa czesc palca
                        index = k;
                        break; //  znaleziono poczatek palca
                    }
                }
            }

            if (index != startIndex) { // znaleziono index przegiacia po ktorym kilka pixeli jest coraz nizej
                break;
            }
        } // for (int i = startIndex; i < endIndex; i = i + serieSize)

        if (index == startIndex)
            throw new HandFeaturesException("findTopExtremum(). Nie udało sie znalesc extremum!");

        return index;
    }

    private int findBottomExtremum(int startIndex, int endIndex, int serieSize) throws Exception {
        int counterSeriePixels = 0;
        int index = startIndex; // tu przechowamy znaleziony index

        for (int i = startIndex; i < endIndex; i = i + serieSize) {
            //showContourList(i, 6);
            counterSeriePixels = 0;
            for (int j = 0; j < serieSize; ++j)
                if (m_conturList.get(i).y > m_conturList.get(i + j + 1).y) ++counterSeriePixels;

            // i = odpowiada dokladnie pixelowi za ktorym nastepne zaczynaja byc coraz nizej
            if (counterSeriePixels == serieSize) {
                // znaleziona dolna lewa czesc palca
                index = i;
                break;
            }
            // jezeli znalezionu jakies pixele ktore sa poniezej to spr. dokladnie gdzie jest poczatek
            else if (counterSeriePixels > 0 && counterSeriePixels < serieSize) {
                for (int k = i + 1; k < i + serieSize - 1; ++k) {
                    counterSeriePixels = 0;
                    for (int j = 0; j < serieSize; ++j)
                        if (m_conturList.get(k).y > m_conturList.get(k + j + 1).y) ++counterSeriePixels;

                    if (counterSeriePixels == serieSize) {
                        // znaleziona dolna lewa czesc palca
                        index = k;
                        break; //  znaleziono poczatek palca
                    }
                }
            }

            if (index != startIndex) { // znaleziono index przegiacia po ktorym kilka pixeli jest coraz nizej
                break;
            }
        } // for (int i = startIndex; i < endIndex; i = i + serieSize)

        if (index == startIndex)
            throw new HandFeaturesException("findBottomExtremum(). Nie udało sie znalesc extremum!");

        return index;
    }

    private int findContourPixelY(int startIndex, int endIndex, int y_val) throws HandFeaturesException {
        for (int i = startIndex; i < endIndex; ++i) {
            if (m_conturList.get(i).y == y_val)
                return i;
        }

        throw new HandFeaturesException("findContourPixelY() nie znaleziono pixelna o wartosvi Y = " + y_val + " w przzedziale " + startIndex + " - " + endIndex);
    }

    // fun. only for debugging process
    private void showContourList(int startIndex, int endIndex) {
        int dy = Math.abs(m_conturList.get(startIndex).y - m_conturList.get(endIndex).y);
        for (int i = 1; i < endIndex - startIndex; ++i)
            if (Math.abs(m_conturList.get(startIndex).y - m_conturList.get(endIndex).y) > dy)
                dy = Math.abs(m_conturList.get(startIndex).y - m_conturList.get(endIndex).y);

        dy = dy * 2 + 2;
        int dx = endIndex - startIndex;

        byte[][] a = new byte[dy+1][(endIndex - startIndex) * 2];
        for (int i = startIndex; i < endIndex; ++i)
            a[m_conturList.get(i).y - m_conturList.get(startIndex).y][m_conturList.get(i).x - m_conturList.get(startIndex).x + dx] = 1;

        for (byte[] b : a){
            for (byte c : b)
                System.out.print(c);
            System.out.println("");
        }
    }

    private void showNeighbours(Point p) {
        System.out.println("Sasiedzi dla punktu: " + p);

        for (int y = p.y - 2; y < p.y + 2 + 1; ++y) {
            for (int x = p.x - 2; x < p.x + 2 + 1; ++x) {
                if (x == p.x && y == p.y)
                    System.out.print(String.format("%3s", "X"));
                else
                    System.out.print(String.format("%3d", m_image.pixel(x, y)));
            }
            System.out.println("");
        }
    }

    //
    // Getters
    //

    public MyImage getImage() { return m_image; }
    public Finger[] getFingers() { return m_fingers; }
    public float getLowerHandWidth() { return m_lowerHandWidth; }
    public float getUpperHandWidth() { return m_upperHandWidth; }
    public Bitmap getProcessed(boolean drawFeatures) {
        if (drawFeatures) {
            drawFeatures(); // rysuje znalezione cechy na wewnetrzej reprezentacji obrazu
        }

        int w = m_image.width();
        int h = m_image.height();

        // tworzymi bitmape zeby mozna bylu zapisac tu obraz
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // type
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);

        int[] diff_image = new int[w * h];

        // przekopiowanie danych z koncowego obrazu do danych tablicy pixeli ARBG(int[])
        for (int y = 1; y < h - 1; ++y) {
            for (int x = 1; x < w - 1; ++x) {
                byte color = m_image.pixel(x, y);
                diff_image[y * m_image.width() + x] = Color.colorToABGR(color);
            }
        }

        // przekopiowanie danych pixeli do bitmapy
        bmp.copyPixelsFromBuffer(IntBuffer.wrap(diff_image));

        return bmp;
    }

    // ===================
    // members
    // =========================
    private Finger[] m_fingers;

    // references to obiect from outside, needed for binaryzation
    private Bitmap m_input; // it's also need for shearch the largest area
    private Bitmap m_background;
    private Bitmap m_binarized; // used if the binaryzation process was done outside the class

    private MyImage m_image;
    private ArrayList<Point> m_conturList;
    private int m_maxContourListSize;

    // szerokosc dloni
    int m_lowerHandWidth;
    int m_upperHandWidth;
    Point m_lowerRightHandWidthPoint;
}
