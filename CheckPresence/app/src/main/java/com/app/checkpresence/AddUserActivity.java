package com.app.checkpresence;

import android.app.Activity;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

public class AddUserActivity extends Activity {

    private Camera mCamera = null;
    private AddUserCameraView mCameraView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        try{
            mCamera = Camera.open(1);//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new AddUserCameraView(this, this, mCamera);//create a SurfaceView to show camera data
            FrameLayout add_user_camera_view = (FrameLayout)findViewById(R.id.add_user_camera_view);
            add_user_camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }
    }

    @Override
    public void onBackPressed(){
        //mCameraView.saveFeaturesToFile();
        mCamera.release();
    }


}
