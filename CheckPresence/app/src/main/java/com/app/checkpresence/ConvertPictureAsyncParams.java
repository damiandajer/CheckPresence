package com.app.checkpresence;

public class ConvertPictureAsyncParams{
    public int[] argb;
    int [] inputColorSegmentationDataPicture;
    int height;
    int width;
    int warunek;

    ConvertPictureAsyncParams(int[] argb, int [] inputColorSegmentationDataPicture, int height, int width, int warunek){
        this.argb = argb;
        this.inputColorSegmentationDataPicture = inputColorSegmentationDataPicture;
        this.height = height;
        this.width = width;
        this.warunek = warunek;
    }
}
