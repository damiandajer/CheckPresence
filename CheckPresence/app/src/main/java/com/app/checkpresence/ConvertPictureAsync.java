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
public class ConvertPictureAsync extends AsyncTask<ConvertPictureAsyncParams, Void, Bitmap> {

    public ConvertPictureAsync(){}

    /**
     * Method runs new thread in background
     * @param params parameters for converting bytes to image
     * @return bitmap
     */
    @Override
    protected Bitmap doInBackground(ConvertPictureAsyncParams... params) {

        YuvImage image = new YuvImage(params[0].data, params[0].parameters.getPreviewFormat(),
                params[0].size.width, params[0].size.height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //image.
        image.compressToJpeg(
                new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                out);

        byte[] imageBytes = out.toByteArray();
        Bitmap imageBmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return imageBmp;
    }
}
