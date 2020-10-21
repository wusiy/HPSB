package com.sunland.cpocr.path_record.record;

import java.util.ArrayList;
import java.util.List;
import com.amap.api.location.AMapLocation;

/**
 * 用于记录一条轨迹，包括起点、终点、轨迹中间点、距离、耗时、平均速度、时间
 * 
 * @author ligen
 * 
 */
public class PathRecord {
	private AMapLocation mStartPoint;
	private AMapLocation mEndPoint;
	private String mStrStartPoint;
	private String mStrEndPoint;
	private List<AMapLocation> mPathLinePoints = new ArrayList<AMapLocation>();
	private String mDistance;
	private String mDuration;
	private String mAveragespeed;
	private String mDate;
	private int mId = 0;

	public PathRecord() {

	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public AMapLocation getStartpoint() {
		return mStartPoint;
	}

	public void setStartpoint(AMapLocation startpoint) {
		this.mStartPoint = startpoint;
	}

	public AMapLocation getEndpoint() {
		return mEndPoint;
	}

	public void setEndpoint(AMapLocation endpoint) {
		this.mEndPoint = endpoint;
	}

	public List<AMapLocation> getPathline() {
		return mPathLinePoints;
	}

	public void setPathline(List<AMapLocation> pathline) {
		this.mPathLinePoints = pathline;
	}

	public String getDistance() {
		return mDistance;
	}

	public void setDistance(String distance) {
		this.mDistance = distance;
	}

	public String getDuration() {
		return mDuration;
	}

	public void setDuration(String duration) {
		this.mDuration = duration;
	}

	public String getAveragespeed() {
		return mAveragespeed;
	}

	public void setAveragespeed(String averagespeed) {
		this.mAveragespeed = averagespeed;
	}

	public String getStrStartPoint() {
		return mStrStartPoint;
	}

	public void setStrStartPoint(String strStartPoint) {
		this.mStrStartPoint = strStartPoint;
	}

	public String getStrEndPoint() {
		return mStrEndPoint;
	}

	public void setStrEndPoint(String strEndPoint) {
		this.mStrEndPoint = strEndPoint;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		this.mDate = date;
	}

	public void addpoint(AMapLocation point) {
		mPathLinePoints.add(point);
	}

	@Override
	public String toString() {
		StringBuilder record = new StringBuilder();
		record.append("巡逻点记录个数: " + getPathline().size() + "    ");
		record.append("巡逻距离: " + getDistance() + "m    ");
		record.append("巡逻时长: " + getDuration() + "s ");
		return record.toString();
	}
}
