package com.app.checkpresence;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

public class AddUserActivity extends Activity {

    private Camera mCamera = null;
    private AddUserCameraView mCameraView = null;
    private Context context;
    private LayoutInflater li;
    private View promptsView;
    private AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        openCamera();

        if(mCamera != null) {
            mCameraView = new AddUserCameraView(this, this, this, mCamera);//create a SurfaceView to show camera data
            FrameLayout add_user_camera_view = (FrameLayout)findViewById(R.id.add_user_camera_view);
            add_user_camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }
    }

    public void openCamera(){
        try{
            mCamera = Camera.open(1);//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed(){
        //mCameraView.saveFeaturesToFile();
        mCamera.stopPreview();
        //mCamera.release();
        this.closeActivity();
    }

    public void addUserData(){
        MyAlertDialog myAlertDialog = new MyAlertDialog();
        myAlertDialog.setAddUserActivity(this);
        myAlertDialog.setAddUserCameraView(mCameraView);
        myAlertDialog.setContext(this);
        myAlertDialog.addUserData();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void closeActivity(){
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        finish();
    }
}
