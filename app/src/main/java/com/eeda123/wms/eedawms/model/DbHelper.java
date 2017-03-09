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
        //String sql = "create table gate_in(id INTEGER PRIMARY KEY AUTOINCREMENT, qr_code varchar(200), part_no varchar(60), quantity int);";
        String gete_in_sql = "CREATE TABLE `gate_in` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT , `qr_code`  varchar(255) NULL ," +
             " `part_no`  varchar(255) NULL , `quantity`  int(20) NULL , `shelves`  varchar(255) NULL ," +
             " `creator`  varchar(255) NULL  ,`create_time`  datetime NULL, return_flag varchar(2) default 'N'," +
             " move_flag varchar(2) default 'N') ";
        db.execSQL(gete_in_sql);

        String gate_out_sql = "CREATE TABLE `gate_out` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT , `qr_code`  varchar(255) NULL ," +
             " `part_no`  varchar(255) NULL , `quantity`  int(20) NULL , `shelves`  varchar(255) NULL ," +
             " `creator`  varchar(255) NULL  ,`create_time`  datetime NULL , move_flag varchar(2) default 'N')";
        db.execSQL(gate_out_sql);

        String inv_check_sql = " CREATE TABLE `inv_check_order` ( 	`id` INTEGER PRIMARY KEY AUTOINCREMENT,  " +
           "  `order_no` VARCHAR (255) NULL, 	`qr_code` VARCHAR (255) NULL, 	`part_no` VARCHAR (255) NULL," +
           "  	`quantity` INT (20) NULL, 	`check_quantity` INT (20) NULL, `shelves` VARCHAR (255) NULL, " +
           " 	`creator` VARCHAR (255) NULL, 	`create_time` datetime NULL,`check_time` datetime NULL )";
        db.execSQL(inv_check_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
