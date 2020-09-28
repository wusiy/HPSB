package com.sunland.cpocr.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.lpr.LPR;
import com.sunland.cpocr.utils.CpocrUtils;

/**
 * 根据图片识别号牌
 */
public class LprOperation {

	private static LprOperation mLprOperation = null;
	private Bitmap mBitmap = null;
	private Context mContext;

	private LprOperation(Context c){
		mContext = c;

		LPR.getInstance().copyDataBase(c);

		ImageOperation.init(c);
	}

	public static LprOperation getInstance(Context c) {
		if (mLprOperation == null)
			mLprOperation = new LprOperation(c);
		return mLprOperation;
	}

	private short[] pixs = null;
	private int[] pix = null;
	private int imageSize = 0;

	private String startDetectLPRFromBmp(Context context, Bitmap rawBitmap) {

		if (rawBitmap == null) {
			return "";
		}
		int rawHeight = rawBitmap.getHeight();
		int rawWidth = rawBitmap.getWidth();
		int size = rawHeight * rawWidth;

		if (pix == null || size > this.imageSize) {
			pix = null;
			pixs = null;
			pix = new int[size];
			pixs = new short[size * 3];
			this.imageSize = size;
		}

		rawBitmap.getPixels(pix, 0, rawWidth, 0, 0, rawWidth, rawHeight);
		if (rawBitmap.isRecycled() == false)
			rawBitmap.recycle();

		for (int i = 0, j = 0; i < size; i++) {
			pixs[j++] = (short) ((pix[i] & 0x00FF0000) >> 16);
			pixs[j++] = (short) ((pix[i] & 0x0000FF00) >> 8);
			pixs[j++] = (short) ((pix[i] & 0x000000FF) >> 0);
		}

		String res = null;
		try {
			byte result[] = LPR.getInstance().detectLPR(context, pixs, rawWidth, rawHeight);
			res = new String(result, "gb2312").trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 根据图片路径识别车辆号牌
	 *
	 * @param filePath
	 * @return 如果识别成功，返回一个包含两值的数组，1是车牌号码，2是号牌颜色；如果识别失败，返回null
	 */
	public String[] getHphm(String filePath){
		mBitmap = ImageOperation.imageAdjusted(filePath);
		if(mBitmap == null){
			return null;
		}

		String[] res = new String[2];
		//int width = mBitmap.getWidth();
		//int height = mBitmap.getHeight();

		String hpInfo = startDetectLPRFromBmp(mContext, mBitmap);
		if (!mBitmap.isRecycled())
			mBitmap.recycle();

		if (!TextUtils.isEmpty(hpInfo)) {
			toHphm(res, hpInfo);
		}else{
			res = null;
		}

		System.gc();
		return res;
	}

	private void toHphm(String[] res, String hpInfo){
		/*格式为“车牌号码，车牌颜色”,其中车牌颜色由数字代替：
		0：代表未知颜色
		1：代表蓝色车牌
		2：代表黑色车牌
		3：代表黄色车牌
		4：代表白色车牌
		5：代表绿色车牌（新能源汽车）
		如输出结果为“京A12345,1”表示车牌号码为 “京A12345” 车牌颜色为 蓝色车牌*/

		String[] ress = hpInfo.trim().split(",");
		res[0] = ress[0];
		res[1] = CpocrUtils.getHphzys(ress[1]);
	}
}
