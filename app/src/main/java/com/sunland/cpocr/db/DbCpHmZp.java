package com.sunland.cpocr.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 车牌号码照片数据库
 *
 */
public class DbCpHmZp {
    public static final String KEY_ROWID = "id";
    public static final String KEY_CPHM = "carnum";
    public static final String KEY_CPZL = "carzl";
    public static final String KEY_PHOTOS = "carphoto";
    private final static String DATABASE_PATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/HPSB/recordPhotoPath";
    static final String DATABASE_NAME = DATABASE_PATH + "/" + "recordphoto.db";
    private static final int DATABASE_VERSION = 5;
    private static final String RECORD_TABLE = "record_numphoto";
    private static final String RECORD_CREATE = "create table if not exists record_numphoto("
            + KEY_ROWID
            + " integer primary key autoincrement,"
            + "carnum STRING,"
            + "carzl STRING,"
            + "carphoto STRING" + ");";

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
    public DbCpHmZp(Context ctx) {
        this.mCtx = ctx;
        dbHelper = new DatabaseHelper(mCtx);
    }

    public DbCpHmZp open() throws SQLException {

        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

//    public Cursor getall() {
//        return db.rawQuery("SELECT * FROM record_numphoto", null);
//    }
//
//    // remove an entry
//    public boolean delete(long rowId) {
//        return db.delete(RECORD_TABLE, "id=" + rowId, null) > 0;
//    }


    /**
     * 数据库存入一条新车牌数据
     *
     * @param carnum
     * @param carphoto
     * @return
     */
    public long add_newcar(String carnum, String cartype, String carphoto) {
        ContentValues args = new ContentValues();
        args.put(KEY_CPHM, carnum);
        args.put(KEY_CPZL, cartype);
        args.put(KEY_PHOTOS, carphoto);
        return db.insert(RECORD_TABLE, null, args);
    }

    /**
     * 数据库为已存在的旧车牌存入一张新识别图片
     * @param carnum
     * @param carphoto
     * @return
     */
    public long add_carphoto( String carnum, String cartype, String carphoto) {
        String where1 = KEY_CPHM + "=?";
        String where2 = KEY_CPZL + "=?";
        ContentValues args = new ContentValues();
        args.put(KEY_CPHM, carnum);
        args.put(KEY_CPZL, cartype);
        args.put(KEY_PHOTOS, carphoto);
        return db.update(RECORD_TABLE, args, where1 + " and " + where2,new String[]{carnum, cartype});
    }

    /**
     * 数据库为车牌识别存入数据
     * @param carnum
     * @param carphoto
     */
    public void save_carinfo(String carnum, String cartype, String carphoto) {
        String record = queryRecordByCarNum(carnum, cartype);
        long i = 0;
        if(record.equals("")){
            i = add_newcar( carnum,  cartype, carphoto);
        }else{
            String s =  carphoto + "|" + record;
            Log.d("PPP", s);
            i = add_carphoto( carnum,  cartype, s);
        }
    }

    /**
     * 查询所有车牌号码
     *
     * @return
     */
    public List<String> queryAllCarNum() {
        List<String> allRecord = new ArrayList<String>();
        Cursor allRecordCursor = db.query(RECORD_TABLE, getColumns(), null,
                null, null, null, null);
        while (allRecordCursor.moveToNext()) {
            String record = "";
            record = allRecordCursor.getString(allRecordCursor.getColumnIndex(DbCpHmZp.KEY_CPHM)) + "|" +
                    allRecordCursor.getString(allRecordCursor.getColumnIndex(DbCpHmZp.KEY_CPZL));
            allRecord.add(record);
        }
        Collections.reverse(allRecord);
        return allRecord;
    }

    /**
     * 按照车牌号, 车牌种类查询
     *
     * @param mCarNum
     * @return
     */
    public String queryRecordByCarNum(String mCarNum, String mCartype) {
        String where1 = KEY_CPHM + "=?";
        String where2 = KEY_CPZL + "=?";
//        String[] selectionArgs = new String[] { String.valueOf(mCarNum) };
//        Cursor cursor = db.query(RECORD_TABLE, getColumns(), where,
//                selectionArgs, null, null, null);
        Cursor cursor=db.query(RECORD_TABLE,null,where1 + " and " + where2,
                new String[]{mCarNum,mCartype},null,null,null);
        String record = "";

        if (cursor.moveToNext()) {
            record = cursor.getString(cursor.getColumnIndex(DbCpHmZp.KEY_PHOTOS));
        }
        return record;
    }

    /**
     * 删除一条车牌记录
     * @return
     */
    public boolean deleteOneCpHmZp(String cphm,String cpzl ) {
        String where1 = KEY_CPHM + "=?";
        String where2 = KEY_CPZL + "=?";
        return db.delete(RECORD_TABLE, where1 + " and " + where2, new String[] { cphm, cpzl}) > 0;
    }


    private String[] getColumns() {
        return new String[] { KEY_ROWID, KEY_CPHM, KEY_CPZL, KEY_PHOTOS, };
    }
}
