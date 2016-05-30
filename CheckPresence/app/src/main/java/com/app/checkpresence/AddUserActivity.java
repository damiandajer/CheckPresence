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
        mCamera.release();
    }

    public void addUserData(){
        context = this;
        li = LayoutInflater.from(context);
        promptsView = li.inflate(R.layout.get_user_data, null);

        alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInputFirstName = (EditText) promptsView
                .findViewById(R.id.EditTextGetFirstName);
        final EditText userInputSecondName = (EditText) promptsView
                .findViewById(R.id.EditTextGetSecondName);
        final EditText userInputIndex = (EditText) promptsView
                .findViewById(R.id.EditTextGetIndex);
        final EditText userInputGroup = (EditText) promptsView
                .findViewById(R.id.EditTextGetGroupName);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                mCameraView.setFirstName(userInputFirstName.getText().toString());
                                mCameraView.setSecondName(userInputSecondName.getText().toString());
                                mCameraView.setIndexUser(Integer.valueOf(userInputIndex.getText().toString()));
                                mCameraView.setGroupName(userInputGroup.getText().toString());
                                mCameraView.createUser();
                                dialog.cancel();
                                //finish();
                                closeActivity();
                                //mCameraView.closeActivity();
                                //createNewCategory(nameOfNewCategory);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                closeActivity();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void onDestroy(){
        //mCameraView.closeActivity();
        //mCamera.setPreviewCallback(null);
        //mCamera.stopPreview();
        //mCamera.release();
        //mCamera = null;
        super.onDestroy();
    }

    public void closeActivity(){
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        //mCamera = null;
        finish();
    }
}
