package com.app.memory;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by Damian on 19.05.2016.
 */
final public class CopyManager {
    private CopyManager(){};

    /**
     * Method is saving bmp file to memory
     * @param image Bitmap
     * @param licznik int number of picture
     * @param fileName name of file
     */
    public static void saveBitmapToDisk(Bitmap image, int licznik, String fileName){

        //String backupDBPath = "backupBMP/TomekB"+licznik+".bmp";
        String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/backupBMP");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        String newFileFullName = fileName + licznik + ".bmp";
        File backupImage = new File(mFolder.getAbsolutePath(), newFileFullName);

        if(!backupImage.exists()){
            System.out.println("Tworzę backupDB");
            try {
                backupImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(backupImage.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(backupImage);
                image.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        System.out.println("Zamykam OutputStream");
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveBitmapToDisk(List<Bitmap> images, int licznik, String fileName){
        int i = 1;
        String newFileName;
        for (Bitmap bmp:images
             ) {
            newFileName = fileName + i + "-";
            saveBitmapToDisk(bmp, licznik, newFileName);
            ++i;
        }
    }

    public static void saveHandFeaturesToTxt(List<float[]> handFeatures, String fileName){
        String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/handFeatures");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        String newFileFullName = fileName + ".txt";
        File results = new File(mFolder.getAbsolutePath(), newFileFullName);

        if(!results.exists()){
            System.out.println("Tworzę "+ newFileFullName);
            try {
                results.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(results.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(results);
                PrintWriter pw = new PrintWriter(fos);
                for (float[] feature:handFeatures
                     ) {
                    System.out.println("Zapisuje dane");
                    for(int i = 0; i<30; i++){
                        pw.print(String.valueOf(feature[i]) + " ");
                    }
                    pw.println();
                }
                pw.flush();
                pw.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        System.out.println("Zamykam OutputStream");
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
