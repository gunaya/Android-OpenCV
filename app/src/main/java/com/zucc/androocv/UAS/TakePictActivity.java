package com.zucc.androocv.UAS;

import android.content.Context;
import android.media.MediaActionSound;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zucc.androocv.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TakePictActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "Circular Tracking";
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat input, circles, gray, canny;
    Mat imRGB, imHSV, imThreshold1, imThreshold2, imThresholded, array255, distance;
    Button okBtn;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV Loaded Successfully");
                    cameraBridgeViewBase.enableView();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pict);
        cameraBridgeViewBase = findViewById(R.id.javacam);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
//        cameraBridgeViewBase.setMaxFrameSize(1080,2160);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "There is a problem in OpenCV", Toast.LENGTH_LONG).show();
        } else {
            mLoaderCallback.onManagerConnected(mLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        imRGB = new Mat(height, width, CvType.CV_8UC4);
        imHSV = new Mat(height, width, CvType.CV_8UC4);
        array255 = new Mat(height, width, CvType.CV_8UC1);
        imThreshold1 = new Mat(height, width, CvType.CV_8UC1);
        imThreshold2 = new Mat(height, width, CvType.CV_8UC1);

    }

    @Override
    public void onCameraViewStopped() {
        imRGB.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        input = inputFrame.gray();

        circles = new Mat();
        canny = new Mat();

        Imgproc.blur(input, input, new Size(7,7), new Point(2,2));
        Imgproc.Canny(input, canny,50,150);
        Log.i("src","blur");

        Imgproc.HoughCircles(canny, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 145, 100, 100, 0,200);
        Log.i("src", String.valueOf("size: "+circles.cols())+". "+String.valueOf(circles.rows()));

        if (circles.cols() > 0 ) {
            for (int x = 0; x < Math.min(circles.cols(), 5); x++ ){
                double circleVec[] = circles.get(0,x);

                if (circleVec == null){
                    break;
                }

                Point center = new Point(circleVec[0], circleVec[1]);
                int radius = (int) circleVec[2];

                Core.circle(input, center, 3, new Scalar(255,0,0,255),5);
                Core.circle(input, center, radius, new Scalar(255,0,0,255), 2);
            }
        }
        circles.release();
        return inputFrame.rgba();

    }


}
