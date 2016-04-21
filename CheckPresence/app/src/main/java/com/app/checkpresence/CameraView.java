package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.ImageFormat;
        import android.graphics.Matrix;
        import android.graphics.Rect;
        import android.graphics.YuvImage;
        import android.hardware.Camera;
        import android.os.Environment;
        import android.util.Base64;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.ImageView;
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
    private ImageView segmentatedHand;
    private Bitmap result;
    Buffer buffer;
    public native int[] myNativeCode(int[] argb, int[] returnedInputSegmentationFileData, int rows, int cols, int warunek);

    public CameraView(Context context, Camera camera, TextView saved, ImageView segmentatedHand){
        super(context);

        this.savedPic = saved;
        this.segmentatedHand = segmentatedHand;
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
    public void surfaceChanged(SurfaceHolder surfaceHolder, final int i, int i2, int i3) {

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
        parameters.setPreviewSize(size.width/2, size.height/2);
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

                    if(frames == 1) {
                        //number of saved pictures
                        ++pictureSaved;
                        savedPic.setText(pictureSaved + " saved");

                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();
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
*/
                        int[] argb = createIntArrayFromPreviewFrame(data, size);

                        int warunek = 2;
                        //for(int warunek = 2; warunek < 1; ++warunek) {
                        int[] inputColorSegmentationDataPicture = new int[size.height * size.width];
                        for (int i = 0; i < size.height * size.width; ++i) {
                            inputColorSegmentationDataPicture[i] = 0;
                        }
                        int[] segmentationDataPicture = myNativeCode(argb, inputColorSegmentationDataPicture, size.height, size.width, warunek);

                        Bitmap bmp = createBitmapAfterSegmentation(segmentationDataPicture, size);
                        //addCopy(bmp, pictureSaved, "wiedmo" + pictureSaved  + "_" + warunek + ".png");
                        //System.out.println("Zapisno:" + "wiedmo" + pictureSaved  + "_" + warunek + ".png");
                        setImageToImageView(segmentatedHand, bmp);

                        //Bitmap bmpColor = createBitmapBeforeSegmentation(inputColorSegmentationDataPicture, size);
                        //addCopy(bmp, pictureSaved, "wiedmoColor" + pictureSaved  + "_" + warunek + ".png");
                        //System.out.println("Zapisno:" + "wiedmoColor" + pictureSaved  + "_" + warunek + ".png");
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
    public void addCopy(Bitmap image, int licznik, String fileName){

       // FileOutputStream out = null;
        //File sd = Environment.getExternalStorageDirectory();
        //String backupDBPath = "backupBMP/TomekB"+licznik+".bmp";
        String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/backupBMP");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        //String s = "/zdjecie" + licznik + ".bmp";
        String s = fileName;

        File backupImage = new File(mFolder.getAbsolutePath(), s);
        //System.out.println("Utworzoni plik: " + s + " w lokalizacji: " + mFolder.getAbsolutePath().toString());

        FileOutputStream fos = null;

        /*String backupDBPath = "backupBMP/zdjecie.bmp";
        File backupImage = new File(sd, backupDBPath);*/
        //System.out.println(backupImage.toString());
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
                //out = new FileOutputStream(backupImage);
                //StringWriter writer = new StringWriter();
                fos = new FileOutputStream(backupImage);
                //System.out.println("Kompresuję...");
                image.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                //image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                /*image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                        out);*/
                //System.out.println(image.toString());
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

    /**
     * Method is rotating bitmap
     * @param source Bitmap to rotation
     * @param angle rotation angle
     * @return Bitmap
     */
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        System.out.println("Bitmap rotated");
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Method is flipping bitmap by its y axis
     * @param source Bitmap to flip
     * @return Bitmap
     */
    public static Bitmap flipBitmap(Bitmap source)
    {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        System.out.println("Bitmap flipped");
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Method create a Bitmap from int array of ARGB pixels after segmentation,
     * set Bitmap configuration to ARGB_8888, rotate Bitmap to portrait mode and
     * flip it by its y axis (when we convert right hand, thumb is in right side
     * of picture)
     * @param segmentationDataPicture int array of ARGB pixels after segmentation
     * @param size Size parameters of camera in device
     * @return Bitmap
     */
    public Bitmap createBitmapAfterSegmentation(int[] segmentationDataPicture, Camera.Size size){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(size.width, size.height, conf);
        bmp.setPixels(segmentationDataPicture, 0, size.width, 0, 0, size.width, size.height);
        bmp = RotateBitmap(bmp, -90);
        bmp = flipBitmap(bmp);

        return bmp;
    }

    /**
     * Method create a Bitmap from int array of ARGB pixels before segmentation,
     * set Bitmap configuration to ARGB_8888, rotate Bitmap to portrait mode and
     * flip it by its y axis (when we convert right hand, thumb is in right side
     * of picture)
     * @param inputColorSegmentationDataPicture int array of ARGB pixels before segmentation
     * @param size Size parameters of camera in device
     * @return Bitmap
     */
    public Bitmap createBitmapBeforeSegmentation(int[] inputColorSegmentationDataPicture, Camera.Size size){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(size.width, size.height, conf);
        bmp.setPixels(inputColorSegmentationDataPicture, 0, size.width, 0, 0, size.width, size.height);
        bmp = RotateBitmap(bmp, -90);
        bmp = flipBitmap(bmp);

        return bmp;
    }

    /**
     * Returns int array of pixels in ARGB configuration
     * @param data byte array from PreviewFrame
     * @param size Size parameters of camera in device
     * @return int array
     */
    public int[] createIntArrayFromPreviewFrame(byte[] data, Camera.Size size){
        int[] argb;
        argb = new int[size.height * size.width];
        YUV_NV21_TO_RGB(argb, data, size.width, size.height);

        return argb;
    }

    public void setImageToImageView(ImageView imageView, Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

}

