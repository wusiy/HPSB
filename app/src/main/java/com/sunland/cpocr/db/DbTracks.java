package com.sunland.cpocr.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sunland.cpocr.activity.navi.StrategyChooseActivity;
import com.sunland.cpocr.path_record.record.PathRecord;
import com.sunland.cpocr.path_record.recorduitl.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 轨迹记录数据库
 * 
 */
public class DbTracks {
	public static final String KEY_ROWID = "id";
	public static final String KEY_DISTANCE = "distance";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_SPEED = "averagespeed";
	public static final String KEY_LINE = "pathline";
	public static final String KEY_STRAT = "stratpoint";
	public static final String KEY_END = "endpoint";
	public static final String KEY_STR_STARTPOINT ="str_startpoint";
	public static final String KEY_STR_ENDPOINT = "str_endpoint";
	public static final String KEY_DATE = "date";
	private final static String DATABASE_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/HPSB/recordPath";
	static final String DATABASE_NAME = DATABASE_PATH + "/" + "record.db";
	private static final int DATABASE_VERSION = 1;
	private static final String RECORD_TABLE = "record";
	private static final String RECORD_CREATE = "create table if not exists record("
			+ KEY_ROWID
			+ " integer primary key autoincrement,"
			+ "stratpoint STRING,"
			+ "endpoint STRING,"
			+ "str_startpoint STRING,"
			+ "str_endpoint STRING,"
			+ "pathline STRING,"
			+ "distance STRING,"
			+ "duration STRING,"
			+ "averagespeed STRING,"
			+ "date STRING" + ");";

	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(RECORD_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	private Context mCtx = null;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	// constructor
	public DbTracks(Context ctx) {
		this.mCtx = ctx;
		dbHelper = new DatabaseHelper(mCtx);
	}

	public DbTracks open() throws SQLException {

		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor getall() {
		return db.rawQuery("SELECT * FROM record", null);
	}

	// remove an entry
	public boolean delete(long rowId) {
		return db.delete(RECORD_TABLE, "id=" + rowId, null) > 0;
	}

	/**
	 * 数据库存入一条轨迹
	 * 
	 * @param distance
	 * @param duration
	 * @param averagespeed
	 * @param pathline
	 * @param stratpoint
	 * @param endpoint
	 * @param date
	 * @return
	 */
	public long createrecord(String distance, String duration,
			String averagespeed, String pathline, String stratpoint, String endpoint,
							 String str_startpoint, String str_endpoint, String date) {
		ContentValues args = new ContentValues();
		args.put(KEY_DISTANCE, distance);
		args.put(KEY_DURATION, duration);
		args.put(KEY_SPEED, averagespeed);
		args.put(KEY_LINE, pathline);
		args.put(KEY_STRAT, stratpoint);
		args.put(KEY_END, endpoint);
		args.put(KEY_STR_STARTPOINT, str_startpoint);
		args.put(KEY_STR_ENDPOINT, str_endpoint);
		args.put(KEY_DATE, date);
		return db.insert(RECORD_TABLE, null, args);
	}

	/**
	 * 查询所有轨迹记录
	 * @return
	 */
	public List<PathRecord> queryRecordAll() {
		List<PathRecord> allRecord = new ArrayList<PathRecord>();
		Cursor allRecordCursor = db.query(RECORD_TABLE, getColumns(), null,
				null, null, null, null);
		while (allRecordCursor.moveToNext()) {
			PathRecord record = new PathRecord();
			record.setId(allRecordCursor.getInt(allRecordCursor
					.getColumnIndex(DbTracks.KEY_ROWID)));
			record.setDistance(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_DISTANCE)));
			record.setDuration(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_DURATION)));
			record.setDate(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_DATE)));
			String lines = allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_LINE));
			record.setPathline(Util.parseLocations(lines));
			record.setStartpoint(Util.parseLocation(allRecordCursor
					.getString(allRecordCursor
							.getColumnIndex(DbTracks.KEY_STRAT))));
			record.setEndpoint(Util.parseLocation(allRecordCursor
					.getString(allRecordCursor
							.getColumnIndex(DbTracks.KEY_END))));
			record.setStrStartPoint(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_STR_STARTPOINT)));
			record.setStrEndPoint(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_STR_ENDPOINT)));
			allRecord.add(record);
		}
		Collections.reverse(allRecord);
		return allRecord;
	}

	/**
	 * 按照时间查询
	 * @param date
	 * @return
	 */
	public PathRecord queryRecordByTime(String date){
		String where = KEY_DATE + "=?";
		String[] selectionArgs = new String[] {date};
		Cursor cursor = db.query(RECORD_TABLE, getColumns(), where,
				selectionArgs, null, null, null);
		PathRecord record = new PathRecord();
		if (cursor.moveToNext()) {
			record.setId(cursor.getInt(cursor
					.getColumnIndex(DbTracks.KEY_ROWID)));
			record.setDistance(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_DISTANCE)));
			record.setDuration(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_DURATION)));
			record.setDate(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_DATE)));
			String lines = cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_LINE));
			record.setPathline(Util.parseLocations(lines));
			record.setStartpoint(Util.parseLocation(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_STRAT))));
			record.setEndpoint(Util.parseLocation(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_END))));
			record.setStrStartPoint(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_STR_STARTPOINT)));
			record.setStrEndPoint(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_STR_ENDPOINT)));
		}
		return record;
	}

	/**
	 * 按照id查询
	 * @param mRecordItemId
	 * @return
	 */
	public PathRecord queryRecordById(int mRecordItemId) {
		String where = KEY_ROWID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(mRecordItemId) };
		Cursor cursor = db.query(RECORD_TABLE, getColumns(), where,
				selectionArgs, null, null, null);
		PathRecord record = new PathRecord();
		if (cursor.moveToNext()) {
			record.setId(cursor.getInt(cursor
					.getColumnIndex(DbTracks.KEY_ROWID)));
			record.setDistance(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_DISTANCE)));
			record.setDuration(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_DURATION)));
			record.setDate(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_DATE)));
			String lines = cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_LINE));
			record.setPathline(Util.parseLocations(lines));
			record.setStartpoint(Util.parseLocation(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_STRAT))));
			record.setEndpoint(Util.parseLocation(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_END))));
			record.setStrStartPoint(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_STR_STARTPOINT)));
			record.setStrEndPoint(cursor.getString(cursor
					.getColumnIndex(DbTracks.KEY_STR_ENDPOINT)));
		}
		return record;
	}

	/**
	 * 获取最后一条记录
	 * @return
	 */
	public PathRecord queryLastRecord() {
		Cursor allRecordCursor = db.query(RECORD_TABLE, getColumns(), null,
				null, null, null, null);
		PathRecord record = new PathRecord();
		while (allRecordCursor.moveToNext()) {
			record.setId(allRecordCursor.getInt(allRecordCursor
					.getColumnIndex(DbTracks.KEY_ROWID)));
			record.setDistance(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_DISTANCE)));
			record.setDuration(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_DURATION)));
			record.setDate(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_DATE)));
			String lines = allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_LINE));
			record.setPathline(Util.parseLocations(lines));
			record.setStartpoint(Util.parseLocation(allRecordCursor
					.getString(allRecordCursor
							.getColumnIndex(DbTracks.KEY_STRAT))));
			record.setEndpoint(Util.parseLocation(allRecordCursor
					.getString(allRecordCursor
							.getColumnIndex(DbTracks.KEY_END))));
			record.setStrStartPoint(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_STR_STARTPOINT)));
			record.setStrEndPoint(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(DbTracks.KEY_STR_ENDPOINT)));
		}
		return record;
	}

	/**
	 * 更新最后一条数据
	 *
	 * @param distance
	 * @param duration
	 * @param averagespeed
	 * @param pathline
	 * @param stratpoint
	 * @param endpoint
	 * @param date
	 * @return
	 */
	public long updatelastrecord(String distance, String duration,
							 String averagespeed, String pathline, String stratpoint, String endpoint,
								 String str_startpoint, String str_endpoint, String date) {

		Cursor allRecordCursor = db.query(RECORD_TABLE, getColumns(), null,
				null, null, null, null);
		int mRecordItemId = allRecordCursor.getCount();
		Log.e("AAA",mRecordItemId + "z");
		ContentValues args = new ContentValues();
		args.put(KEY_DISTANCE, distance);
		args.put(KEY_DURATION, duration);
		args.put(KEY_SPEED, averagespeed);
		args.put(KEY_LINE, pathline);
		args.put(KEY_STRAT, stratpoint);
		args.put(KEY_END, endpoint);
		args.put(KEY_STR_STARTPOINT, str_startpoint);
		args.put(KEY_STR_ENDPOINT, str_endpoint);
		args.put(KEY_DATE, date);
 		long b = queryLastRecord().getId();
		return db.update(RECORD_TABLE, args,"id=" + b, null) ;
	}

	/**
	 * 删除一条轨迹记录
	 * @param date
	 * @return
	 */
	public boolean deleteOneTrack(String date ) {
		String where = KEY_DATE + "=?";
		return db.delete(RECORD_TABLE, where, new String[] { date}) > 0;
	}

	private String[] getColumns() {
		return new String[] { KEY_ROWID, KEY_DISTANCE, KEY_DURATION, KEY_SPEED,
				KEY_LINE, KEY_STRAT, KEY_END, KEY_STR_STARTPOINT, KEY_STR_ENDPOINT, KEY_DATE };
	}
}
