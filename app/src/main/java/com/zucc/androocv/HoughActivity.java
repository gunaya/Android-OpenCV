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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class HoughActivity extends AppCompatActivity {
    Button lineDet, circleDet, loadImage;
    ImageView previewImg, prev2;
    Uri imageUri;
    Bitmap bitmap;
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

        loadImage  = findViewById(R.id.btn_get);
        lineDet = findViewById(R.id.btn_line);
        circleDet = findViewById(R.id.btn_circle);
        previewImg = findViewById(R.id.image_preview);

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
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = BitmapFactory.decodeStream(inputStream);
                LineDetection(bitmap);
            }
        });

        circleDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = BitmapFactory.decodeStream(inputStream);
                CircleDetection(bitmap);
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
                previewImg.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void LineDetection(Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);
//        Imgproc.blur(greyImg, greyImg, new Size(10,10));

        Imgproc.Canny(greyImg,greyImg,80, 100, 3);
        Mat lines = new Mat();
//        Imgproc.HoughLinesP(greyImg, lines, 1, Math.PI/180, threshold, minLineSize, lineGap);
//
//
//        for (int x = 0; x< lines.rows(); x++){
//            double[] vec = lines.get(x,0);
//            double x1 = vec[0],
//                    y1 = vec[1],
//                    x2 = vec[2],
//                    y2 = vec[3];
//            Point start = new Point(x1, y1);
//            Point end = new Point(x2, y2);
//
//            Core.line(initImg, start, end, new Scalar(255,0,0),3);
//
//        }

        Imgproc.HoughLines(greyImg, lines,1,Math.PI/180, threshold);
        for(int i = 0; i<lines.cols();i++) {
            double data[] = lines.get(0,i);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta*rho1;
            double y0 = sinTheta*rho1;
            Point pt1 = new Point(x0+2000*(-sinTheta), y0+2000*cosTheta);
            Point pt2 = new Point(x0-2000*(-sinTheta), y0-2000*cosTheta);

            Core.line(initImg, pt1, pt2, new Scalar(139,0,0,255),2);
        }
        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }

    public void CircleDetection(Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        initImg = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, initImg);

        //ubah ke greyscale
        greyImg = new Mat();
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
        Bitmap bmp = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(), Bitmap.Config.ARGB_8888);

        Imgproc.blur(greyImg, greyImg, new Size(10,10));
        Imgproc.Canny(greyImg,greyImg,80, 100, 3);
        Mat circles = new Mat();

        Imgproc.HoughCircles(greyImg, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 100, 0,500);
        for (int i=0;i<circles.cols();i++){
            double[] vCircle = circles.get(0,i);
            Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int)Math.round(vCircle[2]);

            Core.circle(initImg, pt, radius, new Scalar(139, 0,0, 255), 2);
        }
        Utils.matToBitmap(initImg, bmp);
        previewImg.setImageBitmap(bmp);
    }
}
