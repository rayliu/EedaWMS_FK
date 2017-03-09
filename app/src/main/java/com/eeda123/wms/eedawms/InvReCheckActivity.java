package com.eeda123.wms.eedawms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.eeda123.wms.eedawms.model.DbHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvReCheckActivity extends AppCompatActivity {
    private int offset = 0;
    private EditText orderNoEditText;
    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText checkQuantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;

    private EditText mFocusedEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_recheck);

        getSupportActionBar().setHomeButtonEnabled(true);//返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getsystemscandata();
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

    protected void findViewById() {
        orderNoEditText  = (EditText) findViewById(R.id.order_no);
        qrCodeEditText = (EditText) findViewById(R.id.qrCodeEditText);
        partNoEditText = (EditText) findViewById(R.id.part_no);
        quantityEditText = (EditText) findViewById(R.id.quantity);
        checkQuantityEditText = (EditText) findViewById(R.id.check_quantity);
        shelfEditText = (EditText) findViewById(R.id.shelfEditText);

        mFocusedEditText = shelfEditText;
        //qrCodeEditText.setOnFocusChangeListener(focusListener);

        findViewById(R.id.comfirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //unregisterReceiver(mBrReceiver);
                //Toast.makeText(getApplicationContext(), "反注册广播完成", Toast.LENGTH_SHORT).show();

                DbHelper database_helper = new DbHelper(InvReCheckActivity.this);
                SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

                String order_no = orderNoEditText.getText().toString();
                String qrCode = qrCodeEditText.getText().toString();
                String part_no = partNoEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String check_quantity = checkQuantityEditText.getText().toString();
                String shelf = shelfEditText.getText().toString();
                String userName = getIntent().getStringExtra(USER_NAME);

                db.execSQL("update inv_check_order set check_quantity=?,check_time=? where qr_code=?",
                        new Object[] { check_quantity, MainActivity.getDate(), qrCode });
                db.close();
                clearDate();
            }
        });
    };

    public void clearDate(){
        qrCodeEditText.setText("");
        orderNoEditText.setText("");
        partNoEditText.setText("");
        quantityEditText.setText("");
        checkQuantityEditText.setText("");
        shelfEditText.setText("");

        qrCodeEditText.requestFocus();
        mFocusedEditText = qrCodeEditText;

        Toast.makeText(this, "复核成功!", Toast.LENGTH_SHORT).show();
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

                        if(StringUtils.isNotEmpty(datat)){
                            DbHelper database_helper = new DbHelper(InvReCheckActivity.this);
                            SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库
                            Cursor cursor = db.rawQuery("select * from inv_check_order where qr_code = '"+datat+"'", null);
                            while (cursor.moveToNext()) {
                                int id = cursor.getInt(0); //获取第一列的值,第一列的索引从0开始
                                String order_no = cursor.getString(1);
                                String shelves  = cursor.getString(6);
                                orderNoEditText.setText(order_no);
                                shelfEditText.setText(shelves);
                            }
                            cursor.close();
                            db.close();
                        }
                    }

                    if(qrCodeEditText.hasFocus()) {
                        checkQuantityEditText.requestFocus();
                        mFocusedEditText = checkQuantityEditText;
                    }
                }
            };
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
    }
}
