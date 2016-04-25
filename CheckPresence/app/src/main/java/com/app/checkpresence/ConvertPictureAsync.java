package com.app.checkpresence;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Damian on 09.04.2016.
 */
public class ConvertPictureAsync extends AsyncTask<ConvertPictureAsyncParams, Void, int[]> {

    public ConvertPictureAsync(){}

    public native int[] myNativeCode(int[] argb, int[] returnedInputSegmentationFileData, int rows, int cols, int warunek);

    /**
     * Method runs new thread in background
     * @param params parameters for converting bytes to image
     * @return bitmap
     */
    @Override
    protected int[] doInBackground(ConvertPictureAsyncParams... params) {

        int[] segmentationDataPicture = myNativeCode(params[0].argb, params[0].inputColorSegmentationDataPicture, params[0].height,
                params[0].width, params[0].warunek);

        return segmentationDataPicture;
    }
}
