package com.app.checkpresence;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class MainActivity extends Activity {
    //private static final String TAG = "OCV::Activity";
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    public TextView saved;
    public ImageView segmentatedHand1, segmentatedHand3, segmentatedHand4, segmentatedHand5, segmentatedHand6;
    public ImageView liveView;
    public Button backgroundBtn;
    private DataBase dataBase;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        saved = (TextView) findViewById(R.id.saved);
        segmentatedHand1 = (ImageView) findViewById(R.id.segmentatedHand1);
        segmentatedHand3 = (ImageView) findViewById(R.id.segmentatedHand3);
        segmentatedHand4 = (ImageView) findViewById(R.id.segmentatedHand4);
        segmentatedHand5 = (ImageView) findViewById(R.id.segmentatedHand5);
        segmentatedHand6 = (ImageView) findViewById(R.id.segmentatedHand6);
        liveView = (ImageView) findViewById(R.id.liveView);
        backgroundBtn = (Button) findViewById(R.id.backgroundBtn);

        try{
            mCamera = Camera.open(1);//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera, saved, segmentatedHand1, liveView,
                    segmentatedHand3, segmentatedHand4, segmentatedHand5, segmentatedHand6,
                    backgroundBtn);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        //btn to close the application
        ImageButton imgClose = (ImageButton) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        openDB();
        //System.out.println(myNativeCode());
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
        System.exit(0);
    }
}
