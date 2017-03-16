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

public class ListActivity extends AppCompatActivity {
    public static String page_type;
    //private EditText mFocusedEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getSupportActionBar().setHomeButtonEnabled(true);//返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getsystemscandata();//注册接收数据广播，以TOAST形式显示，退出界面也可以查看数据
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
            cursor = db.rawQuery("select shelves,part_no,quantity from gate_in order by id desc", null);
        }else if("gateInReturn".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity from gate_in where return_flag = 'Y' order by id desc", null);
        }else if("shiftIn".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity from gate_in where move_flag = 'Y'  order by id desc", null);
        }else if("gateOut".equals(page_type)){
            cursor = db.rawQuery("select id,part_no,quantity from gate_out order by id desc", null);
        }else if("shiftOut".equals(page_type)){
            cursor = db.rawQuery("select id,part_no,quantity from gate_out where move_flag = 'Y' order by id desc", null);
        }else if("invCheck".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,quantity from inv_check_order order by id desc", null);
        }else if("invReCheck".equals(page_type)){
            cursor = db.rawQuery("select shelves,part_no,check_quantity from inv_check_order where check_quantity is not null order by id desc", null);
        }

        String data[] = new String[cursor.getCount()];
        int num = 0;
        if(cursor.moveToFirst()){
            do{
                data[num] = cursor.getString(0)+"  |  "+cursor.getString(1)+"  |  "+cursor.getString(2);
                num++;
            }while(cursor.moveToNext());
        }

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(ListActivity.this,android.R.layout.simple_list_item_1,data);
        ListView listView=(ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        Log.v("debug","5秒后打印出我！");
    };


    private BroadcastReceiver mBrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.android.receive_scan_action")) {
                String datat = intent.getStringExtra("data");
                System.out.println("####baishi######:"+datat);
            }
        }
    };

    /**
     * 获取接受到的扫描数据,注册广播
     */
    public void getsystemscandata() {
        final String getstr = "com.android.receive_scan_action";
        mBrReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            };
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
    }


}