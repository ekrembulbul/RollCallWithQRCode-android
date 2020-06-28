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
        if (finderRects.size() != 18) {
            return matToBitmap(rgb);
        }

        List<Double> sizes = new ArrayList<>();
        for (RotatedRect rect: finderRects) {
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

        Rect mainRect = new Rect((int) leftTopPoint.x, (int) leftTopPoint.y, (int) (rightTopPoint.x - leftTopPoint.x), (int) (leftBottomPoint.y - leftTopPoint.y));

        Imgproc.circle(rgb, mainRect.tl(), 10, new Scalar(0, 0, 255), 10);
        Imgproc.circle(rgb, mainRect.br(), 10, new Scalar(0, 0, 255), 10);
        Imgproc.rectangle(rgb, mainRect.tl(), mainRect.br(), new Scalar(0, 255, 0), 2);

        Rect[][] byteRectMat = new Rect[25][25];

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                double byteSizeWidth = mainRect.width / 25.0;
                double byteSizeHeight = mainRect.height / 25.0;
                int x = (int) (mainRect.x + j * byteSizeWidth);
                int y = (int) (mainRect.y + i * byteSizeHeight);
                Rect byteRect = new Rect(x, y, (int) byteSizeWidth, (int) byteSizeHeight);
                byteRectMat[i][j] = byteRect;
                Imgproc.rectangle(rgb, byteRect.tl(), byteRect.br(), new Scalar(255, 0, 255), 2);
            }
        }

        double[][] data = new double[25][25];
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                Point center = new Point(byteRectMat[i][j].x + byteRectMat[i][j].width / 2, byteRectMat[i][j].y + byteRectMat[i][j].height / 2);
                double[] dataMat = gray.get((int) center.y, (int) center.x);
                data[i][j] = dataMat[0];
            }
        }

//        Log.d(TAG, "data: " + data[2][0] + " " + data[2][1] + " " + data[2][2] + " " + data[2][3] + " " + data[2][4] + " " + data[2][5] + " " + data[2][6]);

        int[][] bitData = new int[25][25];
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if (data[i][j] < 128) bitData[i][j] = 1;
                else bitData[i][j] = 0;
            }
        }

        for (int i = 0; i < 25; i++) {
            String str = i + ": ";
            for (int j = 0; j < 25; j++) {
                str += bitData[i][j] + " ";
            }
            //Log.d(TAG, str);
        }

        String codeStr = "";

        int i = 0, k = 24;
        while (i < 16) {
            codeStr += String.valueOf(bitData[k][24]);
            codeStr += String.valueOf(bitData[k][23]);
            ++i;
            --k;
        }
        i = 0;
        k = 9;
        while (i < 16) {
            codeStr += String.valueOf(bitData[k][22]);
            codeStr += String.valueOf(bitData[k][21]);
            ++i;
            ++k;
        }
        i = 0;
        k = 24;
        while (i < 4) {
            codeStr += String.valueOf(bitData[k][20]);
            codeStr += String.valueOf(bitData[k][19]);
            ++i;
            --k;
        }
        i = 0;
        k = 15;
        while (i < 7) {
            codeStr += String.valueOf(bitData[k][20]);
            codeStr += String.valueOf(bitData[k][19]);
            ++i;
            --k;
        }
        i = 0;
        k = 9;
        while (i < 7) {
            codeStr += String.valueOf(bitData[k][18]);
            codeStr += String.valueOf(bitData[k][17]);
            ++i;
            ++k;
        }
        i = 0;
        k = 21;
        while (i < 4) {
            codeStr += String.valueOf(bitData[k][18]);
            codeStr += String.valueOf(bitData[k][17]);
            ++i;
            ++k;
        }
        i = 0;
        k = 24;
        while (i < 4) {
            codeStr += String.valueOf(bitData[k][16]);
            codeStr += String.valueOf(bitData[k][15]);
            ++i;
            --k;
        }
        i = 0;
        k = 20;
        while (i < 5) {
            codeStr += String.valueOf(bitData[k][15]);
            ++i;
            --k;
        }
        i = 0;
        k = 15;
        while (i < 9) {
            codeStr += String.valueOf(bitData[k][16]);
            codeStr += String.valueOf(bitData[k][15]);
            ++i;
            --k;
        }
        i = 0;
        k = 5;
        while (i < 6) {
            codeStr += String.valueOf(bitData[k][16]);
            codeStr += String.valueOf(bitData[k][15]);
            ++i;
            --k;
        }
        i = 0;
        k = 0;
        while (i < 6) {
            codeStr += String.valueOf(bitData[k][14]);
            codeStr += String.valueOf(bitData[k][13]);
            ++i;
            ++k;
        }
        i = 0;
        k = 7;
        while (i < 18) {
            codeStr += String.valueOf(bitData[k][14]);
            codeStr += String.valueOf(bitData[k][13]);
            ++i;
            ++k;
        }
        i = 0;
        k = 24;
        while (i < 18) {
            codeStr += String.valueOf(bitData[k][12]);
            codeStr += String.valueOf(bitData[k][11]);
            ++i;
            --k;
        }
        i = 0;
        k = 5;
        while (i < 6) {
            codeStr += String.valueOf(bitData[k][12]);
            codeStr += String.valueOf(bitData[k][11]);
            ++i;
            --k;
        }
        i = 0;
        k = 0;
        while (i < 6) {
            codeStr += String.valueOf(bitData[k][10]);
            codeStr += String.valueOf(bitData[k][9]);
            ++i;
            ++k;
        }
        i = 0;
        k = 7;
        while (i < 18) {
            codeStr += String.valueOf(bitData[k][10]);
            codeStr += String.valueOf(bitData[k][9]);
            ++i;
            ++k;
        }
        i = 0;
        k = 9;
        while (i < 8) {
            codeStr += String.valueOf(bitData[k][8]);
            codeStr += String.valueOf(bitData[k][7]);
            ++i;
            ++k;
        }
        i = 0;
        k = 9;
        while (i < 8) {
            codeStr += String.valueOf(bitData[k][5]);
            codeStr += String.valueOf(bitData[k][4]);
            ++i;
            ++k;
        }
        i = 0;
        k = 9;
        while (i < 8) {
            codeStr += String.valueOf(bitData[k][3]);
            codeStr += String.valueOf(bitData[k][2]);
            ++i;
            ++k;
        }
        i = 0;
        k = 9;
        while (i < 8) {
            codeStr += String.valueOf(bitData[k][1]);
            codeStr += String.valueOf(bitData[k][0]);
            ++i;
            ++k;
        }

        Log.d(TAG, codeStr);

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
