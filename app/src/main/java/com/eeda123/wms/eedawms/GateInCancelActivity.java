package com.eeda123.wms.eedawms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.eeda123.wms.eedawms.model.DbHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GateInCancelActivity extends AppCompatActivity {

    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;
    private EditText mFocusedEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_in_cancel);

        getSupportActionBar().setHomeButtonEnabled(true);//返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getsystemscandata();//注册接收数据广播，以TOAST形式显示，退出界面也可以查看数据

        findViewById();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //设置返回按钮 actionBar.setHomeButtonEnabled(true) 后; 响应返回按钮
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BroadcastReceiver mBrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.android.receive_scan_action")) {
                String datat = intent.getStringExtra("data");
                System.out.println("####baishi######:"+datat);
            }
        }
    };

    protected void findViewById() {

        qrCodeEditText = (EditText) findViewById(R.id.qrCodeEditText);
        partNoEditText = (EditText) findViewById(R.id.part_no);
        quantityEditText = (EditText) findViewById(R.id.quantity);
        shelfEditText = (EditText) findViewById(R.id.shelfEditText);

        mFocusedEditText = qrCodeEditText;
        //qrCodeEditText.setOnFocusChangeListener(focusListener);
        findViewById(R.id.comfirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(mBrReceiver);
                //Toast.makeText(getApplicationContext(), "反注册广播完成", Toast.LENGTH_SHORT).show();

                DbHelper database_helper = new DbHelper(GateInCancelActivity.this);
                SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

                String qrCode = qrCodeEditText.getText().toString();

                db.execSQL("delete from gate_in where qr_code = '"+qrCode+"'; ");
               // finish();
                clearDate();
            }
        });
    }

    public void clearDate(){
        qrCodeEditText.setText("");
        partNoEditText.setText("");
        quantityEditText.setText("");
        shelfEditText.setText("");

        qrCodeEditText.requestFocus();
        mFocusedEditText = qrCodeEditText;

        Toast.makeText(this, "取消成功!", Toast.LENGTH_SHORT).show();
    }

    public void getsystemscandata() {
        final String getstr = "com.android.receive_scan_action";
        mBrReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(getstr)) {
                    String datat = intent.getStringExtra("data");
                    System.out.println("####qr######:"+datat);
                    mFocusedEditText.setText(datat);

                    int mIndex = 0;
                    Matcher m= Pattern.compile("[^\\(\\)]+").matcher(datat);
                    while(m.find()) {

                        System.out.println("####m str ######:"+m.group());
                        if(mIndex == 4)
                            partNoEditText.setText(m.group());
                        if(mIndex == 6)
                            quantityEditText.setText(m.group());

                        mIndex++;
                    }

                    if(StringUtils.isNotEmpty(datat)){
                        DbHelper database_helper = new DbHelper(GateInCancelActivity.this);
                        SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库
                        Cursor cursor = db.rawQuery("select * from gate_in where qr_code = '"+datat+"'", null);
                        while (cursor.moveToNext()) {
                            int id = cursor.getInt(0); //获取第一列的值,第一列的索引从0开始
                            String shelves  = cursor.getString(4);//获取第三列的值
                            shelfEditText.setText(shelves);
                        }
                        cursor.close();
                        db.close();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
        //Toast.makeText(this, "注册广播完成，自动接收数据", Toast.LENGTH_LONG).show();

    }
}
