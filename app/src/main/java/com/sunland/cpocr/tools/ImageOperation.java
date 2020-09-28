package com.sunland.cpocr.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageOperation {
    //public static final int  IMAGE_MIN_HEIGHT= 1920;
    public static final int IMAGE_MIN_HEIGHT = 640;
    //public static final int  IMAGE_MIN_WIDHT = 1080;
    public static final int IMAGE_MIN_WIDHT = 480;
    public static final int MIN_IMAGE_SIZE = IMAGE_MIN_WIDHT * IMAGE_MIN_HEIGHT;//图片最小尺寸 130W

    private static Bitmap mBitmap = null;
    private static Context mContext = null;

    public ImageOperation() {
    }

    public static void init(Context c) {
        mContext = c;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        if (angle == 0)
            return bitmap;
        if (bitmap == null)
            return null;
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        matrix = null;
        return resizedBitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 1. 处理大图片，缩小到屏幕尺寸
     * 2. 选择图片，正常显示
     */
    public static Bitmap imageAdjusted(Uri uri) {
        if (uri == null)
            return null;
        return imageAdjusted(getFilePathFromContentUri(uri, mContext.getContentResolver()));
    }

    /**
     * 1. 处理大图片，缩小到屏幕尺寸
     * 2. 选择图片，正常显示
     */
    public static Bitmap imageAdjusted(String path) {

        if (path == null || path.isEmpty())
            return null;

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmpFactoryOptions);

        if (bmpFactoryOptions.outHeight * bmpFactoryOptions.outWidth > (MIN_IMAGE_SIZE * 0.8)) {
            mBitmap = imageAdjustedDisplay(path, false);
        } else {
            mBitmap = BitmapFactory.decodeFile(path, null);
        }

        if (mBitmap != null) {
            int degree = readPictureDegree(path);
            Bitmap b = rotaingImageView(degree, mBitmap);
            if (mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            return b;
        } else
            return null;
    }

    /**
     * 获取适应屏幕大小的图片
     *
     * @param path          图片绝对路径
     * @param adjustDisplay 是否适应屏幕，否则调整到S.MIN_IMAGE_SIZE
     * @return Bitmap 缩小后的图像
     * 屏幕的宽 《 高
     * 图像的宽 《 高
     */
    private static Bitmap imageAdjustedDisplay(String path, boolean adjustDisplay) {

        if (path == null || path.isEmpty())
            return null;

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int dw = dm.widthPixels;
        int dh = dm.heightPixels;
        if (dw > dh) {
            int t = dw;
            dw = dh;
            dh = t;
        }

        //if(adjustDisplay==false && dw*dh<MIN_IMAGE_SIZE){
        dw = IMAGE_MIN_WIDHT;
        dh = IMAGE_MIN_HEIGHT;
        //}

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmpFactoryOptions);

        int width = bmpFactoryOptions.outWidth;
        int height = bmpFactoryOptions.outHeight;
        if (width > height) {
            int t = width;
            width = height;
            height = t;
        }

        float heightRatio = (float) Math.ceil(height * 1.0 / (float) dh);
        float widthRatio = (float) Math.ceil(width * 1.0 / (float) dw);

        bmpFactoryOptions.inJustDecodeBounds = false;
        int resize = (int) (((heightRatio > widthRatio ? heightRatio : widthRatio) + 0.5) * 1.2);
        if (heightRatio > 1 || widthRatio > 1) {
            bmpFactoryOptions.inSampleSize = resize;
        }
        return BitmapFactory.decodeFile(path, bmpFactoryOptions);
    }

    public static String savePicToSdcard(Bitmap bitmap, String path, String fileName) {
        String filePath = "";
        if (bitmap == null) {
            return filePath;
        } else {

            filePath = path + fileName;
            File destFile = new File(filePath);
            OutputStream os = null;
            try {
                os = new FileOutputStream(destFile);
                bitmap.compress(CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
            } catch (IOException e) {
                filePath = "";
            }
        }
        return filePath;
    }

    private static String getFilePathFromContentUri(Uri uri,
                                                    ContentResolver contentResolver) {
        String filePath = null;
        String[] filePathColumn = {MediaColumns.DATA};

        Cursor cursor = contentResolver.query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }
}
