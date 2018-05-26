package com.example.christ.musicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "MySQLite.db";

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据表
        String CREATE_TABLE_Users = "CREATE TABLE " + User.TABLE + "("
                + User.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + User.KEY_Name + " TEXT,"
                + User.KEY_Gender + " INTEGER,"
                + User.KEY_Password + " TEXT,"
                + User.KEY_Score + " INTEGER,"
                + User.KEY_Date + " TEXT,"
                + User.KEY_Age + " INTEGER)";
        db.execSQL(CREATE_TABLE_Users);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //如果旧表存在，删除，所以数据将会消失
        db.execSQL("DROP TABLE IF EXISTS "+User.TABLE );
        //再次创建表
        onCreate(db);
    }
}
