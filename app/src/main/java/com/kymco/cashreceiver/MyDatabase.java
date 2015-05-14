package com.kymco.cashreceiver;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MyDatabase extends Activity{
    public SQLiteDatabase db = null;
    private final static String DATABASE_NAME = "data.db";
    private final static String TABLE_NAME = "data";
    private final static String _ID = "_id";
    private final static String PATH_ID = "path_id";
    private final static String CLIENT_ID = "client_id";
    private final static String DATE = "date";
    private final static String CASH = "cash";
    private final static String CASH2 = "cash2";
    private final static String ISTRANSE = "istranse";

    private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
            + " (" + _ID + " INTEGER PRIMARY KEY," + PATH_ID + " TEXT," + CLIENT_ID + " TEXT," + DATE + " TEXT,"
            + CASH + " TEXT," + CASH2 + " TEXT," + ISTRANSE + " TEXT)";
    private Context mContext = null;

    public MyDatabase(Context context){
        this.mContext = context;
    }

    public void open() throws SQLException{
        db = mContext.openOrCreateDatabase(DATABASE_NAME, 0, null);

        try{
            db.execSQL(CREATE_TABLE);
        } catch (Exception e){

        }
    }

    public void close() throws SQLException{
        db.close();
    }

    public Cursor getALL(){
        Cursor mCursor = db.query(TABLE_NAME, new String[]{ _ID, PATH_ID, CLIENT_ID, DATE, CASH, CASH2, ISTRANSE},
                null, null, null, null, null);

        while(mCursor.moveToNext()){

        }
        return mCursor;
    }

    public Cursor get(long id){
        String where = _ID + "=" + id;
        Cursor mCursor = db.query(
                TABLE_NAME, null, where, null, null, null, null);
        if(mCursor.moveToFirst()) {
            return mCursor;
        }else{
            return null;
        }
    }

    public long append(String _pathid, String _clientid, String _date, String _cash, String _cash2, String _istranse){
        ContentValues args = new ContentValues();
        args.put(PATH_ID, _pathid);
        args.put(CLIENT_ID, _clientid);
        args.put(DATE, _date);
        args.put(CASH, _cash);
        args.put(CASH2, _cash2);
        args.put(ISTRANSE, _istranse);

        return db.insert(TABLE_NAME, null, args);
    }

    public boolean delete(long rowid){
        Log.d("mylog", "del: " + rowid);
        return db.delete(TABLE_NAME, _ID + "=" + rowid, null) > 0;
    }

    public boolean update(long rowid, String _pathid, String _clientid, String _date, String _cash, String _cash2, String _istranse){
        ContentValues args = new ContentValues();
        args.put(PATH_ID, _pathid);
        args.put(CLIENT_ID, _clientid);
        args.put(DATE, _date);
        args.put(CASH, _cash);
        args.put(CASH2, _cash2);
        args.put(ISTRANSE, _istranse);

        return db.update(TABLE_NAME, args, _ID + "=" + rowid, null) > 0;
    }
}
