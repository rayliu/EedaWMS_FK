package com.eeda123.wms.eedawms.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a13570610691 on 2017/3/1.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "eeda_data.db"; //数据库名称
    private static final int version = 1; //数据库版本

    public DbHelper(Context context) {
        super(context, DB_NAME, null, version);
    }

    //该函数在数据库第一次被建立时调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table gate_in(id INTEGER PRIMARY KEY AUTOINCREMENT, qr_code varchar(200), part_no varchar(60), quantity int);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
