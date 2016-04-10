package com.app.checkpresence;

import android.hardware.Camera;

public class ConvertPictureAsyncParams{
    byte[] data;
    Camera.Parameters parameters;
    Camera.Size size;

    ConvertPictureAsyncParams(byte[] data, Camera.Parameters parameters, Camera.Size size){
        this.data = data;
        this.parameters = parameters;
        this.size = size;
    }
}
