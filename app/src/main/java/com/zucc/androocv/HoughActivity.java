package com.zucc.androocv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HoughActivity extends AppCompatActivity {
    Button lineDet,linePDet, circleDet, loadImage, resetImage, compLabel, ellipseDet;
    ImageView previewImg;
    Uri imageUri;
    Bitmap bitmap, bitmapReset;
    Mat initImg, greyImg;
    int threshold  =200, minLineSize = 20, lineGap = 20;
    static {
        if (OpenCVLoader.initDebug()){
            Log.i("OpenCV", "OpenCV Loaded Successfully");
        } else {
            Log.i("OpenCV", "OpenCV not Loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hough);

        resetImage = findViewById(R.id.btn_reset);
        loadImage  = findViewById(R.id.btn_get);
        lineDet = findViewById(R.id.btn_line);
        circleDet = findViewById(R.id.btn_circle);
        previewImg = findViewById(R.id.image_preview);
        compLabel = findViewById(R.id.btn_labelling);
        linePDet = findViewById(R.id.btn_linep);
        ellipseDet = findViewById(R.id.btn_ellipse);

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        });

        lineDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LineDetection(bitmap);
            }
        });

        circleDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CircleDetection(bitmap);
            }
        });

        compLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComponentsLabelling(bitmap);
            }
        });

        linePDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LineP(bitmap);
            }
        });

        resetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = bitmapReset.copy(Bitmap.Config.ARGB_8888, true);
                previewImg.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100) {
            imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                bitmapReset = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                previewImg.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void LineDetection(Bitmap bitmap) {
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);

        Imgproc.GaussianBlur(greyImg, greyImg,new Size(5,5),0);
        Imgproc.Canny(greyImg,greyImg,80, 100, 1);

        Mat lines = new Mat();

        Imgproc.HoughLines(greyImg, lines,1,Math.PI/180, 150);
        for(int i = 0; i<lines.cols();i++) {
            double data[] = lines.get(0,i);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta*rho1;
            double y0 = sinTheta*rho1;
            Point pt1 = new Point(x0+1000*(-sinTheta), y0+1000*cosTheta);
            Point pt2 = new Point(x0-1000*(-sinTheta), y0-1000*cosTheta);

            Core.line(initImg, pt1, pt2, new Scalar(255,0,0,255),2);
        }
        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }

    public void LineP(Bitmap bitmap) {
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);

        Imgproc.GaussianBlur(greyImg, greyImg,new Size(5,5),0);
        Imgproc.Canny(greyImg,greyImg,80, 100, 1);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(greyImg, lines, 1, Math.PI/180, 150, 1, 200);
        for (int x = 0; x< lines.rows(); x++){
            double[] vec = lines.get(x,0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);

            Core.line(initImg, start, end, new Scalar(255,0,0, 255),3);
        }
        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }

    public void CircleDetection(Bitmap bitmap) {
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);

        Imgproc.GaussianBlur(greyImg, greyImg,new Size(9,9),0);
        Imgproc.Canny(greyImg,greyImg,100, 100, 3);

        Mat circles = new Mat();

        Imgproc.HoughCircles(greyImg, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 145, 100, 100, 0,200);
        for (int i=0;i<circles.cols();i++){
            double[] vCircle = circles.get(0,i);
            Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int)Math.round(vCircle[2]);

            Core.circle(initImg, pt, radius+1, new Scalar(255, 0,0, 255), 3);
        }
        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }

    public void EllipseDetection(Bitmap bitmap) {
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);

        Imgproc.GaussianBlur(greyImg, greyImg,new Size(9,9),0);
        Imgproc.Canny(greyImg,greyImg,100, 100, 3);

        Mat circles = new Mat();

        Imgproc.HoughCircles(greyImg, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 145, 100, 100, 0,200);
        for (int i=0;i<circles.cols();i++){
            double[] vCircle = circles.get(0,i);
            Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int)Math.round(vCircle[2]);

            Core.circle(initImg, pt, radius+1, new Scalar(255, 0,0, 255), 3);

        }
        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }

    public void ComponentsLabelling(Bitmap bitmap){
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);

        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);
        Imgproc.Canny(greyImg,greyImg,100, 100, 3);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(greyImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for(int idx=0; idx< contours.size(); idx++) {
            Imgproc.drawContours(initImg, contours, idx, new Scalar(255,0,0,255), -1);
        }

        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }
}
