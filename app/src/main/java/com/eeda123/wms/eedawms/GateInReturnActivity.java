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

public class GateInReturnActivity extends AppCompatActivity {
    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_in_return);

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
        qrCodeEditText = (EditText) findViewById(R.id.qrCodeEditText);
        partNoEditText = (EditText) findViewById(R.id.part_no);
        quantityEditText = (EditText) findViewById(R.id.quantity);
        shelfEditText = (EditText) findViewById(R.id.shelfEditText);

        MainActivity.disableShowSoftInput(shelfEditText);
        MainActivity.disableShowSoftInput(qrCodeEditText);
        MainActivity.disableShowSoftInput(quantityEditText);
        MainActivity.disableShowSoftInput(partNoEditText);

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
                intent.putExtra(ListActivity.page_type,"gateInReturn");
                startActivity(intent);
            }
        });
    };

    public void confirmOrder(Context context) {
        DbHelper database_helper = new DbHelper(GateInReturnActivity.this);
        SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

        String qrCode = qrCodeEditText.getText().toString();
        String part_no = partNoEditText.getText().toString();
        String quantity = quantityEditText.getText().toString();
        String shelf = shelfEditText.getText().toString();
        String userName = getIntent().getStringExtra(USER_NAME);

        Cursor cursor = db.rawQuery("select * from gate_in where qr_code = '"+qrCode+"'", null);
        while (cursor.moveToNext()) {
            MainActivity.showAlertDialog(context,"库存中已存在此货品!\n\n编码："+partNoEditText.getText().toString()+"\n"+"数量："
                    +quantityEditText.getText());
            //Toast.makeText(getApplicationContext(), "库存中已存在此货品!", Toast.LENGTH_LONG).show();
            clearDate();
            return;
        }

        db.execSQL("insert into gate_in(qr_code, part_no, quantity, shelves,creator,create_time,return_flag)" +
                " values ('"+qrCode+"','"+part_no+"','"+quantity+"','"+shelf+"','"+userName+"','"+MainActivity.getDate()+"','Y')");

        MainActivity.showAlertDialog(context,"入库成功\n\n编码："+partNoEditText.getText().toString()+"\n"+"数量："
                +quantityEditText.getText());
        clearDate();
        //Toast.makeText(getApplicationContext(), "入库成功!", Toast.LENGTH_LONG).show();
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
                    if(qrCodeEditText.hasFocus()) {
                        if(StringUtils.isEmpty(shelfEditText.getText())){
                            MainActivity.showAlertDialog(context,"请先扫描货架号");
                            shelfEditText.requestFocus();
                            return;
                        }
                        int mIndex = 0;
                        Matcher m= Pattern.compile("[^\\(\\)]+").matcher(datat);
                        while(m.find()) {
                            if (mIndex == 4)
                                partNoEditText.setText(m.group());
                            if (mIndex == 6)
                                quantityEditText.setText(m.group());
                            mIndex++;
                        }

                        if(mIndex>1){
                            qrCodeEditText.setText(datat);
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
//                        if(datat.length()>7){
//                            MainActivity.showAlertDialog(context,"货架格式无法识别");
//                        }else{
                            shelfEditText.setText(datat);
                            qrCodeEditText.requestFocus();
                            MainActivity.showAlertDialog(context,datat);
//                        }
                    }
                }
            };
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
    }
}
