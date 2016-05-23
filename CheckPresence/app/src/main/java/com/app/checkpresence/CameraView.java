package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

import android.app.Activity;
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
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    Activity mainActivity;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public static Camera.Size size;
    private int frames = 1;
    private int pictureSaved = 0;
    private TextView savedPic;
    private ImageView bottomRight, bottomLeft, topLeft, topCenter, topRight, bottomCenter;
    private Button backgroundBtn;
    private Bitmap bmpBackground;
    Boolean getBckg = true;
    List<ImageView> cppViews, openCVViews;
    Frame frame, backgroundFrame;

    public CameraView(Context context, Activity activity, Camera camera){
        super(context);
        this.mainActivity = activity;
        this.backgroundBtn = (Button) this.mainActivity.findViewById(R.id.backgroundBtn);
        this.savedPic = (TextView) this.mainActivity.findViewById(R.id.saved);
        mCamera = camera;
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        mCamera.setDisplayOrientation(90);
        setCameraParameters();
        this.backgroundBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getBckg = true;
            }
        });
        initiateOpenCV();
        this.cppViews = new ArrayList<>();
        this.openCVViews = new ArrayList<>();
        this.frame = new Frame();
        this.backgroundFrame = new Frame();
        setAllViewsToVariables();
        setCppViewsList();
        setOpenCVViewsList();
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

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera _camera) {
                //getting once bitmap with background
                if(isGetBackgroundButtonClicked()){
                    getBackgroundFrame(data);
                }

                if(frames == 5) {
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
        frame.setActualFrame(data);

        Bitmap liveViewBitmap = frame.getActualBitmap();
        setImageToImageView(bottomCenter, liveViewBitmap);

        //-----------------OpenCV part (substracting background)----------------------------------------------
        frame.setBackground(bmpBackground);
        frame.setThresholds(10, 60, 4);
        frame.segmentateFrameWithOpenCV();
        List<Bitmap> openCVBitmaps = frame.getOpenCVBitmaps();
        setBitmapsToViews(openCVViews, openCVBitmaps);
        //CopyManager.saveBitmapToDisk(openCVBitmaps.get(0), pictureSaved, "OpenCV1-");
        //CopyManager.saveBitmapToDisk(openCVBitmaps.get(1), pictureSaved, "OpenCV2-");
        //CopyManager.saveBitmapToDisk(openCVBitmaps.get(2), pictureSaved, "OpenCV3-");

        //processing frame segmentation
        frame.setNumberOfConditions(3);
        frame.segmentateFrameWithCpp();
        List<Bitmap> cppBitmaps = frame.getCppBitmaps();

        setBitmapsToViews(cppViews, cppBitmaps);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
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
     * Get new background frame
     */
    public void getBackgroundFrame(byte[] data){
        backgroundFrame.setActualFrame(data);
        this.bmpBackground = backgroundFrame.getActualBitmap();
        this.getBckg = false;
        System.out.println("Pobrano nową próbkę tła...");
    }

    private Boolean isGetBackgroundButtonClicked(){
        return this.getBckg;
    }

    private void setAllViewsToVariables(){
        this.bottomRight = (ImageView) this.mainActivity.findViewById(R.id.segmentatedHand1);
        this.bottomLeft = (ImageView) this.mainActivity.findViewById(R.id.segmentatedHand3);
        this.topLeft = (ImageView) this.mainActivity.findViewById(R.id.segmentatedHand4);
        this.topCenter = (ImageView) this.mainActivity.findViewById(R.id.segmentatedHand5);
        this.topRight = (ImageView) this.mainActivity.findViewById(R.id.segmentatedHand6);
        this.bottomCenter = (ImageView) this.mainActivity.findViewById(R.id.liveView);
    }

    private void setCppViewsList(){
        this.cppViews.add(bottomRight);
        this.cppViews.add(bottomLeft);
    }

    private void setOpenCVViewsList(){
        this.openCVViews.add(topRight);
        this.openCVViews.add(topCenter);
        this.openCVViews.add(topLeft);
    }

    public void setBitmapsToViews(List<ImageView> views, List<Bitmap> bitmaps){
        int counter = 0;
        for (ImageView view:views) {
            setImageToImageView(view, bitmaps.get(counter));
            ++counter;
        }
    }

    private static void initiateOpenCV(){
        if (!OpenCVLoader.initDebug()) {
            Log.e("TEST", "OpenCVLoader Failed");
        }else {
            Log.e("TEST", "OpenCVLoader Succeeded");
            //System.loadLibrary("CameraVision");
            System.loadLibrary("opencv_java3");
        }
    }
}

