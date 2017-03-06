package com.eeda123.wms.eedawms.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by a13570610691 on 2017/3/2.
 */

public class GateInDao {
    private DbHelper dbHelper;

    public GateInDao(Context context){
        dbHelper=new DbHelper(context);
    }

    public ArrayList<HashMap<String, ?>> getList(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT * FROM gate_in";
        ArrayList<HashMap<String,?>> list=new ArrayList<HashMap<String, ?>>();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> student=new HashMap<String, String>();
                student.put("id",cursor.getString(cursor.getColumnIndex("id")));
                student.put("qr_code",cursor.getString(cursor.getColumnIndex("qr_code")));
                list.add(student);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
