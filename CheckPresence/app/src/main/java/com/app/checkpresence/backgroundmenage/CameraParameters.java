package com.app.checkpresence.backgroundmenage;

import android.hardware.Camera;

import com.app.checkpresence.CameraView;

import java.util.List;

/**
 * Created by bijat on 12.06.2016.
 */
final public class CameraParameters {
    final private static int PREVIEW_WIDTH = 440;

    public static Camera.Size initCameraParameters(Camera camera){
        if (CameraParameters.m_parameters != null)
            CameraParameters.m_parameters.getPreviewSize();

        CameraParameters.m_parameters = camera.getParameters();
        CameraParameters.m_parameters.set("orientation", "portrait");
        CameraParameters.m_parameters.setRotation(90);
        List<Camera.Size> sizes = CameraParameters.m_parameters.getSupportedPreviewSizes();
        int delta_width = Integer.MAX_VALUE;
        Camera.Size previewSize = null;
        for (Camera.Size size : sizes) {
            int temp = Math.abs(size.width - CameraParameters.PREVIEW_WIDTH);
            if (temp < delta_width) {
                previewSize = size;
                delta_width = temp;
            }
        }
        CameraParameters.m_parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(CameraParameters.m_parameters);

        CameraParameters.setParameters(camera.getParameters()); // zapamietuje poczatkowe ustawienia kamery

        return CameraParameters.m_parameters.getPreviewSize();
    }

    public static void setParameters(Camera.Parameters parameters) {
        CameraParameters.m_parameters = parameters;
    }

    public static Camera.Parameters getParameters() {
        return CameraParameters.m_parameters;
    }

    public static void realeaseCameraParameters() {
        CameraParameters.m_parameters = null;
    }

    private static Camera.Parameters m_parameters;
}
