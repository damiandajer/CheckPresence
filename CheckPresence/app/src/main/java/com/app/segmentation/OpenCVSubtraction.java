package com.app.segmentation;

import android.graphics.Bitmap;

import com.app.checkpresence.CameraView;
import com.app.handfeatures.Color;
import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesData;
import com.app.handfeatures.HandFeaturesException;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Damian on 17.05.2016.
 */
public class OpenCVSubtraction implements Runnable {
    private volatile Bitmap resultBitmap;
    Bitmap inputBitmap, backgroundBitmap;
    int height, width, threshold;
    Mat imgToProcess1, imgToProcess2, imgToProcess, mask;
    int[] intARGBArray;
    float[] handFeatures;
    private native int[] deleteSmallAreas(int[] intARGBArray, int height, int width);
    private HandFeaturesData handFeaturesData = null;


    /**
     *
     * @param inputBitmap Bitmap to process
     * @param backgroundBitmap Bitmap with background
     */
    public OpenCVSubtraction(Bitmap inputBitmap, Bitmap backgroundBitmap, int threshold) {
        this.inputBitmap = inputBitmap;
        this.backgroundBitmap = backgroundBitmap;
        resultBitmap = inputBitmap;

        this.height = inputBitmap.getHeight();
        this.width = inputBitmap.getWidth();
        this.threshold = threshold;

        this.imgToProcess = new Mat(height, width, CvType.CV_8UC4);
        this.imgToProcess1 = new Mat(height, width, CvType.CV_8UC4);
        this.imgToProcess2 = new Mat(height, width, CvType.CV_8UC4);
        this.mask = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void run() {
        boolean useOpenCV = true;
        if (useOpenCV)
            System.out.println("Thread is processing frame with OpenCV");
        else
            System.out.println("Thread is processing frame withOUT OpenCV");

        HandFeatures handFeatures = null;
        int numberOfElementPixels = 0;
        try {
            handFeatures = new HandFeatures(inputBitmap, backgroundBitmap);
            //
            // ETAP 1 - przygotowanie obazu do natepnego etapu
            //
            if (useOpenCV) { // Z OPENCV
                numberOfElementPixels = handFeatures.binarizationOpenCV(25);
            }
            else { // BEZ OPENCV
                numberOfElementPixels = handFeatures.binaryzation(45, Color.BG_COLOR, Color.EL_COLOR);
            }

            // ---------------
            // RESZTA KODU JEST TAKA SAMA NIEZALEZNIE OD PROCESU BINARYZACJI
            // ------------------------

            boolean tooFewElementPixels = false;
            // jezeli obraz ma mniej niz 10% pixeli koloru elementu nie przetwarzaj dalej - najprowodpodobniej nie ma reki na obrazie
            if (numberOfElementPixels < (handFeatures.getImage().width() * handFeatures.getImage().height()) * 0.10) {
                System.out.println("Binaryzacja - znaleziono za malo pixeli elementu");
                //CameraView.refreshBackground = true;
                //resultBitmap = handFeatures.getProcessed(false);
                //return;
                tooFewElementPixels = true;
            }
            // jezeli obraz ma wiecej niz 60% pixeli koloru elementu nie przetwarzaj dalej - najprowodpodobniej nie ma reki na obrazie
            if (numberOfElementPixels > (handFeatures.getImage().width() * handFeatures.getImage().height()) * 0.70) {
                System.out.println("Binaryzacja - znaleziono za duzo pixeli elementu");
                resultBitmap = handFeatures.getProcessed(false);
                CameraView.refreshBackground = true;
                return;
            }

            // jezeli np plama dloni jest za mala, lub jakis blad przy przetwarzaniu to zwruc aktualny wyglad obrazu i zakoncz przetwarzanie tej klatki
            // czyscimy pijedyncze kropki
            handFeatures.getImage().setBorderColor(Color.BG_COLOR);
            handFeatures.getImage().smoothEdge(Color.EL_COLOR, Color.BG_COLOR);
            // segmentacja
            int foundAreas = handFeatures.segmentation();
            if (tooFewElementPixels == true && foundAreas == 0 || foundAreas > HandFeatures.maxAllowedAreas) {
                System.out.println("Segmentacja - blad przetwarzania!");
                resultBitmap = handFeatures.getProcessed(false);
                CameraView.refreshBackground = true;
                return;
            }

            //
            // ETAP 2 - zyznaczanie cech z obrazu
            //
            if (handFeatures.calculateFeatures() == true) {
                //CopyManager.saveBitmapToDisk(handFeatures.getConturBitmap(true), CameraView.foundedHandsFeatures++, "Contour_");
                handFeaturesData = new HandFeaturesData(handFeatures);
                handFeaturesData.show(true); // cechy 1 lini
                handFeaturesData.show(false); // wypisuje pogrupowane cechy
                ++HandFeatures.foundedHandsFeatures;
                resultBitmap = handFeatures.getProcessed(true);
                //Thread.sleep(2000);
            }
        } catch (HandFeaturesException hfe) {
            System.out.println("HandFeaturesException!." + hfe.toString());
            resultBitmap = handFeatures.getProcessed(false);
        } catch (Exception e) {
            System.out.println("Exception: Nie przewidziany wyjatek dla HandFeatures!");
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Returns Bitmap
     * @return Bitmap
     */
    public Bitmap getBitmap(){
        return this.resultBitmap;
    }

    public HandFeaturesData getHandFeaturesData(){
        return handFeaturesData;
    }


}
