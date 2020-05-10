package com.example.rollcall.Camera;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class QrCodeDetector {

    private final static String TAG = "QrCodeDetector";

    public void detect(Bitmap bitmap) {
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat (bmp32.getHeight(), bmp32.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp32, mat);
        Log.d(TAG, mat.toString());

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(mat, mat, 11, 17, 17);
        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 0);
        Imgproc.Canny(mat, mat, 30, 200);

        List<MatOfPoint> counters = new ArrayList<>();
        Mat hierarchy = new Mat ();
        Imgproc.findContours(mat, counters, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        
    }
}
