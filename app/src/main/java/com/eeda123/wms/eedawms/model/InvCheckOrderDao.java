package com.eeda123.wms.eedawms.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by a13570610691 on 2017/3/2.
 */

public class InvCheckOrderDao {
    private DbHelper dbHelper;

    public InvCheckOrderDao(Context context){
        dbHelper=new DbHelper(context);
    }

    public ArrayList<HashMap<String, ?>> getList(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="select * from inv_check_order";
        ArrayList<HashMap<String,?>> list=new ArrayList<HashMap<String, ?>>();
        Cursor cursor=db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> student=new HashMap<String, String>();
                for (int i= 0;i<cursor.getColumnCount();i++){
                    student.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(student);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
