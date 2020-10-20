package com.sunland.cpocr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.text.format.Time;
import android.widget.Toast;

//import com.sunland.cpocr.activity.SbhpActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class CpocrUtils {
    public final static int REQUEST_CPOCR_RECG = 9999;//启动号牌识别时启用相机的返回码

    /**
     * 通过OCR获取号牌种类信息
     *
     * @param hphm    号牌号码
     * @param hpysStr 号牌颜色中文
     * @return
     */
    public static String[] getHpzlFromOcr(String hphm, String hpysStr) {
        String hpzl = "02";
        String hpzlStr = "小型汽车";
        if (hphm.endsWith("港")) {
            hpzl = "26";
            hpzlStr = "香港出入境车";
        } else if (hphm.endsWith("澳")) {
            hpzl = "27";
            hpzlStr = "澳门出入境车";
        } else if (hphm.endsWith("使")) {
            hpzl = "03";
            hpzlStr = "使馆汽车";
        } else if (hphm.endsWith("领")) {
            hpzl = "04";
            hpzlStr = "领馆汽车";
        } else if (hphm.endsWith("警")) {
            hpzl = "23";
            hpzlStr = "警用汽车";
        } else if (hphm.endsWith("挂")) {
            hpzl = "15";
            hpzlStr = "挂车";
        } else if (hphm.endsWith("学")) {
            hpzl = "16";
            hpzlStr = "教练汽车";
        } else if (hpysStr.contains("蓝")) {
            hpzl = "02";
            hpzlStr = "小型汽车";
        } else if (hpysStr.contains("黑")) {
            hpzl = "05";
            hpzlStr = "境外汽车";
        } else if (hpysStr.contains("黄")) {
            hpzl = "01";
            hpzlStr = "大型汽车";
        } else if (hpysStr.contains("白")) {
            //由于目前车牌识别，容易将泛白的黄牌（阳光照射等情况）识别成白牌，白牌车辆又非常少，这里也默认成黄牌
            hpzl = "01"; // 默认大型汽车
            hpzlStr = "大型汽车";
        } else if (hpysStr.contains("绿")) {
            if (hphm.endsWith("D") || hphm.endsWith("F")) {//大型新能源汽车
                hpzl = "51";// 大型新能源汽车
                hpzlStr = "大型新能源汽车";
            } else if (hphm.length() > 3 && (hphm.substring(2).startsWith("A") || hphm.substring(2).startsWith("D") || hphm.substring(2).startsWith("F"))) {
                hpzl = "52";// 小型新能源汽车
                hpzlStr = "小型新能源汽车";
            } else {//可能是大型汽车识别错误导致
                hpzl = "01"; // 默认大型汽车
                hpzlStr = "大型汽车";
            }
        } else {
            hpzl = "99";
            hpzlStr = "其他号牌";
        }

        return new String[]{hpzl, hpzlStr};
    }

    /**
     * 转化成汉字颜色
     *
     * @param hpys OCR返回的号牌颜色代码
     * @return 号牌颜色名称
     */
    public static String getHphzys(String hpys) {
        String hpysStr = "未知颜色";
        hpys = hpys.trim();
        if ("0".equals(hpys)) {
            hpysStr = "未知颜色";
        } else if ("1".equals(hpys)) {
            hpysStr = "蓝色";
        } else if ("2".equals(hpys)) {
            hpysStr = "黑色";
        } else if ("3".equals(hpys)) {
            hpysStr = "黄色";
        } else if ("4".equals(hpys)) {
            hpysStr = "白色";
        } else if ("5".equals(hpys)) {
            hpysStr = "绿色";
        }
//        switch (Integer.parseInt(hpys)) {
//            case 0:
//                hpys = "未知颜色";
//                break;
//            case 1:
//                hpys = "蓝色";
//                break;
//            case 2:
//                hpys = "黑色";
//                break;
//            case 3:
//                hpys = "黄色";
//                break;
//            case 4:
//                hpys = "白色";
//                break;
//            case 5:
//                hpys = "绿色";
//                break;
//
//        }
        return hpysStr;
    }

    public static String savePicture(Context context, Bitmap bitmap, String filePath, String tag) {
        String strCaptureFilePath = filePath + tag + pictureName() + ".jpg";
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(strCaptureFilePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            Toast.makeText(context, "图片存储失败,请检查SD卡", Toast.LENGTH_SHORT).show();
        }
        return strCaptureFilePath;
    }

    private static String pictureName() {
        String str = "";
        Time t = new Time();
        t.setToNow(); // 取得系统时间。
        int year = t.year;
        int month = t.month + 1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        if (month < 10)
            str = String.valueOf(year) + "0" + String.valueOf(month);
        else {
            str = String.valueOf(year) + String.valueOf(month);
        }
        if (date < 10)
            str = str + "0" + String.valueOf(date + "_");
        else {
            str = str + String.valueOf(date + "_");
        }
        if (hour < 10)
            str = str + "0" + String.valueOf(hour);
        else {
            str = str + String.valueOf(hour);
        }
        if (minute < 10)
            str = str + "0" + String.valueOf(minute);
        else {
            str = str + String.valueOf(minute);
        }
        if (second < 10)
            str = str + "0" + String.valueOf(second);
        else {
            str = str + String.valueOf(second);
        }
        return str;
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int[] convertYUV420_NV21toARGB8888(byte[] data, int width, int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i along Y and the final pixels
        // k along pixels U and V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoARGB(y1, u, v);
            pixels[i + 1] = convertYUVtoARGB(y2, u, v);
            pixels[width + i] = convertYUVtoARGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoARGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }

        return pixels;
    }

    public static int convertYUVtoARGB(int y, int u, int v) {
        int r, g, b;

        r = y + (int) 1.402f * u;
        g = y - (int) (0.344f * v + 0.714f * u);
        b = y + (int) 1.772f * v;
        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * 根据路径识别号牌号码
     *
     * @param activity
     */
//    public static void cpocrHp(Activity activity, String filePath) {
//        ArrayList<String> filePaths = new ArrayList<String>();
//        filePaths.add(filePath);
//        cpocrHp(activity, filePaths);
//    }
//
//    public static void cpocrHp(Activity activity, ArrayList<String> filePaths) {
//        Intent intent = new Intent(activity.getApplicationContext(), SbhpActivity.class);
//        intent.putStringArrayListExtra("filePaths", filePaths);
//        activity.startActivityForResult(intent, REQUEST_CPOCR_RECG);
//    }

}
