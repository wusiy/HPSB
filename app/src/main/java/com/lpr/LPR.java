package com.lpr;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Native;

public class LPR {

	private static LPR lpr = null;
	private String dst = Environment.getExternalStorageDirectory().toString() + "/lpr.key";

	private LPR() {

	}

	public static LPR getInstance() {
		if (lpr == null)
			lpr = new LPR();
		return lpr;
	}

	public void copyDataBase(Context c){
		try {
			copyDataBase(c.getAssets().open("lpr.key"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void copyDataBase(InputStream myInput){

		File file = new File(dst);
		if (file.exists()) {
			file.delete();
		}

		try {
			OutputStream myOutput = new FileOutputStream(dst);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (Exception e) {
			System.out.println("lpr.key" + "is not found");
		}
	}

	public byte[] detectLPR(Context context, short[] pixs, int width, int height){
		return DetectLPR(context, pixs, width, height, dst);
	}

	public int init(Context context,int roileft,int roitop,int roiright,int roibottom,int nwidth,int nheight){
		return Init(context, roileft, roitop, roiright, roibottom, nwidth, nheight, dst);
	}

	static
	{
		System.loadLibrary("LPRecognition");
	}

	public native int Init(Context context,int roileft,int roitop,int roiright,int roibottom,int nwidth,int nheight,String filePath);
	public native byte[]  VideoRec(byte[] ImageStreamNV21, int width, int height,int imgflag);
	public native int Release();
//	public native byte[] DetectLPR(short[] ImageStreamNV21, int width, int height,String imputstring);
	public native static byte[] DetectLPR(Context context, short[] pixs, int width, int height, String filePath);
}
