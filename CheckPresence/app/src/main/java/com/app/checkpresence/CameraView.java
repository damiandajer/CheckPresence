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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.database.DataBase;
import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesData;
import com.app.memory.CopyManager;
import com.app.picture.Frame;
import com.app.recognition.HandRecognizer;
import com.app.recognition.Normalizer;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    public static boolean refreshBackground = true;
    public final static long autoAdjustmentTime = 2000000000L; // czas w ns, po jakim kamera zablokuje ekspozycje swiatla us

    public boolean measureCameraTime; // czy odliczac czas dla ustabilizowania kamery i jej blokady
    private long startTime; //punkt poczatkowy czasu. actualTime - startTime >= autoAdjustmentTime => zablokowanie kamery
    private Camera.Parameters startParameters; // poczatkowe ustawienia kamery, jezeli odblokujemy aparato to chcemy wlasnie do nich powrucic

    Activity mainActivity;
    MainActivity mainActivityObject;
    protected SurfaceHolder mHolder;
    protected Camera mCamera;
    public static Camera.Size size;
    protected int frames = 1;
    protected int pictureSaved = 0;
    protected TextView savedPic;
    private ImageView bottomRight, bottomLeft, topLeft, topCenter, topRight, bottomCenter;
    private ImageButton backgroundBtn;
    private Bitmap bmpBackground;
    //public static boolean refreshBackground = true;
    private List<ImageView> cppViews, openCVViews;
    private Frame frame, backgroundFrame;
    private List<float[]> actualHandFeatures, allHandFeatures;
    private HandRecognizer handRecognizer;
    private Map<Integer, List<float[]>> usersWithTraits;
    private DataBase dataBase;
    private List<Integer> recognisedUsers;
    private boolean cameraNull;
    private HandFeaturesData handFeaturesData;

    public CameraView(Context context, Activity activity, MainActivity mainActivityObject, Camera camera){
        super(context);

        startAutoExposure();

        this.mainActivity = activity;
        this.mainActivityObject = mainActivityObject;
        this.backgroundBtn = (ImageButton) this.mainActivity.findViewById(R.id.backgroundBtn);
        this.savedPic = (TextView) this.mainActivity.findViewById(R.id.saved);
        mCamera = camera;
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        mCamera.setDisplayOrientation(90);
        setCameraParameters();
        this.dataBase = MainActivity.getDataBase();
        this.cameraNull = false;
        this.backgroundBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                refreshBackground = true;
            }
        });
        initiateOpenCV();
        this.cppViews = new ArrayList<>();
        this.openCVViews = new ArrayList<>();
        this.actualHandFeatures = new ArrayList<>();
        this.allHandFeatures = new ArrayList<>();
        this.recognisedUsers = new ArrayList<>();
        this.usersWithTraits = new HashMap<>();
        this.frame = new Frame();
        this.backgroundFrame = new Frame();
        this.handRecognizer = new HandRecognizer(new Normalizer());
        setAllViewsToVariables();
        setCppViewsList();
        setOpenCVViewsList();
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            if(cameraNull == true){
                openCameraAndSetParameters();
                cameraNull = false;
            }
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

                if (measureCameraTime) { // odliczanie czasu do zablokowania automatycznej ekpozycji kamery
                    //System.out.println("Diff time: " + (System.nanoTime() - startTime) + " > " + CameraView.autoAdjustmentTime);
                    if (System.nanoTime() - startTime > CameraView.autoAdjustmentTime) {
                        lockCameraExposure(true);
                        measureCameraTime = false;
                    }
                }

                if(frames == 5) {
                    //number of processed pictures
                    ++pictureSaved;
                    savedPic.setText(pictureSaved + " processed. Good " + HandFeatures.foundedHandsFeatures);

                    segmentateImagesGivenAsBytes(data);
                    findHandFeaturesFromSegmentatedHands();
                    recognizeUser();
                    if(recognisedUsers.size() != 0) {
                        mCamera.stopPreview();
                        System.out.println(recognisedUsers.get(0));
                        mainActivityObject.pushFoundUserToScreen(recognisedUsers, actualHandFeatures);
                        recognisedUsers = new ArrayList<>();
                    }

                    //set frames to 0 (return to the beginning of loop)
                    frames = 0;
                }
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
        startParameters = parameters; // zapamietuje poczatkowe ustawienia kamery
        mCamera.setParameters(parameters);
        this.size = parameters.getPreviewSize();
    }

    public void lockCameraExposure(boolean lock){
        Camera.Parameters parameters = mCamera.getParameters();

        // chcemy zablokowac automatyczna ekspozycje siatla aparatu
        if (lock == true) {
            if (parameters.getAutoExposureLock() == false) { // ekspozycja swiatla automatyczna
                System.out.println("Ekspozycja swiatła automatyczna. Wylaczono");
                parameters.setAutoExposureLock(true); // zabllkuj ekspozycje swiatla
            }
            if (parameters.getAutoWhiteBalanceLock() == false) { // balans bieli automatyczny
                System.out.println("Balans bielie automatyczny. Wylaczono");
                parameters.setAutoWhiteBalanceLock(true); // zablokuj
            }
            //parameters.set("iso", 800);
        }
        else {
            // przywraca poczatkowe ustaiania kamery
            mCamera.setParameters(startParameters);
        }

        mCamera.setParameters(parameters);
    }


    /**
     * Process segmentation of data from camera preview, sets results to ImageViews
     * @param data byte Array
     */
    public void segmentateImagesGivenAsBytes(byte[] data){
        frame.setActualFrame(data);

        Bitmap liveViewBitmap = frame.getActualBitmap();
        setImageToImageView(bottomCenter, liveViewBitmap);

        frame.setBackground(bmpBackground);
        frame.setThresholds(15, 15, 1);
        frame.segmentateFrameWithOpenCV();
        List<Bitmap> openCVBitmaps = frame.getOpenCVBitmaps();
        setBitmapsToViews(openCVViews, openCVBitmaps);

        //CopyManager.saveBitmapToDisk(openCVBitmaps, pictureSaved, "OpenCV");
    }

    public void findHandFeaturesFromSegmentatedHands(){
        //actualHandFeatures.clear();
        frame.findHandFeatures();
        //this.actualHandFeatures = frame.getHandFeatures();
        for (float[] features:frame.getHandFeatures()
             ) {
            this.allHandFeatures.add(features);
            this.actualHandFeatures.add(features);
        }
    }

    public void recognizeUser(){
        getAllUsersWithTraits();
        if (actualHandFeatures.size() > 2 * frame.getHandFeatures().size()) {
            recognisedUsers = handRecognizer.recognise(actualHandFeatures.get(0).clone(), usersWithTraits);
            actualHandFeatures.clear();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        //mCamera.stopPreview();

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
        this.refreshBackground = false;
        actualHandFeatures.clear();
        System.out.println("Pobrano nową próbkę tła...");
    }

    protected Boolean isGetBackgroundButtonClicked(){
        return this.refreshBackground;
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
        for (Bitmap bitmap:bitmaps) {
            setImageToImageView(views.get(counter), bitmap);
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

    public void saveFeaturesToFile(){
        if(actualHandFeatures != null)
            CopyManager.saveHandFeaturesToTxt(allHandFeatures, "HandFeatures-3");
    }

    private void getAllUsersWithTraits(){
        this.dataBase = MainActivity.getDataBase();
        usersWithTraits = this.dataBase.getAllUsersWithTraits();
    }

    public void setMarkerCameraNull(){
        cameraNull = true;
    }

    private void openCameraAndSetParameters(){
        try{
            mCamera.release();
            mCamera = null;
            mCamera = Camera.open(1);//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
        mCamera.setDisplayOrientation(90);
        setCameraParameters();
    }

    public void startPreviewInCameraView(){
        mCamera.startPreview();
    }

    public void stopPreviewInCameraView(){
        mCamera.stopPreview();
    }

    public void startAutoExposure(){
        measureCameraTime = true;
        startTime = System.nanoTime();
    }
}

