package com.zucc.androocv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoadImageActivity extends AppCompatActivity {
    private static String TAG = "Load Image";
    Button galleryBtn, saveBtn, flipVerti, flipHori;
    ImageView previewImg;
    Uri imageUri;
    Mat resultImage;
    Bitmap bitmap, resultBitmap;
    int i, j, k;
    static {
        if (OpenCVLoader.initDebug()){
            Log.i("OpenCV", "OpenCV Loaded Successfully");
        } else {
            Log.i("OpenCV", "OpenCV not Loaded");
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);

        galleryBtn = findViewById(R.id.btn_gallery);
        flipHori = findViewById(R.id.btn_hori);
        flipVerti = findViewById(R.id.btn_verti);
        saveBtn = findViewById(R.id.btn_save);
        previewImg = findViewById(R.id.image_preview);

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        });

        flipVerti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = BitmapFactory.decodeStream(inputStream);
                flipVertical(bitmap);
            }
        });

        flipHori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmap = BitmapFactory.decodeStream(inputStream);
                flipHorizontal(bitmap);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                String strDate = "hh:mm:ss a";
                DateFormat dateFormat = new SimpleDateFormat(strDate);
                String formattedDate = dateFormat.format(date);
                String fileName = "/image_" + formattedDate + ".PNG";
                File file = new File(new File("/sdcard/DCIM"), fileName);
                if (file.exists()){
                    file.delete();
                }
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Log.i("Saving Image","Success");
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Mat sampledImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                Utils.bitmapToMat(bitmap, sampledImage);

                Size sizeMat = sampledImage.size();
                String[] matrix = new String[(int) sizeMat.width];
                for (i=0;i<sizeMat.height;i++){
                    matrix[i] = "| ";
                    for (j=0;j<sizeMat.width;j++){
                        double[] dataMat = sampledImage.get(i,j);
                        matrix[i] = matrix[i]+""+dataMat[0]+", "+dataMat[1]+", "+dataMat[2]+" | ";
                    }
                    Log.i("Matrix Image",""+matrix[i]);
                    simpanMatriks(matrix[i], "asli");
                }
                Log.i(TAG, "Load Image Successfully");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void flipVertical (Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat sampledImage = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, sampledImage);

        Size sizeMat = sampledImage.size();
        resultImage = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

//                Log.i(TAG, ""+dataMat[0]);
        double[] dataFlip = new double[0];
        i=0;
        k=0;
        String[] matrix = new String[(int) sizeMat.width];
        for (i= (int) (sizeMat.height - 1); i>=0; i--){
            matrix[i] = "| ";
            for (j=0;j<sizeMat.width;j++){
                dataFlip = sampledImage.get(i,j);
                matrix[i] = matrix[i]+""+dataFlip[0]+", "+dataFlip[1]+", "+dataFlip[2]+" | ";
                resultImage.put(k,j,dataFlip);

            }
            k++;
            Log.i("Matrix Image",""+matrix[i]);
            simpanMatriks(matrix[i], "vertikal");
        }
        Utils.matToBitmap(resultImage, resultBitmap);
        previewImg.setImageBitmap(resultBitmap);

        Log.i(TAG, "Flip Image Vertical Success");
    }

    public void flipHorizontal (Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat sampledImage = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, sampledImage);

        Size sizeMat = sampledImage.size();
        resultImage = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

//                Log.i(TAG, ""+dataMat[0]);
        double[] dataFlip = new double[0];
        i=0;
        String[] matrix = new String[(int) sizeMat.width];
        for (i=0; i<sizeMat.height; i++){
            k=0;
            matrix[i] = "| ";
            for (j= (int) (sizeMat.width - 1); j>=0; j--){
                dataFlip = sampledImage.get(i,j);
                matrix[i] = matrix[i]+""+dataFlip[0]+", "+dataFlip[1]+", "+dataFlip[2]+" | ";
                resultImage.put(i,k,dataFlip);
                k++;
            }
            Log.i("Matrix Image",""+matrix[i]);
            simpanMatriks(matrix[i],"horizontal");
        }
        Utils.matToBitmap(resultImage, resultBitmap);
        previewImg.setImageBitmap(resultBitmap);

        Log.i(TAG, "Flip Image Horizontal Success");
    }

    public void simpanMatriks(String matriks, String asli) {
        Date date = new Date();
        String strDate = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDate);
        String formattedDate = dateFormat.format(date);

        try {
            if (asli=="asli") {
                String fileName = "/matriks_asli_" + formattedDate + ".txt";
                File logMatriks = new File(new File("sdcard/matrix"),fileName);

                BufferedWriter buf = new BufferedWriter(new FileWriter(logMatriks,true));
                buf.append(matriks);
                buf.newLine();
                buf.close();
            } else if (asli=="vertikal") {
                String fileName = "/matriks_flipVertikal_" + formattedDate + ".txt";
                File logMatriks = new File(new File("sdcard/matrix"),fileName);

                BufferedWriter buf = new BufferedWriter(new FileWriter(logMatriks,true));
                buf.append(matriks);
                buf.newLine();
                buf.close();
            } else if (asli=="horizontal") {
                String fileName = "/matriks_flipHorizontal_" + formattedDate + ".txt";
                File logMatriks = new File(new File("sdcard/matrix"),fileName);

                BufferedWriter buf = new BufferedWriter(new FileWriter(logMatriks,true));
                buf.append(matriks);
                buf.newLine();
                buf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
