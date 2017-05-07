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

import com.eeda123.wms.eedawms.model.DbHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GateOutActivity extends AppCompatActivity {
    private int offset = 0;
    private EditText orderNoText;
    private EditText qrCodeEditText;
    private EditText partNoEditText;
    private EditText quantityEditText;
    private EditText shelfEditText;
    public static String USER_NAME;
    private EditText shelfTotalText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_out);

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
        orderNoText = (EditText) findViewById(R.id.order_no);
        qrCodeEditText = (EditText) findViewById(R.id.qrCodeEditText);
        partNoEditText = (EditText) findViewById(R.id.part_no);
        quantityEditText = (EditText) findViewById(R.id.quantity);
        shelfEditText = (EditText) findViewById(R.id.shelfEditText);
        shelfTotalText = (EditText) findViewById(R.id.shelfTotal);

        //MainActivity.disableShowSoftInput(OrderNoText);
        MainActivity.disableShowSoftInput(shelfEditText);
        MainActivity.disableShowSoftInput(qrCodeEditText);
        MainActivity.disableShowSoftInput(quantityEditText);
        MainActivity.disableShowSoftInput(partNoEditText);
        MainActivity.disableShowSoftInput(shelfTotalText);
        orderNoText.hasFocusable();

        findViewById(R.id.listBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        ListActivity.class);
                intent.putExtra(ListActivity.page_type,"gateOut");
                startActivity(intent);
            }
        });
    };

    public void confirmOrder(Context context) {
        DbHelper database_helper = new DbHelper(GateOutActivity.this);
        SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库

        String order_no = orderNoText.getText().toString();
        String qrCode = qrCodeEditText.getText().toString();
        String part_no = partNoEditText.getText().toString();
        String quantity = quantityEditText.getText().toString();
        String userName = getIntent().getStringExtra(USER_NAME);

        Cursor cursor = db.rawQuery("select * from gate_out where qr_code = '"+qrCode+"'", null);
        while (cursor.moveToNext()) {
            MainActivity.showAlertDialog(context,"此货品不能二次出库!\n\n编码："+partNoEditText.getText().toString()+"\n"+"数量："
                    +quantityEditText.getText());
            //Toast.makeText(getApplicationContext(), "此货品不能二次出库!", Toast.LENGTH_LONG).show();
            clearDate();
            return;
        }

        db.execSQL("insert into gate_out(order_no, qr_code, part_no, quantity,creator,create_time)" +
                " values ('"+order_no+"','"+qrCode+"','"+part_no+"','"+quantity+"','"+userName+"','"+MainActivity.getDate()+"')");

        MainActivity.showAlertDialog(context,"出库成功!\n\n编码："+partNoEditText.getText().toString()+"\n"+"数量："
                +quantityEditText.getText());
        clearDate();
        getShelfTotal();
        //Toast.makeText(getApplicationContext(), "出库成功!", Toast.LENGTH_LONG).show();
    }

    public void getShelfTotal(){
        String order_no = orderNoText.getText().toString();
        //统计当前货架总数量
        DbHelper database_helper = new DbHelper(GateOutActivity.this);
        SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库
        Cursor cursor2 = db.rawQuery("select count(1) total from gate_out where order_no = '"+order_no+"' ", null);
        while (cursor2.moveToNext()) {
            shelfTotalText.setText(cursor2.getString(0));
        }
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
                            confirmOrder(context);
                        }else{
                            MainActivity.showAlertDialog(context,"QR CODE格式无法识别");
                        }
                    };

                    if(orderNoText.hasFocus()) {
                        orderNoText.setText(datat);
                        qrCodeEditText.requestFocus();
                        MainActivity.showAlertDialog(context,datat);
                        getShelfTotal();
                    }
                };
            }
        };
        IntentFilter filter = new IntentFilter(getstr);
        registerReceiver(mBrReceiver, filter);
    }
}
