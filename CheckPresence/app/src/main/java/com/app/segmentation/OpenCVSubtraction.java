package com.app.segmentation;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.app.checkpresence.AddUserCameraView;
import com.app.checkpresence.CameraView;
import com.app.checkpresence.Configure;
import com.app.handfeatures.Color;
import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesData;
import com.app.handfeatures.HandFeaturesException;
import com.app.handfeatures.HandFeaturesRaport;
import com.app.memory.CopyManager;

import org.opencv.core.CvType;
import org.opencv.core.Mat;


/**
 * Created by Damian on 17.05.2016.
 */
public class OpenCVSubtraction implements Runnable {
    private volatile Bitmap resultBitmap;
    Bitmap inputBitmap, backgroundBitmap;
    int height, width, threshold;

    private HandFeatures handFeatures = null;
    Rect areaToSegmentation;

    HandFeaturesRaport report;

    public HandFeaturesRaport getReport() {
        return report;
    }


    /**
     *
     * @param inputBitmap Bitmap to process
     * @param backgroundBitmap Bitmap with background
     */
    public OpenCVSubtraction(Bitmap inputBitmap, Bitmap backgroundBitmap, int threshold, Rect sizeOfBitmapToSegmentation) {
        this.inputBitmap = inputBitmap;
        this.backgroundBitmap = backgroundBitmap;
        resultBitmap = inputBitmap;
        this.areaToSegmentation = sizeOfBitmapToSegmentation;

        this.height = inputBitmap.getHeight();
        this.width = inputBitmap.getWidth();
        this.threshold = threshold;
    }

    @Override
    public void run() {
        boolean useOpenCV = true;
        if (useOpenCV)
            System.out.println("Thread is processing frame with OpenCV");
        else
            System.out.println("Thread is processing frame withOUT OpenCV");


        int numberOfElementPixels = 0;
        try {
            handFeatures = new HandFeatures(inputBitmap, backgroundBitmap);
            //
            // ETAP 1 - przygotowanie obazu do natepnego etapu
            //
            if (useOpenCV) { // Z OPENCV
                numberOfElementPixels = handFeatures.binarizationOpenCV(this.threshold);
            }
            else { // BEZ OPENCV
                numberOfElementPixels = handFeatures.binaryzation(45, Color.BG_COLOR, Color.EL_COLOR);
            }

            // zapisuje obraz po binaryzacji do bitmapy na external storage
            if (Configure.SAVE_HAND_RECOGNIZATION_STEPS) {
                CopyManager.saveBitmapToDisk(handFeatures.getProcessed(false), HandFeatures.foundedHandsFeatures, "binarized_");
            }

            // ---------------
            // RESZTA KODU JEST TAKA SAMA NIEZALEZNIE OD PROCESU BINARYZACJI
            // ------------------------

            // jezeli np plama dloni jest za mala, lub jakis blad przy przetwarzaniu to zwruc aktualny wyglad obrazu i zakoncz przetwarzanie tej klatki
            resultBitmap = handFeatures.getProcessed(false);
            // jezeli obraz ma mniej niz 3% pixeli koloru elementu nie przetwarzaj dalej - najprowodpodobniej nie ma reki na obrazie
            if (numberOfElementPixels < (handFeatures.getImage().width() * handFeatures.getImage().height()) * 0.03) {
                report = handFeatures.getRaport();
                //System.out.println("R1!");
                return;
            }
            // jezeli obraz ma wiecej niz 70% pixeli koloru elementu nie przetwarzaj dalej - najprowodpodobniej nie ma reki na obrazie
            if (numberOfElementPixels > (handFeatures.getImage().width() * handFeatures.getImage().height()) * 0.70) {
                report = handFeatures.getRaport();
                //System.out.println("R2!");
                return;
            }

            // czyscimy pijedyncze kropki
            handFeatures.getImage().setBorderColor(Color.BG_COLOR);
            handFeatures.getImage().smoothEdge(Color.EL_COLOR, Color.BG_COLOR);

            // segmentacja
            int foundAreas = handFeatures.segmentation(areaToSegmentation);
            resultBitmap = handFeatures.getProcessed(false);
            if (foundAreas == 0) {
                report = handFeatures.getRaport();
                //System.out.println("R3!");
                return;
            }

            // zapisanie do pliku obrazu po segmentacji
            if (Configure.SAVE_HAND_RECOGNIZATION_STEPS) {
                CopyManager.saveBitmapToDisk(handFeatures.getProcessed(false), HandFeatures.foundedHandsFeatures, "segmentated_");
            }

            if (foundAreas > HandFeatures.maxAllowedAreas) {
                report = handFeatures.getRaport();
                //System.out.println("R4!");
                return;
            }

            report = handFeatures.getRaport();
            //System.out.println("R5!");

        } catch (HandFeaturesException hfe) {
            if (Configure.SHOW_FOUND_HAND_FEATURES_EXCEPTIONS == true)
                System.out.println("HandFeaturesException!." + hfe.toString());
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

    public HandFeatures getHandFeaturesObject(){
        return this.handFeatures;
    }

}
