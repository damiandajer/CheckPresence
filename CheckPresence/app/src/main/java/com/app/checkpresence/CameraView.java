package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.hardware.Camera;
        import android.util.Log;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;

        import org.opencv.android.OpenCVLoader;

        import java.io.IOException;
        import java.util.List;

        import static org.opencv.core.Core.absdiff;
        import static org.opencv.core.Core.subtract;
        import static org.opencv.imgproc.Imgproc.cvtColor;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Size size;
    private int frames = 1;
    private int pictureSaved = 0;
    private TextView savedPic;
    private ImageView bottomRight, bottomLeft, topLeft, topCenter, topRight, bottomCenter;
    private Bitmap bmpBackground;
    Boolean getBckg = true;

    public CameraView(Context context, Camera camera, TextView saved, ImageView segmentatedHand1, ImageView liveView,
                      ImageView segmentatedHand3, ImageView segmentatedHand4, ImageView segmentatedHand5, ImageView segmentatedHand6,
                      Button backgroundBtn){
        super(context);

        this.savedPic = saved;
        this.bottomRight = segmentatedHand1;
        this.bottomLeft = segmentatedHand3;
        this.topLeft = segmentatedHand4;
        this.topCenter = segmentatedHand5;
        this.topRight = segmentatedHand6;
        this.bottomCenter = liveView;
        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        setCameraParameters();
        backgroundBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getBackgroundFrame();
            }
        });
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

        //setCameraParameters();

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
            if(getBckg){
                int[] intBackground = createIntArrayFromPreviewFrame(data);
                bmpBackground = getBitmapsFromIntArray(intBackground);
                getBckg = false;
            }

            if(frames == 1) {
                //number of processed pictures
                ++pictureSaved;
                savedPic.setText(pictureSaved + " processed");

                segmentateImagesGivenAsBytes(data);

                //set frames to 0 (return to the beginning of loop)
                frames = 0;
            }
            //number of frames
            ++frames;
            }
        });
    }

    /**
     * Setting camera parameters
     */
    public void setCameraParameters(){
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.set("orientation", "portrait");
        parameters.setRotation(90);

        Camera.Size size = parameters.getPreviewSize();
        parameters.setPreviewSize(size.width / 2, size.height / 2);
        mCamera.setParameters(parameters);
        this.size = parameters.getPreviewSize();
    }

    /**
     * Process segmentation of data from camera preview, sets results to ImageViews
     * @param data byte Array
     */
    public void segmentateImagesGivenAsBytes(byte[] data){
        int[] argb = createIntArrayFromPreviewFrame(data);

        Bitmap liveViewBitmap = getBitmapsFromIntArray(argb);
        setImageToImageView(bottomCenter, liveViewBitmap);

        //-----------------OpenCV part (substracting background)----------------------------------------------
        List<Bitmap> openCVBitmaps = getBitmapsProcessedWithOpenCV(liveViewBitmap);
        //CopyManager.saveBitmapToDisk(openCVBitmaps.get(0), pictureSaved, "OpenCV");

        //processing frame segmentation
        List<int[]> segmentatedIntArray = getBitmapsProcessedWithCpp(argb, 4);

        //getting results of processing
        List<Bitmap> processedFramesBitmaps = getBitmapsFromIntArray(segmentatedIntArray);

        setImageToImageView(bottomRight, processedFramesBitmaps.get(0));
        setImageToImageView(bottomLeft, processedFramesBitmaps.get(1));
        setImageToImageView(topLeft, processedFramesBitmaps.get(2));
        setImageToImageView(topCenter, openCVBitmaps.get(0));
        setImageToImageView(topRight, openCVBitmaps.get(1));
    }

    /**
     * Returns Bitmap created from int Array of pixels
     * @param argb int Array of pixels
     * @return Bitmap
     */
    public Bitmap getBitmapsFromIntArray(int[] argb){
        BitmapFromPixelsThreads bitmapFromPixelsThreads = BitmapFromPixelsThreads.getNewObject();
        bitmapFromPixelsThreads.addNewThread(argb, size.height, size.width);
        bitmapFromPixelsThreads.executeThreads();

        return bitmapFromPixelsThreads.getBitmaps().get(0);
    }

    /**
     * Returns List of Bitmap created from List of int Arrays of pixels
     * @param argb int Array of pixels
     * @return List of Bitmaps
     */
    public List<Bitmap> getBitmapsFromIntArray(List<int[]> argb){
        BitmapFromPixelsThreads bitmapFromPixelsThreads = BitmapFromPixelsThreads.getNewObject();
        bitmapFromPixelsThreads.addNewThread(argb, size.height, size.width);
        bitmapFromPixelsThreads.executeThreads();

        return bitmapFromPixelsThreads.getBitmaps();
    }

    /**
     * Returns List of segmentated Bitmaps in OpenCV, created from Bitmap of preview frame, background Bitmap
     * and List of thresholds
     * @param liveViewBitmap Bitmap of preview frame
     * @return List of segmentated Bitmaps
     */
    public List<Bitmap> getBitmapsProcessedWithOpenCV(Bitmap liveViewBitmap){
        OpenCVSubtractionThreads openCVSubtractionThreads = OpenCVSubtractionThreads.getNewObject();
        openCVSubtractionThreads.createListOfThresholds(50, 100, 2);
        openCVSubtractionThreads.addNewThread(liveViewBitmap, bmpBackground);
        openCVSubtractionThreads.executeThreads();

        return openCVSubtractionThreads.getBitmaps();
    }

    /**
     * Returns List of segmentated Bitmaps in Native code with many conditions
     * @param argb int Array of pixels
     * @param numberOfConditions number of conditions (1-4)
     * @return List of segmentated Bitmaps
     */
    public List<int[]> getBitmapsProcessedWithCpp(int[] argb, int numberOfConditions){
        SegmentationThreads segmentationThreads = SegmentationThreads.getNewObject();
        segmentationThreads.createListOfConditions(numberOfConditions);
        segmentationThreads.addNewThread(argb, size.height, size.width);
        segmentationThreads.executeThreads();

        return segmentationThreads.getIntArrays();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
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
     * @return int array
     */
    public int[] createIntArrayFromPreviewFrame(byte[] data){
        int[] argb = new int[size.height * size.width];
        YUV_NV21_TO_RGB(argb, data, size.width, size.height);

        return argb;
    }

    /**
     * Sets Bitmap to selected ImageView
     * @param imageView selected imageView
     * @param bitmap Bitmap to show
     */
    public void setImageToImageView(ImageView imageView, Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Set new background
     */
    public void getBackgroundFrame(){
        this.getBckg = true;
        System.out.println("Pobrano nową próbkę tła...");
    }
}

