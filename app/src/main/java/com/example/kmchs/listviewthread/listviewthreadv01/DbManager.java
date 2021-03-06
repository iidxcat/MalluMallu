package com.example.kmchs.listviewthread.listviewthreadv01;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbManager extends SQLiteOpenHelper {
    static final int DATABASE_VERSION=1;
    static final String DATABASE_NAME="history.db";
    public DbManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // id. 날짜. 만화번호(seq). 화수(제목포함). url(해당화만)
        db.execSQL("CREATE TABLE tableName (_id INTEGER PRIMARY KEY AUTOINCREMENT, date DATE, seq INTEGER, ep TEXT, url TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
