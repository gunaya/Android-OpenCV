package com.zucc.androocv.UAS;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zucc.androocv.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class TrackActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "Tracking";
    private static final int    VIEW_MODE_RGBA   = 0;
    private static final int    VIEW_MODE_HSV   = 1;
    private static final int    VIEW_MODE_CANNY  = 2;
    private static final int    VIEW_MODE_FEATURES = 3;
    private static final int    VIEW_MODE_GRAY = 4;
    int mViewMode;
    Mat mRgba, mIntermediateMat ,mGray, mHSV, mThresholded, mThresholded2, array255, distance;
    MenuItem mItemPreviewRGBA, mItemPreviewGray, mItemPreviewCanny, mItemPreviewHSV;
    MenuItem mItemPreviewFeatures, mItemGray;
    CameraBridgeViewBase mCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_track);
        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
    }
    public TrackActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreate");
        mItemPreviewRGBA = menu.add("RGBA");
        mItemPreviewHSV = menu.add("HSV");
        mItemPreviewCanny = menu.add("Thresholded");
        mItemPreviewFeatures = menu.add("Ball");
        mItemPreviewGray = menu.add("Gray");
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mCameraView != null)
            mCameraView.disableView();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "There is a problem in OpenCV", Toast.LENGTH_LONG).show();
        } else {
            mLoaderCallback.onManagerConnected(mLoaderCallback.SUCCESS);
        }
    }
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mHSV = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        array255=new Mat(height,width,CvType.CV_8UC1);
        distance=new Mat(height,width, CvType.CV_8UC1);
        mThresholded=new Mat(height,width,CvType.CV_8UC1);
        mThresholded2=new Mat(height,width,CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        mRgba = inputFrame.rgba();

        //return nilai RGB
        if (viewMode==VIEW_MODE_RGBA) return mRgba;
        List<Mat> lhsv = new ArrayList<Mat>(3);
        Mat circles = new Mat(); // No need (and don't know how) to initialize it.
        // The function later will do it... (to a 1*N*CV_32FC3)
        array255.setTo(new Scalar(255));
        Scalar hsv_min = new Scalar(0, 50, 50, 0);
        Scalar hsv_max = new Scalar(6, 255, 255, 0);
        Scalar hsv_min2 = new Scalar(175, 50, 50, 0);
        Scalar hsv_max2 = new Scalar(179, 255, 255, 0);
        //double[] data=new double[3];
        // One way to select a range of colors by Hue
        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV,4);

        //return nilai HSV
        if (viewMode==VIEW_MODE_HSV) return mHSV;
        Core.inRange(mHSV, hsv_min, hsv_max, mThresholded);
        Core.inRange(mHSV, hsv_min2, hsv_max2, mThresholded2);
        Core.bitwise_or(mThresholded, mThresholded2, mThresholded);
        /*Core.line(mRgba, new Point(150,50), new Point(202,200), new Scalar(100,10,10)CV_BGR(100,10,10), 3);
             Core.circle(mRgba, new Point(210,210), 10, new Scalar(100,10,10),3);
             data=mRgba.get(210, 210);
             Core.putText(mRgba,String.format("("+String.valueOf(data[0])+","+String.valueOf(data[1])+","+String.valueOf(data[2])+")"),new Point(30, 30) , 3 //FONT_HERSHEY_SCRIPT_SIMPLEX
                   ,1.0,new Scalar(100,10,10,255),3);*/
        // Notice that the thresholds don't really work as a "distance"
        // Ideally we would like to cut the image by hue and then pick just
        // the area where S combined V are largest.
        // Strictly speaking, this would be something like sqrt((255-S)^2+(255-V)^2)>Range
        // But if we want to be "faster" we can do just (255-S)+(255-V)>Range
        // Or otherwise 510-S-V>Range
        // Anyhow, we do the following... Will see how fast it goes...
        Core.split(mHSV, lhsv); // We get 3 2D one channel Mats
        Mat S = lhsv.get(1);
        Mat V = lhsv.get(2);
        Core.subtract(array255, S, S);
        Core.subtract(array255, V, V);
        S.convertTo(S, CvType.CV_32F);
        V.convertTo(V, CvType.CV_32F);
        Core.magnitude(S, V, distance);
        Core.inRange(distance,new Scalar(0.0), new Scalar(200.0), mThresholded2);
        Core.bitwise_and(mThresholded, mThresholded2, mThresholded);
 /*       if (viewMode==VIEW_MODE_CANNY){
             Imgproc.cvtColor(mThresholded, mRgba, Imgproc.COLOR_GRAY2RGB, 4);
             return mRgba;
        }*/

        if (viewMode==VIEW_MODE_CANNY){
            Imgproc.Canny(mThresholded, mThresholded, 500, 250); // This is not needed.
            // It is just for display
            Imgproc.cvtColor(mThresholded, mGray, Imgproc.COLOR_GRAY2RGB, 4);
            return mGray;
        }

        // Apply the Hough Transform to find the circles
        Imgproc.GaussianBlur(mThresholded, mThresholded, new Size(9,9),0,0);
        Imgproc.HoughCircles(mThresholded, circles, Imgproc.CV_HOUGH_GRADIENT, 2, mThresholded.height()/4, 500, 50, 0, 0);


        if (viewMode==VIEW_MODE_FEATURES) {

            //int cols = circles.cols();
            int rows = circles.rows();
            int elemSize = (int)circles.elemSize(); // Returns 12 (3 * 4bytes in a float)
            float[] data2 = new float[rows * elemSize/4];
            if (data2.length>0){
                circles.get(0, 0, data2); // Points to the first element and reads the whole thing
                // into data2
                for(int i=0; i<data2.length; i=i+3) {
                    Point center= new Point(data2[i], data2[i+1]);
                    Core.ellipse( mRgba, center, new Size((double)data2[i+2], (double)data2[i+2]), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );
                }
            }
            return mRgba;
        }
        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewHSV) {
            mViewMode = VIEW_MODE_HSV;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        }
        return true;
    }
}
