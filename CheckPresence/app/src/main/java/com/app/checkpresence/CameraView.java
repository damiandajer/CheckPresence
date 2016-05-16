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

        import org.opencv.android.BaseLoaderCallback;
        import org.opencv.android.LoaderCallbackInterface;
        import org.opencv.android.OpenCVLoader;
        import org.opencv.android.Utils;
        import org.opencv.core.CvType;
        import org.opencv.core.Mat;
        import org.opencv.imgproc.Imgproc;
        import org.opencv.video.BackgroundSubtractorMOG2;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.nio.Buffer;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.concurrent.ExecutionException;

        import static org.opencv.core.Core.absdiff;
        import static org.opencv.core.Core.subtract;
        import static org.opencv.imgproc.Imgproc.cvtColor;
        import static org.opencv.imgproc.Imgproc.threshold;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int frames = 0;
    private int pictureSaved = 0;
    private TextView savedPic;
    private ImageView segmentatedHand1, segmentatedHand3, segmentatedHand4, segmentatedHand5, segmentatedHand6;
    private ImageView liveView;
    private Bitmap bmpBackground, subtractingResult;

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

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("TEST", "OpenCVLoader Failed");
        }else {
            Log.e("TEST", "OpenCVLoader Succeeded");
            //System.loadLibrary("CameraVision");
            System.loadLibrary("opencv_java3");
        }
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
        parameters.setPreviewSize(size.width / 2, size.height / 2);
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
                    //getting once bitmap with background
                    if(frames == 0){
                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();

                        int[] intBackground = createIntArrayFromPreviewFrame(data, size);

                        //creating colored bitmap from frame, cropping it (in new thread) and setting to imageView
                        CreateBitmapFromPixels colouredBitmapFromPixelsBackground = new CreateBitmapFromPixels(intBackground, size.height, size.width);
                        Thread threadColouredBitmapFromPixelsBackground = new Thread(colouredBitmapFromPixelsBackground);
                        threadColouredBitmapFromPixelsBackground.start();
                        try {
                            threadColouredBitmapFromPixelsBackground.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bmpBackground = colouredBitmapFromPixelsBackground.getBitmap();
                        //number of frames
                        ++frames;
                    }

                    if(frames == 1) {
                        //number of processed pictures
                        ++pictureSaved;
                        savedPic.setText(pictureSaved + " processed");

                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();

                        int[] argb = createIntArrayFromPreviewFrame(data, size);

                        //creating colored bitmap from frame, cropping it (in new thread) and setting to imageView
                        CreateBitmapFromPixels colouredBitmapFromPixels = new CreateBitmapFromPixels(argb, size.height, size.width);
                        Thread threadColouredBitmapFromPixels = new Thread(colouredBitmapFromPixels);
                        threadColouredBitmapFromPixels.start();
                        try {
                            threadColouredBitmapFromPixels.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setImageToImageView(liveView, colouredBitmapFromPixels.getBitmap());

                        //-----------------OpenCV part (substracting background)----------------------------------------------
                        //width and height after cropping
                        int width = colouredBitmapFromPixels.getBitmap().getWidth();
                        int height = colouredBitmapFromPixels.getBitmap().getHeight();

                        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                        subtractingResult = Bitmap.createBitmap(width, height, conf);

                        Mat  imgToProcess1 = new Mat(height, width, CvType.CV_8UC4);
                        Mat  imgToProcess2 = new Mat(height, width, CvType.CV_8UC4);
                        Mat  imgToProcess = new Mat(height, width, CvType.CV_8UC4);

                        Utils.bitmapToMat(colouredBitmapFromPixels.getBitmap(), imgToProcess1);
                        Utils.bitmapToMat(bmpBackground, imgToProcess2);

                        //absdiff(imgToProcess1, imgToProcess2, imgToProcess);
                        subtract(imgToProcess2, imgToProcess1, imgToProcess);

                        Mat mask = new Mat(height, width, CvType.CV_8U);
                        cvtColor(imgToProcess, mask, Imgproc.COLOR_RGBA2GRAY, 1); //your conversion specifier may vary
                        Imgproc.threshold(mask, mask, 50, 255, Imgproc.THRESH_BINARY);

                        Utils.matToBitmap(mask, subtractingResult);
                        //---------------------------------------------------------------

                        //processing frame segmentation (in new thread)
                        Segmentation[] segmentations = new Segmentation[4];
                        for(int i=1; i<5;i++){
                            Segmentation newSegmentation = new Segmentation(argb,size.height,size.width,i);
                            segmentations[i-1] = newSegmentation;
                            ThreadHandler.createThread(newSegmentation);
                        }
                        ThreadHandler.startThreads();

                        //wait for threads end
                        try {
                            ThreadHandler.joinThreads();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //getting results of processing
                        List<int[]> segmentationDataPictures = new ArrayList<int[]>();
                        for(int i=0;i<4;i++)
                            segmentationDataPictures.add(segmentations[i].getSegmentatedPicture());

                        //creating segmentated bitmap from int array and cropping it (in new thread)
                        CreateBitmapFromPixels[] bitmaps = new CreateBitmapFromPixels[4];
                        for(int i=0; i<4;i++){
                            CreateBitmapFromPixels bitmap = new CreateBitmapFromPixels(segmentationDataPictures.get(i),size.height,size.width);
                            bitmaps[i] = bitmap;
                            ThreadHandler.createThread(bitmap);
                        }
                        ThreadHandler.startThreads();

                        //wait for threads end and set bitmap to ImageView
                        try {
                            ThreadHandler.joinThreads();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setImageToImageView(segmentatedHand1, bitmaps[0].getBitmap());
                        setImageToImageView(segmentatedHand3, bitmaps[1].getBitmap());
                        setImageToImageView(segmentatedHand4, bitmaps[2].getBitmap());
                        setImageToImageView(segmentatedHand5, bitmaps[3].getBitmap());
                        setImageToImageView(segmentatedHand6, subtractingResult);
                        //set frames to 0 (return to the beginning of loop)
                        frames = 0;
                    }

                    //number of frames
                    ++frames;
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
     * @param licznik int number of picture
     * @param fileName name of file
     */
    public void saveBitmapToDisk(Bitmap image, int licznik, String fileName){

        //String backupDBPath = "backupBMP/TomekB"+licznik+".bmp";
        String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/backupBMP");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        //String s = "/zdjecie" + licznik + ".bmp";
        String s = fileName + licznik + ".bmp";

        File backupImage = new File(mFolder.getAbsolutePath(), s);

        FileOutputStream fos = null;

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

