package com.zucc.androocv.UAS;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Gunaya on 10/11/2018.
 */

public class myJavaCam extends JavaCameraView implements android.hardware.Camera.PictureCallback{

    private static final String TAG = "Take Pict";
    private String mPictureFilename;

    public myJavaCam(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking Picture");
        this.mPictureFilename = fileName;
        mCamera.setPreviewCallback(null);

        mCamera.takePicture(null,null,this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving Bitmap");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        try {
            FileOutputStream fileOS = new FileOutputStream(mPictureFilename);
            fileOS.write(data);
            fileOS.close();

        } catch (IOException e) {
            Log.e(TAG, "Exception in PhotoCallback");
        }
    }
}
