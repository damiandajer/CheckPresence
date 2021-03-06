package com.app.memory;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.app.database.DataBase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by Damian on 19.05.2016.
 */
final public class CopyManager {
    private CopyManager(){}
    private static File mFolder, file, currentDB, backupDB;


    /**
     * Method is saving bmp file to memory
     * @param image Bitmap
     * @param licznik int number of picture
     * @param fileName name of file
     */
    public static void saveBitmapToDisk(Bitmap image, int licznik, String fileName){

        createDir("backupBMP");

        String newFileFullName = fileName + licznik + ".png";
        createFile(newFileFullName);
        saveBitmapToFile(image);
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
        createDir("handFeatures");

        String newFileFullName = fileName + ".txt";
        createFile(newFileFullName);
        saveFeaturesToFile(handFeatures);
    }

    private static void createDir(String nameOfDirectory){
        String extr = Environment.getExternalStorageDirectory().toString();
        mFolder = new File(extr + "/" + nameOfDirectory);
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
    }

    private static void createFile(String nameOfFile){
        file = new File(mFolder.getAbsolutePath(), nameOfFile);

        if(!file.exists()){
            System.out.println("Tworzę "+ file);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveFeaturesToFile(List<float[]> handFeatures){
        if(file.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
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

    private static void saveBitmapToFile(Bitmap image){
        if(file.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
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

    public static void addCopyOfDatabase(Context context){
        setCurrentDBPath();
        createDir("backupCheckpresenceDB");
        createFile("baza.db");
        copyDatabaseToSd(context);
    }

    public static void loadBackupOfDatabase(Context context){
        setCurrentDBPath();
        setBackupDBPath();
        copyDatabaseToApp(context);
    }

    private static void setBackupDBPath(){
        File sd = Environment.getExternalStorageDirectory();
        String backupDBPath = "backupCheckpresenceDB/baza.db";
        backupDB = new File(sd, backupDBPath);
    }

    private static void setCurrentDBPath(){
        String currentDBPath = "//data//com.app.checkpresence//databases//baza.db";
        File data = Environment.getDataDirectory();
        currentDB = new File(data, currentDBPath);
    }

    private static void copyDatabaseToSd(Context context){
        try {
            if(file.exists()) {
                System.out.println("Kopiuję pliki");
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(file).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(context, file.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private static void copyDatabaseToApp(Context context){
        try {
            FileChannel src = new FileInputStream(backupDB).getChannel();
            FileChannel dst = new FileOutputStream(currentDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Toast.makeText(context, currentDB.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
