package com.example.rollcall.Camera;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QrCodeDetector {

    private final static String TAG = "QrCodeDetector";

    public Bitmap detect(Bitmap bmp) {
        Mat mat = bitmapToMat(bmp);

        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY);

        Mat canny = new Mat();
        double highThreshold = 255;
        double lowThreshold  = 255/3;
        Imgproc.Canny(gray, canny, lowThreshold, highThreshold);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11, 11));
        Mat morph = new Mat();
        Imgproc.morphologyEx(canny, morph, Imgproc.MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(morph, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat rgb = new Mat();
        Imgproc.cvtColor(morph, rgb, Imgproc.COLOR_GRAY2RGB);

        List<RotatedRect> rects = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f  contour2f = new MatOfPoint2f( contours.get(i).toArray() );
            RotatedRect rect = Imgproc.minAreaRect(contour2f);
            rects.add(rect);

            Imgproc.drawContours(rgb, contours, i, new Scalar(255, 255, 255), 2);
        }

        List<Integer> rectInfo = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            rectInfo.add(0);
        }

        for (int i = 0; i < rects.size(); i++) {
            for (int j = 0; j < rects.size(); j++) {
//                Log.d(TAG, String.valueOf(Math.abs(rect.center.x - otherRect.center.x)));
                if (
                        Math.abs(rects.get(i).center.x - rects.get(j).center.x) < 10 &&
                        Math.abs(rects.get(i).center.y - rects.get(j).center.y) < 10 &&
                        Math.abs(rects.get(i).size.width - rects.get(i).size.height) < 100 &&
                        Math.abs(rects.get(j).size.width - rects.get(j).size.height) < 100 &&
                        Math.abs((rects.get(i).size.width / rects.get(j).size.width) - (rects.get(i).size.height / rects.get(j).size.height)) < 100
                ) {
                    rectInfo.set(i, rectInfo.get(i) + 1);
                }
            }
        }

        List<RotatedRect> finderRects = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            if (rectInfo.get(i) >= 6) {
//                Imgproc.circle(rgb, rects.get(i).center, 10, new Scalar(255, 0, 0));
//                Imgproc.circle(rgb, new Point(rects.get(i).center.x - rects.get(i).size.width / 2, rects.get(i).center.y - rects.get(i).size.height / 2), 10, new Scalar(255, 0, 0));
                finderRects.add(rects.get(i));
            }
        }
        Log.d(TAG, String.valueOf(finderRects.size()));
        if (finderRects.size() != 18) {
            return matToBitmap(rgb);
        }

        List<Double> sizes = new ArrayList<>();
        for (RotatedRect rect: finderRects) {
//            Imgproc.circle(rgb, new Point(rect.center.x - rect.size.width / 2, rect.center.y - rect.size.height / 2), 10, new Scalar(255, 0, 0));
            sizes.add(rect.size.width);
        }

        List<RotatedRect> bigFinderRects = new ArrayList<>();
        Collections.sort(sizes, Collections.reverseOrder());
        for (RotatedRect rect: finderRects) {
            if (sizes.get(0) == rect.size.width) bigFinderRects.add(rect);
            else if (sizes.get(1) == rect.size.width) bigFinderRects.add(rect);
            else if (sizes.get(2) == rect.size.width) bigFinderRects.add(rect);
            else if (sizes.get(3) == rect.size.width) bigFinderRects.add(rect);
            else if (sizes.get(4) == rect.size.width) bigFinderRects.add(rect);
            else if (sizes.get(5) == rect.size.width) bigFinderRects.add(rect);
        }

        List<RotatedRect> threeBigFinderRect = new ArrayList<>();
        for (int i = 0; i < bigFinderRects.size(); i++) {
            if (i % 2 == 0) {
                threeBigFinderRect.add(bigFinderRects.get(i));
            }
        }

        RotatedRect leftTop = new RotatedRect();
        RotatedRect rightTop = new RotatedRect();
        RotatedRect leftBottom = new RotatedRect();

        List<Double> xs = new ArrayList<>();
        for (RotatedRect rect: threeBigFinderRect) {
            xs.add(rect.center.x);
        }
        Collections.sort(xs, Collections.reverseOrder());
        for (RotatedRect rect: threeBigFinderRect) {
            if (xs.get(0) == rect.center.x) rightTop = rect;
        }

        List<Double> ys = new ArrayList<>();
        for (RotatedRect rect: threeBigFinderRect) {
            ys.add(rect.center.y);
        }
        Collections.sort(ys, Collections.reverseOrder());
        for (RotatedRect rect: threeBigFinderRect) {
            if (ys.get(0) == rect.center.y) leftBottom = rect;
        }

        for (RotatedRect rect: threeBigFinderRect) {
            if (rect != leftBottom && rect != rightTop) leftTop = rect;
        }

        Point leftTopPoint = new Point(leftTop.center.x - leftTop.size.width / 2, leftTop.center.y - leftTop.size.height / 2);
        Imgproc.circle(rgb, leftTopPoint, 10, new Scalar(255, 0, 0), 10);

        Point rightTopPoint = new Point(rightTop.center.x + rightTop.size.width / 2, rightTop.center.y - rightTop.size.height / 2);
        Imgproc.circle(rgb, rightTopPoint, 10, new Scalar(255, 0, 0), 10);

        Point leftBottomPoint = new Point(leftBottom.center.x - leftBottom.size.width / 2, leftBottom.center.y + leftBottom.size.height / 2);
        Imgproc.circle(rgb, leftBottomPoint, 10, new Scalar(255, 0, 0), 10);

        Point rightBottomPoint = new Point(rightTopPoint.x + (leftBottomPoint.x - leftTopPoint.x), leftBottomPoint.y + (rightTopPoint.y - leftTopPoint.y));
        Imgproc.circle(rgb, rightBottomPoint, 10, new Scalar(255, 0, 0), 10);

        Rect mainRect = new Rect(leftTopPoint, rightBottomPoint);

        Imgproc.rectangle(rgb, mainRect.tl(), mainRect.br(), new Scalar(0, 255, 0), 2);

        return matToBitmap(rgb);
    }

    private Mat bitmapToMat(Bitmap bmp) {
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        return mat;
    }

    private Bitmap matToBitmap(Mat mat) {
        Bitmap bmp = null;
        //Mat tmp = new Mat();
        try {
            //Imgproc.cvtColor(mat, tmp, Imgproc.COLOR_HSV2BGR);
            bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bmp);
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }
        return bmp;
    }
}
