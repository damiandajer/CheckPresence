package com.app.checkpresence.backgroundmenage;

import android.hardware.Camera;

/**
 * Created by bijat on 12.06.2016.
 */
final public class CameraParameters {
    public static Camera.Size initCameraParameters(Camera camera){
        if (CameraParameters.m_parameters != null)
            CameraParameters.m_parameters.getPreviewSize();

        CameraParameters.m_parameters = camera.getParameters();
        CameraParameters.m_parameters.set("orientation", "portrait");
        CameraParameters.m_parameters.setRotation(90);
        Camera.Size size = CameraParameters.m_parameters.getPreviewSize();
        CameraParameters.m_parameters.setPreviewSize(size.width / 2, size.height / 2);
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
