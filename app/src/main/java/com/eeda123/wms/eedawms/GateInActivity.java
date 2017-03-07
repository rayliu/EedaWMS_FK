package com.eeda123.wms.eedawms;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.eeda123.wms.eedawms.model.DbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GateInActivity extends AppCompatActivity {
    private int offset = 0;

    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;

    private EditText mFocusedEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_in);

        getSupportActionBar().setHomeButtonEnabled(true);//返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getsystemscandata();//注册接收数据广播，以TOAST形式显示，退出界面也可以查看数据

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

        qrCodeEditText = (EditText) findViewById(R.id.qrCodeEditText);
        partNoEditText = (EditText) findViewById(R.id.part_no);
        quantityEditText = (EditText) findViewById(R.id.quantity);
        shelfEditText = (EditText) findViewById(R.id.shelfEditText);

        mFocusedEditText = shelfEditText;
        //qrCodeEditText.setOnFocusChangeListener(focusListener);

        findViewById(R.id.comfirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //unregisterReceiver(mBrReceiver);
                //Toast.makeText(getApplicationContext(), "反注册广播完成", Toast.LENGTH_SHORT).show();

                DbHelper database_helper = new DbHelper(GateInActivity.this);
                SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

                String qrCode = qrCodeEditText.getText().toString();
                String part_no = partNoEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String shelf = shelfEditText.getText().toString();
                String userName = getIntent().getStringExtra(USER_NAME);

                db.execSQL("insert into gate_in(qr_code, part_no, quantity, shelves,creator,create_time)" +
                        " values ('"+qrCode+"','"+part_no+"','"+quantity+"','"+shelf+"','"+userName+"','"+MainActivity.getDate()+"')");
                //unregisterReceiver(mBrReceiver);
                //finish();
                clearDate();
                System.out.println("入库成功");
            }
        });

        findViewById(R.id.nextShelfBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shelfEditText.setText("");
                shelfEditText.requestFocus();
                mFocusedEditText = shelfEditText;
            }
        });
    };


    public void clearDate(){
        qrCodeEditText.setText("");
        partNoEditText.setText("");
        quantityEditText.setText("");

        qrCodeEditText.requestFocus();
        mFocusedEditText = qrCodeEditText;
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

    /**
     * 获取接受到的扫描数据,注册广播
     */
    public void getsystemscandata() {
        final String getstr = "com.android.receive_scan_action";
        mBrReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(getstr)) {
                    String datat = intent.getStringExtra("data");
                    mFocusedEditText.setText(datat);

                    if(qrCodeEditText.hasFocus()) {
                        qrCodeEditText.setText(datat);
                        int mIndex = 0;
                        Matcher m= Pattern.compile("[^\\(\\)]+").matcher(datat);
                        while(m.find()) {
                            System.out.println("####qr######:"+datat);
                            System.out.println("####m str ######:"+m.group());
                            if(mIndex == 4)
                                partNoEditText.setText(m.group());
                            if(mIndex == 6)
                                quantityEditText.setText(m.group());
                            mIndex++;
                        }
                    }

                    if(shelfEditText.hasFocus()) {
                        qrCodeEditText.requestFocus();
                        mFocusedEditText = qrCodeEditText;
                    }
                 }
            };
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
        //Toast.makeText(this, "注册广播完成，自动接收数据", Toast.LENGTH_LONG).show();

    }

    private void showToast(String datat) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Ray data: " + datat, Toast.LENGTH_SHORT);
//                            "data: " + datat + "\n" + getstr, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, offset);// 居中显示 x,y
        toast.show();
        offset = offset + 50;//设置TOAST显示偏移，便于区分不同次接收到的数据
        if (offset > 400) offset = 0;
    }




}
