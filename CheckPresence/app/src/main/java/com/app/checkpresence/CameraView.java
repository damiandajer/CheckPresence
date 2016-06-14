package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.checkpresence.backgroundmenage.CameraParameters;
import com.app.checkpresence.backgroundmenage.HandFeatureRaportManager;
import com.app.checkpresence.backgroundmenage.HandMatchingLevel;
import com.app.database.DataBase;
import com.app.handfeatures.HandFeaturesData;
import com.app.checkpresence.backgroundmenage.HandFeaturesRaport;
import com.app.measurement.AppExecutionTimes;
import com.app.measurement.ExecutionTimeName;
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

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private int noFoundedHandFeaturesInARow;

    private boolean refreshBackground;
    private long autoAdjustmentTime; // czas w ns, po jakim kamera zablokuje ekspozycje swiatla us
    private boolean measureCameraTime; // czy odliczac czas dla ustabilizowania kamery i jej blokady
    private long startTime; //punkt poczatkowy czasu. actualTime - startTime >= autoAdjustmentTime => zablokowanie kamery
    private Camera.Parameters cameraParameters; // poczatkowe ustawienia kamery, jezeli odblokujemy aparato to chcemy wlasnie do nich powrucic

    private long prevTimePreview; // pomocnicza zmienna do mierzenia czasu miedy pobraniem kolejnych klatek

    Activity mainActivity;
    MainActivity mainActivityObject;
    protected SurfaceHolder mHolder;
    protected Camera mCamera;
    public static Camera.Size size;
    protected int frames = 1;
    protected int pictureSaved = 0;
    protected TextView savedPic;
    private ImageView bottomRight, bottomLeft, topLeft, topCenter, topRight, bottomCenter, infoView, handImage;
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

        refreshBackground = true;

        this.mainActivity = activity;
        this.mainActivityObject = mainActivityObject;
        this.backgroundBtn = (ImageButton) this.mainActivity.findViewById(R.id.backgroundBtn);
        this.savedPic = (TextView) this.mainActivity.findViewById(R.id.saved);
        this.infoView = (ImageView) this.mainActivity.findViewById(R.id.infoView);
        this.handImage = (ImageView) this.mainActivity.findViewById(R.id.handImage);
        mCamera = camera;
        startAutoExposure(3000);

        prevTimePreview = System.nanoTime();

        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        mCamera.setDisplayOrientation(90);
        size = CameraParameters.initCameraParameters(camera); // ustawia poczatkowe parametry kamery
        this.dataBase = MainActivity.getDataBase();
        this.cameraNull = false;

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

        this.backgroundBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startAutoExposure(500);
                refreshBackground = true;
            }
        });
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
                if (refreshBackground) {
                    getBackgroundFrame(data);
                }


                if (measureCameraTime) { // odliczanie czasu do zablokowania automatycznej ekpozycji kamery
                    //System.out.println("Diff time: " + (System.nanoTime() - startTime) + " > " + CameraView.autoAdjustmentTime);
                    if (System.nanoTime() - startTime > autoAdjustmentTime) {
                        lockCameraExposure(true);
                        measureCameraTime = false;
                    }
                }

                if(measureCameraTime == true) {
                    infoView.setVisibility(VISIBLE);
                    infoView.setImageResource(R.drawable.poczekaj);
                }
                else
                    //infoView.setVisibility(INVISIBLE);
                    infoView.setImageResource(R.drawable.dopasuj);

                if(frames == 5) {
                    AppExecutionTimes.clear(); // czyscimy obecnie istniejace czasy
                    // czas pomiedzy pobraniem klatek do przetworzenia
                    long currTime = System.nanoTime();
                    AppExecutionTimes.add(prevTimePreview, currTime, ExecutionTimeName.BETWEEN_CAPTURE_CAMERA_PREVIEW);
                    prevTimePreview = currTime;

                    // poczatek czasu dla przetworzenia klatki
                    AppExecutionTimes.startTime(ExecutionTimeName.CAPTURE_CAMERA_PREVIEW_TO_CALCULATE); // rozpoczynamy mierzenie czasu dla tej nawy

                    //number of processed pictures
                    ++pictureSaved;
                    savedPic.setText(pictureSaved + " processed");

                    HandFeaturesRaport report = segmentateImagesGivenAsBytes(data);
                    HandFeatureRaportManager hfrm = new HandFeatureRaportManager(report);
                    refreshBackground = hfrm.isNeedToTakeNewBackground();

                    if (!refreshBackground && hfrm.isReadyToCalculateFeatures()) {
                        HandFeaturesRaport.CalculationRaport c_report = findHandFeaturesFromSegmentatedHands();
                        hfrm.add(c_report);
                        refreshBackground = hfrm.isNeedToTakeNewBackground();

                        if (Configure.SEARCH_USER_IN_DATABASE == true) { // Tomek - potrzebuje zeby nie blokowalo czasem aplikacji tylko caly czas przetwarzalo kolejne klatki
                            recognizeUser();
                            if (recognisedUsers.size() != 0) {
                                //mCamera.stopPreview();
                                stopPreview();
                                System.out.println(recognisedUsers.get(0));
                                mainActivityObject.pushFoundUserToScreen(recognisedUsers, actualHandFeatures);
                                recognisedUsers = new ArrayList<>();
                            }
                        }
                    }

                    // pobranie stopnia dopasowania dloni do konturu
                    HandMatchingLevel level = hfrm.getMatchingLevel();
                    setProperColorOfContour(level);
                    

                    //set frames to 0 (return to the beginning of loop)
                    frames = 0;

                    AppExecutionTimes.endTime(ExecutionTimeName.CAPTURE_CAMERA_PREVIEW_TO_CALCULATE); // konczymy liczyc czas dla tej nazwy
                    if (Configure.SHOW_MEASURED_TIMES == true)
                        AppExecutionTimes.show(true);
                }
                ++frames;
            }
        });
    }

    public void setCameraParameters(Camera.Parameters parameters) {
        mCamera.setParameters(parameters);
    }

    public Camera.Parameters getCameraParameters() {
        return mCamera.getParameters();
    }

    /**
     * save camera parameters for feture backup last parameters
     */
    public void saveCameraParameters() {
        CameraParameters.setParameters(getCameraParameters());
    }

    public void loadCameraParameters() {
        setCameraParameters(CameraParameters.getParameters());
    }

    public void lockCameraExposure(boolean lock){
        boolean changed = false;
        Camera.Parameters parameters = getCameraParameters();

        // chcemy zablokowac automatyczna ekspozycje siatla aparatu
        if (lock) {
            if (!parameters.getAutoExposureLock()) { // ekspozycja swiatla automatyczna
                System.out.println("Ekspozycja swiatła automatyczna. Wylaczono");
                parameters.setAutoExposureLock(true); // zabllkuj ekspozycje swiatla
                changed = true;
            }
            if (!parameters.getAutoWhiteBalanceLock()) { // balans bieli automatyczny
                System.out.println("Balans bielie automatyczny. Wylaczono");
                parameters.setAutoWhiteBalanceLock(true); // zablokuj
                changed = true;
            }
        }
        else {
            if (parameters.getAutoExposureLock()) { // ekspozycja swiatla automatyczna
                System.out.println("Ekspozycja swiatła automatyczna. Wylaczono");
                parameters.setAutoExposureLock(false); // zabllkuj ekspozycje swiatla
                changed = true;
            }
            if (parameters.getAutoWhiteBalanceLock()) { // balans bieli automatyczny
                System.out.println("Balans bielie automatyczny. Wylaczono");
                parameters.setAutoWhiteBalanceLock(false); // zablokuj
                changed = true;
            }
            //parameters.set("iso", 800);
        }

        if (changed) {
            setCameraParameters(parameters);
            saveCameraParameters();
        }
    }


    /**
     * Process segmentation of data from camera preview, sets results to ImageViews
     * @param data byte Array
     */
    public HandFeaturesRaport segmentateImagesGivenAsBytes(byte[] data){
        AppExecutionTimes.startTime(ExecutionTimeName.SEGMENTATE_IMAGE_THREAD);

        frame.setActualFrame(data);

        Bitmap liveViewBitmap = frame.getActualBitmap();
        setImageToImageView(bottomCenter, liveViewBitmap);

        frame.setBackground(bmpBackground);
        frame.setThresholds(15, 15, 1);
        HandFeaturesRaport report = frame.segmentateFrameWithOpenCV();
        List<Bitmap> openCVBitmaps = frame.getOpenCVBitmaps();
        setBitmapsToViews(openCVViews, openCVBitmaps);

        //CopyManager.saveBitmapToDisk(openCVBitmaps, pictureSaved, "OpenCV");

        AppExecutionTimes.endTime(ExecutionTimeName.SEGMENTATE_IMAGE_THREAD);

        return report;
    }

    public HandFeaturesRaport.CalculationRaport findHandFeaturesFromSegmentatedHands(){
        AppExecutionTimes.startTime(ExecutionTimeName.HAND_FEATURE_THREAD);

        //actualHandFeatures.clear();
        HandFeaturesRaport.CalculationRaport report = frame.findHandFeatures();
        //this.actualHandFeatures = frame.getHandFeatures();
        for (float[] features:frame.getHandFeatures()
                ) {
            this.allHandFeatures.add(features);
            this.actualHandFeatures.add(features);
        }

        AppExecutionTimes.endTime(ExecutionTimeName.HAND_FEATURE_THREAD);
        return report;
    }

    public void recognizeUser(){
        AppExecutionTimes.startTime(ExecutionTimeName.USER_RECOGNICE_THREAD);

        getAllUsersWithTraits();
        if (actualHandFeatures.size() > 2 * frame.getHandFeatures().size()) {
            recognisedUsers = handRecognizer.recognise(actualHandFeatures.get(0).clone(), usersWithTraits);
            actualHandFeatures.clear();
        }

        AppExecutionTimes.endTime(ExecutionTimeName.USER_RECOGNICE_THREAD);
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
        if (cameraParameters == null) {
            CameraParameters.initCameraParameters(mCamera);
            startAutoExposure(200);
        }
        else
            loadCameraParameters();
    }

    public void startPreview(){
        loadCameraParameters();
        mCamera.startPreview();
    }

    public void stopPreview(){
        saveCameraParameters();
        mCamera.stopPreview();
    }

    public void startAutoExposure(int milliseconds){
        if (mCamera == null)
            return;

        lockCameraExposure(false);
        measureCameraTime = true;
        startTime = System.nanoTime();
        autoAdjustmentTime = milliseconds * 1000000L;
    }

    public void setProperColorOfContour(HandMatchingLevel level){
        if(level == HandMatchingLevel.NO) {
            handImage.setImageResource(R.drawable.bad_red_hand);
            infoView.setImageResource(R.drawable.dopasuj);
            infoView.setVisibility(VISIBLE);
        }
        else if(level == HandMatchingLevel.LOW) {
            handImage.setImageResource(R.drawable.bad_orange_hand);
            infoView.setVisibility(INVISIBLE);
        }
        else if(level == HandMatchingLevel.MATCHED) {
            handImage.setImageResource(R.drawable.good_hand);
            infoView.setVisibility(INVISIBLE);
        }
    }
}