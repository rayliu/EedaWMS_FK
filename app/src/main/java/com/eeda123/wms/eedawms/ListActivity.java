package com.eeda123.wms.eedawms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.eeda123.wms.eedawms.model.DbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListActivity extends AppCompatActivity {
    public static String page_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getSupportActionBar().setHomeButtonEnabled(true);//返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://设置返回按钮 actionBar.setHomeButtonEnabled(true) 后; 响应返回按钮
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    protected void findViewById() {
        page_type = getIntent().getStringExtra(page_type);
        DbHelper database_helper = new DbHelper(ListActivity.this);
        SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库
        Cursor cursor = null;
        if("gateIn".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity,qr_code from gate_in order by id desc", null);
        }else if("gateInReturn".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity,qr_code from gate_in where return_flag = 'Y' order by id desc", null);
        }else if("shiftIn".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity,qr_code from gate_in where move_flag = 'Y'  order by id desc", null);
        }else if("gateOut".equals(page_type)){
            cursor = db.rawQuery("select order_no,part_no,quantity,qr_code from gate_out order by id desc", null);
        }else if("shiftOut".equals(page_type)){
            cursor = db.rawQuery("select order_no,part_no,quantity,qr_code from gate_out where move_flag = 'Y' order by id desc", null);
        }else if("invCheck".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity,qr_code from inv_check_order order by id desc", null);
        }else if("invReCheck".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,check_quantity,qr_code from inv_check_order where check_quantity is not null order by id desc", null);
        }

        String data[] = new String[cursor.getCount()];
        int num = 0;
        if(cursor.moveToFirst()){
            do{
                String qr_code = cursor.getString(3);
                Matcher m= Pattern.compile("[^\\(\\)]+").matcher(qr_code);
                List<String> list = new ArrayList<String>();
                while (m.find()) {
                    list.add(m.group());
                }
                String codeId= list.get(0);
                if("gateOut".equals(page_type)){
                    data[num] = "("+codeId+")part_no："+cursor.getString(1)+" 单号："+cursor.getString(0)+" 数量："+cursor.getString(2);
                }else{
                    data[num] = "("+codeId+")part_no："+cursor.getString(1)+" 货架："+cursor.getString(0)+" 数量："+cursor.getString(2);
                }

                num++;
            }while(cursor.moveToNext());
        }

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(ListActivity.this,android.R.layout.simple_list_item_1,data);
        ListView listView=(ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
    };
}
