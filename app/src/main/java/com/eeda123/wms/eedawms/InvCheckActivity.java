package com.eeda123.wms.eedawms;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.eeda123.wms.eedawms.model.DbHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvCheckActivity extends AppCompatActivity {
    private EditText orderNoEditText;
    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_check);

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
        shelfEditText = (EditText) findViewById(R.id.shelfEditText);

        MainActivity.disableShowSoftInput(shelfEditText);
        MainActivity.disableShowSoftInput(qrCodeEditText);
        MainActivity.disableShowSoftInput(quantityEditText);
        MainActivity.disableShowSoftInput(partNoEditText);

        EditText searchView = (EditText) findViewById(R.id.order_no);
        searchView.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                } else {
                    // 此处为失去焦点时的处理内容
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        });

        findViewById(R.id.nextShelfBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDate();
                shelfEditText.setText("");
                shelfEditText.requestFocus();
            }
        });

        findViewById(R.id.listBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        ListActivity.class);
                intent.putExtra(ListActivity.page_type,"invCheck");
                startActivity(intent);
            }
        });
    };

    public static void hideInputMethod(Activity activity) {
        if (null == activity) {
            return;
        }
        if (null != activity.getCurrentFocus() && null != activity.getCurrentFocus().getWindowToken())
        {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void confirmOrder(Context context) {
        DbHelper database_helper = new DbHelper(InvCheckActivity.this);
        SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

        String order_no = orderNoEditText.getText().toString();
        String qrCode = qrCodeEditText.getText().toString();
        String part_no = partNoEditText.getText().toString();
        String quantity = quantityEditText.getText().toString();
        String shelf = shelfEditText.getText().toString();
        String userName = getIntent().getStringExtra(USER_NAME);

        Cursor cursor = db.rawQuery("select shelves from inv_check_order where qr_code = '"+qrCode+"'", null);
        while (cursor.moveToNext()) {
            MainActivity.showAlertDialog(context,"此货品已重复盘点!\n\n编码："+partNoEditText.getText().toString()+"\n"+"数量："
                    +quantityEditText.getText()+"\n"+"货架："
                    +cursor.getString(0));
            //Toast.makeText(getApplicationContext(), "此货品已重复盘点!", Toast.LENGTH_LONG).show();
            clearDate();
            return;
        }

        db.execSQL("insert into inv_check_order(order_no,qr_code, part_no, quantity, shelves,creator,create_time)" +
                " values ('"+order_no+"','"+qrCode+"','"+part_no+"','"+quantity+"','"+shelf+"','"+userName+"','"+MainActivity.getDate()+"')");
        MainActivity.showAlertDialog(context,"确认成功!\n\n编码："+partNoEditText.getText().toString()+"\n"+"数量："
                +quantityEditText.getText());
        clearDate();
        //Toast.makeText(getApplicationContext(), "确认成功!", Toast.LENGTH_SHORT).show();
    }

    public void clearDate(){
        qrCodeEditText.setText("");
        partNoEditText.setText("");
        quantityEditText.setText("");

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
                    if(StringUtils.isEmpty(orderNoEditText.getText())){
                        MainActivity.showAlertDialog(context,"请先填写盘点单号");
                        orderNoEditText.requestFocus();
                        return;
                    }

                    if(qrCodeEditText.hasFocus()) {
                        if(StringUtils.isEmpty(shelfEditText.getText())){
                            MainActivity.showAlertDialog(context,"请先扫描货架号");
                            shelfEditText.requestFocus();
                            return;
                        }

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
                            if(StringUtils.isNotEmpty(shelfEditText.getText())){
                                confirmOrder(context);
                            }else{
                                MainActivity.showAlertDialog(context,"货架号不能为空");
                            }
                        }else{
                            MainActivity.showAlertDialog(context,"QR CODE格式无法识别");
                        }
                    }

                    if(shelfEditText.hasFocus()) {
                        if(datat.length()<7 || datat.length()>12){
                            MainActivity.showAlertDialog(context,"货架格式无法识别");
                        }else{
                            shelfEditText.setText(datat);
                            qrCodeEditText.requestFocus();
                            MainActivity.showAlertDialog(context,datat);
                        }
                    }
                }
            };
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
    }
}
