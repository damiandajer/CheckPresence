package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.hardware.Camera;
        import android.os.AsyncTask;
        import android.os.Environment;
        import android.util.Base64;
        import android.util.Log;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.nio.Buffer;
        import java.util.concurrent.ExecutionException;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int frames = 0;
    private int pictureSaved = 0;
    private TextView savedPic;
    private ImageView segmentatedHand1, segmentatedHand3, segmentatedHand4, segmentatedHand5, segmentatedHand6;
    private ImageView liveView;
    private int[] result;
    Buffer buffer;
    public native int[] myNativeCode(int[] argb, int rows, int cols, int warunek);

    public CameraView(Context context, Camera camera, TextView saved, ImageView segmentatedHand1, ImageView liveView,
                      ImageView segmentatedHand3, ImageView segmentatedHand4, ImageView segmentatedHand5, ImageView segmentatedHand6){
        super(context);

        this.savedPic = saved;
        this.segmentatedHand1 = segmentatedHand1;
        this.segmentatedHand3 = segmentatedHand3;
        this.segmentatedHand4 = segmentatedHand4;
        this.segmentatedHand5 = segmentatedHand5;
        this.segmentatedHand6 = segmentatedHand6;
        this.liveView = liveView;
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
        parameters.setPreviewSize(size.width / 4, size.height / 4);
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
                        savedPic.setText(pictureSaved + " processed");

                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();

                        int[] argb = createIntArrayFromPreviewFrame(data, size);

                        //creating colored bitmap from frame, cropping it (in new thread) and setting to imageView
                        CreateBitmapFromPixels colouredBitmapFromPixels = new CreateBitmapFromPixels(argb, size);
                        Thread threadColouredBitmapFromPixels = new Thread(colouredBitmapFromPixels);
                        threadColouredBitmapFromPixels.start();
                        try {
                            threadColouredBitmapFromPixels.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setImageToImageView(liveView, colouredBitmapFromPixels.getBitmap());

                        System.out.println("Test -03. Juz po czesci watkow!");
                        //processing frame segmentation (in new thread)
                        Segmentation segmentation1 = new Segmentation(argb, size, 1);
                        Thread threadSegmentation1 = new Thread(segmentation1);
                        threadSegmentation1.start();

                        Segmentation segmentation2 = new Segmentation(argb, size, 2);
                        Thread threadSegmentation2 = new Thread(segmentation2);
                        threadSegmentation2.start();

                        Segmentation segmentation3 = new Segmentation(argb,  size, 3);
                        Thread threadSegmentation3 = new Thread(segmentation3);
                        threadSegmentation3.start();

                        Segmentation segmentation4 = new Segmentation(argb, size, 4);
                        Thread threadSegmentation4 = new Thread(segmentation4);
                        threadSegmentation4.start();

                        System.out.println("Test -02. Przed try!");

                        //wait for threads end
                        try {
                            threadSegmentation1.join();
                            threadSegmentation2.join();
                            threadSegmentation3.join();
                            threadSegmentation4.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Test -02. Po try catch!");

                        //getting results of processing
                        int[] segmentationDataPicture1 = segmentation1.getSegmentatedPicture();
                        int[] segmentationDataPicture2 = segmentation2.getSegmentatedPicture();
                        int[] segmentationDataPicture3 = segmentation3.getSegmentatedPicture();
                        int[] segmentationDataPicture4 = segmentation4.getSegmentatedPicture();

                        System.out.println("Test -01. Juz po czesci watkow!");

                        //creating segmentated bitmap from int array and cropping it (in new thread)
                        CreateBitmapFromPixels segmentatedBitmapFromPixels1 = new CreateBitmapFromPixels(segmentationDataPicture1, size);
                        Thread threadSegmentatedBitmapFromPixels1 = new Thread(segmentatedBitmapFromPixels1);
                        threadSegmentatedBitmapFromPixels1.start();

                        CreateBitmapFromPixels segmentatedBitmapFromPixels2 = new CreateBitmapFromPixels(segmentationDataPicture2, size);
                        Thread threadSegmentatedBitmapFromPixels2 = new Thread(segmentatedBitmapFromPixels2);
                        threadSegmentatedBitmapFromPixels2.start();

                        CreateBitmapFromPixels segmentatedBitmapFromPixels3 = new CreateBitmapFromPixels(segmentationDataPicture3, size);
                        Thread threadSegmentatedBitmapFromPixels3 = new Thread(segmentatedBitmapFromPixels3);
                        threadSegmentatedBitmapFromPixels3.start();

                        CreateBitmapFromPixels segmentatedBitmapFromPixels4 = new CreateBitmapFromPixels(segmentationDataPicture4, size);
                        Thread threadSegmentatedBitmapFromPixels4 = new Thread(segmentatedBitmapFromPixels4);
                        threadSegmentatedBitmapFromPixels4.start();

                        System.out.println("Test 00. Juz po czesci watkow!");
                        //wait for threads end and set bitmap to ImageView
                        try {
                            threadSegmentatedBitmapFromPixels1.join();
                            setImageToImageView(segmentatedHand1, segmentatedBitmapFromPixels1.getBitmap());
                            threadSegmentatedBitmapFromPixels2.join();
                            setImageToImageView(segmentatedHand3, segmentatedBitmapFromPixels2.getBitmap());
                            threadSegmentatedBitmapFromPixels3.join();
                            setImageToImageView(segmentatedHand4, segmentatedBitmapFromPixels3.getBitmap());
                            threadSegmentatedBitmapFromPixels4.join();
                            setImageToImageView(segmentatedHand5, segmentatedBitmapFromPixels4.getBitmap());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //set frames to 0 (return to the beginning of loop)
                        frames = 0;
                        System.out.println("Juz po tych wszystkich watkach!");
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
        String s = fileName + licznik + ".bmp";

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

    /**
     * Returns Bitmap created from String of bytes
     * @param encodedString String of bytes
     * @return Bitmap created from String of bytes
     */
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

    /**
     * Returns String of bytes created from bitmap
     * @param bitmap Bitmap
     * @return String of bytes created from bitmap
     */
    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * Method is converting byte array to int array with argb pixels
     * @param argb int array with argb pixels
     * @param yuv byte array
     * @param width width of picture
     * @param height height of picture
     */
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
        //System.out.println("Bitmap rotated");
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
        //System.out.println("Bitmap flipped");
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Method create a Bitmap from int array of ARGB pixels,
     * set Bitmap configuration to ARGB_8888, rotate Bitmap to portrait mode and
     * flip it by its y axis (when we convert right hand, thumb is in right side
     * of picture)
     * @param inputColorSegmentationDataPicture int array of ARGB pixels before segmentation
     * @param size Size parameters of camera in device
     * @return Bitmap
     */
    public Bitmap createProcessedBitmap(int[] inputColorSegmentationDataPicture, Camera.Size size){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(size.width, size.height, conf);
        bmp.setPixels(inputColorSegmentationDataPicture, 0, size.width, 0, 0, size.width, size.height);
        bmp = RotateBitmap(bmp, -90);
        bmp = flipBitmap(bmp);

        Bitmap bmpCropped = Bitmap.createBitmap(bmp, 0, 0, size.height, size.width*2/3);
        return bmpCropped;
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

    private class CreateBitmapFromPixels implements Runnable {
        private volatile Bitmap bmp;
        int[] inputColorSegmentationDataPicture;
        android.hardware.Camera.Size size;
        public CreateBitmapFromPixels(int[] inputColorSegmentationDataPicture, Camera.Size size) {
            // store parameter for later user
            this.inputColorSegmentationDataPicture = inputColorSegmentationDataPicture;
            this.size = size;
        }

        @Override
        public void run() {
            //System.out.println("Tread creating processed bitmap");
            this.bmp = createProcessedBitmap(inputColorSegmentationDataPicture, size);
        }

        public Bitmap getBitmap(){
            return this.bmp;
        }
    }

    private class Segmentation implements Runnable {
        private volatile int[] segmentatedPicture;
        int[] argb;
        int warunek;
        //int[] inputColorSegmentationDataPicture;
        android.hardware.Camera.Size size;
        public Segmentation(int[] argb, Camera.Size size, int warunek) {
            // store parameter for later user
            this.argb = argb;
            //this.inputColorSegmentationDataPicture = inputColorSegmentationDataPicture;
            this.size = size;
            this.warunek = warunek;
        }

        @Override
        public void run() {
            System.out.println("Tread processing frame with condition: " + warunek);
            this.segmentatedPicture = myNativeCode(argb, size.height, size.width, warunek);
        }

        public int[] getSegmentatedPicture(){
            return this.segmentatedPicture;
        }
    }
}

