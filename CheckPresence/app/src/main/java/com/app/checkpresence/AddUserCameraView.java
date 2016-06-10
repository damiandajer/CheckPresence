package com.app.checkpresence;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.database.DataBase;
import com.app.handfeatures.HandFeaturesData;
import com.app.picture.Frame;
import com.app.recognition.HandRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Damian on 30.05.2016.
 */
public class AddUserCameraView extends SurfaceView implements SurfaceHolder.Callback {
    public static boolean refreshBackground = true;

    private long autoAdjustmentTime; // czas w ns, po jakim kamera zablokuje ekspozycje swiatla us
    private boolean measureCameraTime; // czy odliczac czas dla ustabilizowania kamery i jej blokady
    private long startTime; //punkt poczatkowy czasu. actualTime - startTime >= autoAdjustmentTime => zablokowanie kamery
    private Camera.Parameters cameraParameters; // poczatkowe ustawienia kamery, jezeli odblokujemy aparato to chcemy wlasnie do nich powrucic

    Activity mainActivity;
    AddUserActivity addUserActivity;
    protected SurfaceHolder mHolder;
    protected Camera mCamera;
    public static Camera.Size size;
    protected int frames = 1;
    protected int pictureSaved = 0;
    protected TextView savedPic;
    private ImageView bottomCenter;
    private Bitmap bmpBackground;
    private Frame frame, backgroundFrame;
    private List<float[]> actualHandFeatures, allHandFeatures;
    private HandRecognizer handRecognizer;
    private Map<Integer, List<float[]>> usersWithTraits;
    private DataBase dataBase;
    private List<String> recognisedUsers;
    private String firstName;
    private String secondName;
    private String groupName;
    private int indexUser, segmentatedHandsBufor;

    public AddUserCameraView(Context context, Activity activity, AddUserActivity addUserActivity, Camera camera){
        super(context);

        this.mainActivity = activity;
        this.addUserActivity = addUserActivity;
        this.savedPic = (TextView) this.mainActivity.findViewById(R.id.saved);
        mCamera = camera;
        startAutoExposure(3000);

        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        mCamera.setDisplayOrientation(90);
        initCameraParameters();
        this.dataBase = MainActivity.getDataBase();
        this.actualHandFeatures = new ArrayList<>();
        this.allHandFeatures = new ArrayList<>();
        this.recognisedUsers = new ArrayList<>();
        this.usersWithTraits = new HashMap<>();
        this.frame = new Frame();
        this.backgroundFrame = new Frame();
        this.handRecognizer = new HandRecognizer();
        setAllViewsToVariables();
        clearSegmentatedHandsBufor();
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
                    startAutoExposure(500);
                    getBackgroundFrame(data);
                }

                if (measureCameraTime) { // odliczanie czasu do zablokowania automatycznej ekpozycji kamery
                    //System.out.println("Diff time: " + (System.nanoTime() - startTime) + " > " + CameraView.autoAdjustmentTime);
                    if (System.nanoTime() - startTime > autoAdjustmentTime) {
                        lockCameraExposure(true);
                        measureCameraTime = false;
                    }
                }

                if(frames == 5) {
                    //number of processed pictures
                    ++pictureSaved;
                    //savedPic.setText(pictureSaved + " processed");

                    segmentateImagesGivenAsBytes(data);
                    findHandFeaturesFromSegmentatedHands();
                    if(checkIfAllFeatures()){
                        addUser();
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
    public void initCameraParameters(){
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.set("orientation", "portrait");
        parameters.setRotation(90);
        Camera.Size size = parameters.getPreviewSize();
        parameters.setPreviewSize(size.width / 2, size.height / 2);
        mCamera.setParameters(parameters);
        this.size = parameters.getPreviewSize();

        saveCameraParameters(); // zapamietuje poczatkowe ustawienia kamery
    }

    /**
     * set parameters to camera
     * @param parameters
     */
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
        cameraParameters = getCameraParameters();
    }

    public void loadCameraParameters() {
        setCameraParameters(cameraParameters);
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
    public void segmentateImagesGivenAsBytes(byte[] data){
        frame.setActualFrame(data);

        Bitmap liveViewBitmap = frame.getActualBitmap();
        setImageToImageView(bottomCenter, liveViewBitmap);

        frame.setBackground(bmpBackground);
        frame.setThresholds(20, 20, 1);
        frame.segmentateFrameWithOpenCV();

        //CopyManager.saveBitmapToDisk(openCVBitmaps, pictureSaved, "OpenCV");
    }

    public void findHandFeaturesFromSegmentatedHands(){
        frame.findHandFeatures();
        this.actualHandFeatures = frame.getHandFeatures();
        ++segmentatedHandsBufor;
        if(actualHandFeatures.size() != 0 && segmentatedHandsBufor > 2) {
            for (float[] features : frame.getHandFeatures()
                    ) {
                this.allHandFeatures.add(features);
            }
            clearSegmentatedHandsBufor();
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
        clearSegmentatedHandsBufor();
        System.out.println("Pobrano nową próbkę tła...");
    }

    protected Boolean isGetBackgroundButtonClicked(){
        return this.refreshBackground;
    }

    private void setAllViewsToVariables(){
        this.bottomCenter = (ImageView) this.mainActivity.findViewById(R.id.liveView);
    }

    public void setBitmapsToViews(List<ImageView> views, List<Bitmap> bitmaps){
        int counter = 0;
        for (ImageView view:views) {
            setImageToImageView(view, bitmaps.get(counter));
            ++counter;
        }
    }

    private boolean checkIfAllFeatures(){
        if(allHandFeatures.size() < 10){
            savedPic.setText(allHandFeatures.size() + " found");
            return false;
        }
        else
            return true;
    }

    private void addUser(){
        mCamera.stopPreview();
        addUserActivity.addUserData();
    }

    public void createUser(){
        User user = new User(firstName, secondName, indexUser, groupName, allHandFeatures);
        dataBase.insertUser(user);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setIndexUser(int indexUser) {
        this.indexUser = indexUser;
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
        lockCameraExposure(false);
        measureCameraTime = true;
        startTime = System.nanoTime();
        autoAdjustmentTime = milliseconds * 1000000L;
    }

    public void clearSegmentatedHandsBufor(){
        segmentatedHandsBufor = 0;
    }
}
