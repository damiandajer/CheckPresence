package com.app.checkpresence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.app.database.DataBase;


public class MainActivity extends Activity {
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private static DataBase dataBase;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        openCamera();

        if(mCamera != null) {
            mCameraView = new CameraView(this, this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        //btn to close the application
        ImageButton imgClose = (ImageButton) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCamera != null)
                    mCamera.release();
                System.exit(0);
            }
        });

        Button addUserButton = (Button) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivityAddNewUser();
            }
        });

        openDB();
    }

    static {
        System.loadLibrary("native");
    }

    /**
     * Metoda tworzy i otwiera bazę danych
     */
    private void openDB() {
        dataBase = new DataBase(context);
        dataBase.open();

    }

    /**
     * Metoda zamyka aplikację i bazę danych
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataBase.close();
    }

    @Override
    public void onBackPressed(){
        //mCameraView.saveFeaturesToFile();
        if(mCamera != null)
            mCamera.release();
        System.exit(0);
    }

    public void openCamera(){
        try{
            mCamera = Camera.open(1);//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
    }

    public static DataBase getDataBase(){
        return dataBase;
    }

    private void startActivityAddNewUser(){
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        mCameraView.setMarkerCameraNull();
        Intent intent = new Intent(this, AddUserActivity.class);
        startActivity(intent);
    }
}
