package com.example.christ.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UsersRepo {
    private DBHelper dbHelper;

    public UsersRepo(Context context){
        dbHelper = new DBHelper(context);
    }

    public int insert(User user){
        //打开链接，写入数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(User.KEY_Age,user.getAge());
        values.put(User.KEY_Gender,user.getGender());
        values.put(User.KEY_Name,user.getUsername());
        values.put(User.KEY_Password,user.getPassword());
        values.put(User.KEY_Score,user.getScore());
        values.put(User.KEY_Date,user.getDate());
        long ID = db.insert(User.TABLE,null,values);
        db.close();
        return (int)ID;
    }

    public void update(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(User.KEY_Age,user.getAge());
        values.put(User.KEY_Gender,user.getGender());
        values.put(User.KEY_Name,user.getUsername());
        values.put(User.KEY_Password,user.getPassword());
        values.put(User.KEY_Score,user.getScore());
        values.put(User.KEY_Date,user.getDate());
        db.update(User.TABLE,values,User.KEY_ID+"=?",new String[]{String.valueOf(user.getId())});
        db.close();
    }

    public User getUserByName(String name){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT "+
                User.KEY_ID+","+
                User.KEY_Name+","+
                User.KEY_Gender+","+
                User.KEY_Age+","+
                User.KEY_Password+","+
                User.KEY_Date+","+
                User.KEY_Score+
                " FROM "+User.TABLE
                + " WHERE " +
                User.KEY_Name + "=?";

        User user = new User();
        Cursor cursor = db.rawQuery(query,new String[]{name});
        if(cursor.moveToFirst()){
            do{
                user.setScore(cursor.getInt(cursor.getColumnIndex(User.KEY_Score)));
                user.setGender(cursor.getInt(cursor.getColumnIndex(User.KEY_Gender)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(User.KEY_Password)));
                user.setAge(cursor.getInt(cursor.getColumnIndex(User.KEY_Age)));
                user.setUsername(name);
                user.setDate(cursor.getString(cursor.getColumnIndex(User.KEY_Date)));
                user.setId(cursor.getInt(cursor.getColumnIndex(User.KEY_ID)));
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return user;
    }
}
