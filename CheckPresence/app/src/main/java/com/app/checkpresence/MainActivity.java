package com.app.checkpresence;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.database.DataBase;
import com.app.memory.CopyManager;

import java.util.List;


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
            mCameraView = new CameraView(this, this, this, mCamera);//create a SurfaceView to show camera data
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

        Button databaseManagerButton = (Button) findViewById(R.id.databaseManagerButton);
        databaseManagerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startDatabaseManager();
            }
        });
        Button copyDatabaseButton = (Button) findViewById(R.id.loadDatabaseButton);
        copyDatabaseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCameraView.stopPreviewInCameraView();
                CopyManager.addCopyOfDatabase(context);
                mCameraView.startPreviewInCameraView();
            }
        });

        Button loadDatabaseButton = (Button) findViewById(R.id.copyDatabaseButton);
        loadDatabaseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCameraView.stopPreviewInCameraView();
                CopyManager.loadBackupOfDatabase(context);
                mCameraView.startPreviewInCameraView();
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

    @Override
    public void onResume(){
        super.onResume();
        //openCamera();
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

    public void startActivityAddNewUser(){
        if(mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mCameraView.setMarkerCameraNull();
        Intent intent = new Intent(this, AddUserActivity.class);
        startActivity(intent);
    }

    public void startDatabaseManager(){
        Intent intent = new Intent(this, AndroidDatabaseManager.class);
        startActivity(intent);
    }

    public void pushFoundUserToScreen(List<Integer> usersList){
        MyAlertDialog myAlertDialog = new MyAlertDialog();
        myAlertDialog.setMainActivity(this);
        myAlertDialog.setContext(this);
        myAlertDialog.setmCameraView(mCameraView);
        myAlertDialog.convertUsersListToListOfStrings(usersList);
        myAlertDialog.pushFoundUserToScreen();
    }
}
