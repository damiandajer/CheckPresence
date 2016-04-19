package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.ImageFormat;
        import android.graphics.Rect;
        import android.graphics.YuvImage;
        import android.hardware.Camera;
        import android.os.Environment;
        import android.util.Base64;
        import android.util.Log;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.StringWriter;
        import java.nio.Buffer;
        import java.nio.channels.FileChannel;
        import java.util.concurrent.ExecutionException;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int frames = 0;
    private int pictureSaved = 0;
    private TextView savedPic;
    private Bitmap result;
    Buffer buffer;
    public native String myNativeCode(int[] argb ,int dlugosc, int rows, int cols);
    int[] argb;

    public CameraView(Context context, Camera camera, TextView saved){
        super(context);

        this.savedPic = saved;
        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        //this.saved = (TextView) findViewById(R.id.saved);
        //this.saved.setText("0 saved");

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if(mHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        //Setting camera parameters
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        parameters.set("orientation", "portrait");
        parameters.setRotation(90);
        //parameters.setPreviewFormat(ImageFormat.);
        mCamera.setParameters(parameters);

        //now, recreate the camera preview
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }


            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera _camera) {
                    //number of frames
                    ++frames;

                    if(frames == 30) {
                        //number of saved pictures
                        ++pictureSaved;
                        //Log.d("surfaceChanged",String.format("Got %d bytes of camera data", _data.length));
                        //System.out.println("Got bytes of camera data: " + data.length);
                        //Bitmap previewBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();

                        /*for(int i =0; i<data.length; i++)
                            System.out.println(data[i]);*/
                        String dane = "jestem z javy";
                        //System.out.println(myNativeCode(dane, dane.length()));
/*
                        //Creating classes with parameters and asynchronic converting picture
                        ConvertPictureAsyncParams params = new ConvertPictureAsyncParams(data, parameters, size);
                        ConvertPictureAsync convertPictureAsync = new ConvertPictureAsync();

                        //running new thread which convert picture
                        try {
                            result = convertPictureAsync.execute(params).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        //hgfg
*/
                        argb = new int[size.height * size.width];
                        YUV_NV21_TO_RGB(argb, data, size.width, size.height);
                        //Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        //String temp= BitMapToString(result);
                        //System.out.println(temp);
                        System.out.println(myNativeCode(argb, argb.length, size.height, size.width));

                        //addCopy(result, pictureSaved);
                        savedPic.setText(pictureSaved + " saved");
                        frames = 0;
                    }
                }
            });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
    }

    /**
     * Method is saving bmp file to memory
     * @param image Bitmap
     */
    public void addCopy(Bitmap image, int licznik){

        FileOutputStream out = null;
        File sd = Environment.getExternalStorageDirectory();
        //String backupDBPath = "backupBMP/TomekB"+licznik+".bmp";
        String backupDBPath = "backupBMP/zdjecie.bmp";
        File backupImage = new File(sd, backupDBPath);
        System.out.println(backupImage.toString());
        if(!backupImage.exists()){
            System.out.println("Tworzę backupDB");
            try {
                backupImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(backupImage.exists()) {
            try {
                out = new FileOutputStream(backupImage);
                //StringWriter writer = new StringWriter();
                System.out.println("Kompresuję...");
                image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                /*image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                        out);*/
                //System.out.println(image.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        System.out.println("Zamykam OutputStream");
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

}

