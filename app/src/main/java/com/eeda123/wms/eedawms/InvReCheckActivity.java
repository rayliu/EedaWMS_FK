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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.eeda123.wms.eedawms.model.DbHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvReCheckActivity extends AppCompatActivity {
    private EditText orderNoEditText;
    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText checkQuantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;

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

        MainActivity.disableShowSoftInput(shelfEditText);
        MainActivity.disableShowSoftInput(qrCodeEditText);
        MainActivity.disableShowSoftInput(quantityEditText);
        MainActivity.disableShowSoftInput(partNoEditText);
        MainActivity.disableShowSoftInput(orderNoEditText);

        EditText searchView = (EditText) findViewById(R.id.check_quantity);
        searchView.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String qrCode = qrCodeEditText.getText().toString();
                    if(StringUtils.isEmpty(qrCode)){
                        Toast.makeText(getApplicationContext(), "请先扫描QR CODE", Toast.LENGTH_LONG).show();
                        //MainActivity.showAlertDialog(v.getContext(),"请先扫描QR CODE");
                        qrCodeEditText.requestFocus();
                        return;
                    }
                } else {
                    // 此处为失去焦点时的处理内容
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        });

        findViewById(R.id.comfirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbHelper database_helper = new DbHelper(InvReCheckActivity.this);
                SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

                String order_no = orderNoEditText.getText().toString();
                String qrCode = qrCodeEditText.getText().toString();
                String part_no = partNoEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String check_quantity = checkQuantityEditText.getText().toString();
                String shelf = shelfEditText.getText().toString();
                String userName = getIntent().getStringExtra(USER_NAME);

                if(StringUtils.isNotEmpty(qrCode)&&StringUtils.isNotEmpty(check_quantity)){
                    db.execSQL("update inv_check_order set check_quantity=?,check_time=? where qr_code=?",
                            new Object[] { check_quantity, MainActivity.getDate(), qrCode });
                    Toast.makeText(getApplicationContext(), "复核成功!", Toast.LENGTH_LONG).show();
                    db.close();
                    clearDate();
                }else{
                    Toast.makeText(getApplicationContext(), "复核数量不能为空", Toast.LENGTH_LONG).show();
                    checkQuantityEditText.requestFocus();
                }
            }
        });

        findViewById(R.id.listBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        ListActivity.class);
                intent.putExtra(ListActivity.page_type,"invReCheck");
                startActivity(intent);
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
                    Matcher m= Pattern.compile("[^\\(\\)]+").matcher(datat);
                    if(qrCodeEditText.hasFocus()) {
                        List<String> list = new ArrayList<String>();
                        while (m.find()) {
                            list.add(m.group());
                        }


                        if(list.size()>=3){
                            String quantity = list.get(list.size()-1);
                            String partNo = list.get(list.size()-3);
                            qrCodeEditText.setText(datat);
                            partNoEditText.setText(partNo);
                            quantityEditText.setText(quantity);
                            if(StringUtils.isNotEmpty(datat)){
                                DbHelper database_helper = new DbHelper(InvReCheckActivity.this);
                                SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库
                                Cursor cursor = db.rawQuery("select * from inv_check_order where qr_code = '"+datat+"'", null);
                                if(!cursor.moveToNext()){
                                    MainActivity.showAlertDialog(context,"此货品未进行盘点，请先盘点!\n\n编码："+partNoEditText.getText().toString()
                                            +"\n"+"数量："+quantityEditText.getText());
                                    clearDate();
                                    qrCodeEditText.requestFocus();
                                }else {
                                    String order_no = cursor.getString(cursor.getColumnIndex("order_no"));
                                    String shelves  = cursor.getString(cursor.getColumnIndex("shelves"));
                                    orderNoEditText.setText(order_no);
                                    shelfEditText.setText(shelves);
                                    MainActivity.showAlertDialog(context,
                                            "盘点单号：" +order_no
                                                    +"\n"+"编码：" +partNoEditText.getText()
                                                    +"\n"+"库位：" +shelves
                                                    +"\n"+"数量：" +quantityEditText.getText());
                                    checkQuantityEditText.requestFocus();
                                    cursor.close();
                                    db.close();
                                }
                            }
                        }else{
                            MainActivity.showAlertDialog(context,"QR CODE格式无法识别");
                        }
                    };
                }
            };
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
    }
}
