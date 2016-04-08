package com.app.checkpresence;

/**
 * Created by Damian on 04.04.2016.
 */

        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Rect;
        import android.graphics.YuvImage;
        import android.hardware.Camera;
        import android.os.Environment;
        import android.util.Log;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.nio.channels.FileChannel;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int frames = 0;
    private int pictureSaved = 0;
    private TextView savedPic;

    public CameraView(Context context, Camera camera, TextView saved){
        super(context);

        this.savedPic = saved;
        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        //this.saved = (TextView) findViewById(R.id.saved);
        //this.saved.setText("0 saved");
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
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if(mHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
        }

        //now, recreate the camera preview
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }


            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera _camera) {
                    ++frames;
                    if(frames == 10) {
                        ++pictureSaved;
                        //Log.d("surfaceChanged",String.format("Got %d bytes of camera data", _data.length));
                        //System.out.println("Got bytes of camera data: " + data.length);
                        //Bitmap previewBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();
                        parameters.set("orientation", "portrait");
                        parameters.setRotation(90);
                        mCamera.setParameters(parameters);

                        YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                                size.width, size.height, null);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        image.compressToJpeg(
                                new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                                out);

                        byte[] imageBytes = out.toByteArray();
                        Bitmap imageBmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                        addCopy(imageBmp);

                        savedPic.setText(pictureSaved + " saved");
                        frames = 0;
                    }
                }
            });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
    }

    public void addCopy(Bitmap image){
        
        FileOutputStream out = null;
        File sd = Environment.getExternalStorageDirectory();
        String backupDBPath = "backupBMP/backupImage.bmp";
        File backupImage = new File(sd, backupDBPath);
        System.out.println(backupImage.toString());
        if(!backupImage.exists()){
            System.out.println("Tworzę backupDB");
            try {
                backupImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(backupImage.exists()) {
            try {
                out = new FileOutputStream(backupImage);
                image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                /*image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                        out);*/
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
