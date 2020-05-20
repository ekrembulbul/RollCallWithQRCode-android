package com.example.rollcall.Camera;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class QrCodeDetector {

    private int BLUR_VALUE = 3;
    private float SQUARE_TOLERANCE = 0.15f;
    private float AREA_TOLERANCE = 0.15f;
    private float DISTANCE_TOLERANCE = 0.25f;
    private int WARP_DIM = 300;
    private int SMALL_DIM = 29;

    private final static String TAG = "QrCodeDetector";

    public void detect(Bitmap bitmap) {
        
    }
}
