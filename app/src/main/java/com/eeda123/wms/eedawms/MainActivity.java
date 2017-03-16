package com.eeda123.wms.eedawms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.AlphabeticIndex;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eeda123.wms.eedawms.model.DbHelper;
import com.eeda123.wms.eedawms.model.GateInDao;
import com.eeda123.wms.eedawms.model.GateInRecord;
import com.eeda123.wms.eedawms.model.GateOutDao;
import com.eeda123.wms.eedawms.model.InvCheckOrderDao;
import com.opencsv.CSVWriter;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private Button mGateInButton = null;
    private Button mCancelGateInButton = null;
    private Button mReturnGateInButton = null;
    private Button mGateOutButton = null;
    private Button mShiftOutButton = null;
    private Button mShiftInButton = null;
    private Button mInvCheckButton = null;
    private Button mInvReCheckButton = null;
    private Button mExportButton = null;
    private EditText userNameText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        userNameText = (EditText)findViewById(R.id.userName);
        addListeners();
    }

    public static String getDate(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    protected void addListeners() {
        mGateInButton = (Button) findViewById(R.id.gateInBtn);
        mGateInButton.setOnClickListener(this);

        mExportButton = (Button) findViewById(R.id.exportBtn);
        mExportButton.setOnClickListener(this);

        mCancelGateInButton = (Button) findViewById(R.id.cancelGateInBtn);
        mCancelGateInButton.setOnClickListener(this);

        mReturnGateInButton = (Button) findViewById(R.id.returnGateInBtn);
        mReturnGateInButton.setOnClickListener(this);

        mGateOutButton = (Button) findViewById(R.id.gateOutBtn);
        mGateOutButton.setOnClickListener(this);

        mShiftOutButton = (Button) findViewById(R.id.shiftOutBtn);
        mShiftOutButton.setOnClickListener(this);

        mShiftInButton = (Button) findViewById(R.id.shiftInBtn);
        mShiftInButton.setOnClickListener(this);

        mInvCheckButton = (Button) findViewById(R.id.invCheckBtn);
        mInvCheckButton.setOnClickListener(this);

        mInvReCheckButton = (Button) findViewById(R.id.invReCheckBtn);
        mInvReCheckButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gateInBtn:
                Intent gateInIntent = new Intent(getApplicationContext(),
                        GateInActivity.class);
                gateInIntent.putExtra(GateInActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(gateInIntent);
                break;
            case R.id.cancelGateInBtn:
                Intent gateInCancelIntent = new Intent(getApplicationContext(),
                        GateInCancelActivity.class);
                gateInCancelIntent.putExtra(GateInCancelActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(gateInCancelIntent);
                break;
            case R.id.returnGateInBtn:
                Intent gateInReturnIntent = new Intent(getApplicationContext(),
                        GateInReturnActivity.class);
                gateInReturnIntent.putExtra(GateInReturnActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(gateInReturnIntent);
                break;
            case R.id.gateOutBtn:
                Intent gateOutIntent = new Intent(getApplicationContext(),
                        GateOutActivity.class);
                gateOutIntent.putExtra(GateOutActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(gateOutIntent);
                break;
            case R.id.shiftOutBtn:
                Intent shiftOutIntent = new Intent(getApplicationContext(),
                        ShiftOutActivity.class);
                shiftOutIntent.putExtra(ShiftOutActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(shiftOutIntent);
                break;
            case R.id.shiftInBtn:
                Intent shiftInIntent = new Intent(getApplicationContext(),
                        ShiftInActivity.class);
                shiftInIntent.putExtra(ShiftInActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(shiftInIntent);
                break;
            case R.id.invCheckBtn:
                Intent invCheckIntent = new Intent(getApplicationContext(),
                        InvCheckActivity.class);
                invCheckIntent.putExtra(InvCheckActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(invCheckIntent);
                break;
            case R.id.invReCheckBtn:
                Intent invReCheckIntent = new Intent(getApplicationContext(),
                        InvReCheckActivity.class);
                invReCheckIntent.putExtra(InvReCheckActivity.USER_NAME, userNameText
                        .getText().toString());
                startActivity(invReCheckIntent);
                break;
            case R.id.exportBtn:
                ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask();
                exportDatabaseCSVTask.doInBackground();
                break;
        }
    }

    //new async task for file export to csv
    private class ExportDatabaseCSVTask extends AsyncTask<String, String, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        boolean memoryErr = false;

        // to show Loading dialog box
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        // to write process
        protected Boolean doInBackground(final String... args) {
            boolean success = false;
            String currentDateString = new SimpleDateFormat(Constants.SimpleDtFrmt_ddMMyyyy_HHmmss).format(new Date());

            File dbFile = getDatabasePath(DbHelper.DB_NAME);
            Log.v("v", "Db path is: " + dbFile); // get the path of db
            File exportDir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.FILE_DIR_NM, "");

            long freeBytesInternal = new File(getApplicationContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
            long megAvailable = freeBytesInternal / 1048576;

            if (megAvailable < 0.1) {
                System.out.println("Please check"+megAvailable);
                memoryErr = true;
            }else {
                String exportDirStr = exportDir.toString();// to show in dialogbox
                Log.v("v", "exportDir path::" + exportDir);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                String msg = null;
                GateInDao gateInDao = new GateInDao(MainActivity.this);
                success = creatCSV(exportDirStr,"入库记录",gateInDao.getList(),currentDateString);
                GateOutDao gateOutDao = new GateOutDao(MainActivity.this);
                success = creatCSV(exportDirStr,"出库记录",gateOutDao.getList(),currentDateString);
                InvCheckOrderDao invCheckDao = new InvCheckOrderDao(MainActivity.this);
                success = creatCSV(exportDirStr,"盘点单",invCheckDao.getList(),currentDateString);
                Toast.makeText(getApplicationContext(), "所有文件已导出："+exportDir, Toast.LENGTH_LONG).show();
                DbHelper database_helper = new DbHelper(MainActivity.this);
                SQLiteDatabase db = database_helper.getWritableDatabase();//这里是获得可写的数据库
                db.execSQL("delete from gate_in ");
                db.execSQL("delete from gate_out ");
                db.execSQL("delete from inv_check_order ");
                db.close();
            }
            return success;
        }

        public boolean creatCSV(String exportDir,String order, ArrayList<HashMap<String, ?>> listdata ,String currentDateString){
            boolean success = false;
            try {
                if(listdata.size() < 1)
                    return true;
                File file = new File(exportDir, order + "_" + currentDateString + ".csv");
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                if (listdata.size() > 0) {
                    for (int index = 0; index < listdata.size(); index++) {
                        HashMap<String, ?> sa = listdata.get(index);
                        if (index == 0) {
                            String data = (sa.keySet().toString()).substring(1, (sa.keySet().toString()).length() - 1);
                            String dataArray[] = data.split(",");
                            csvWrite.writeNext(dataArray);
                        }

                        String arrStr[] = new String[sa.size()];
                        int i = 0;
                        for (Map.Entry<String, ?> m : sa.entrySet()) {
                            arrStr[i] = (String) m.getValue();
                            i++;
                        }
                        csvWrite.writeNext(arrStr);
                    }
                    success = true;
                }
                csvWrite.close();
            } catch (IOException e) {
                Log.e("MainActivity", e.getMessage(), e);
                return success;
            }
            return success;
        }

        // close dialog and give msg
        protected void onPostExecute(Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {

                Log.v("v", "OK...");
            } else {
                if (memoryErr==true) {
                    Log.v("v", "errr...");
                } else {
                    Log.v("v", "errr...");
                }
            }
        }
    }

    public static void disableShowSoftInput(EditText editText)
    {
        if (android.os.Build.VERSION.SDK_INT <= 10)
        {
            editText.setInputType(InputType.TYPE_NULL);
        }
        else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus",boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            }catch (Exception e) {
                // TODO: handle exception
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus",boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            }catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    static AlertDialog adRef = null;
    public static void showAlertDialog(Context context ,String datat ){
        if(adRef!=null) {
            adRef.dismiss();
        }
        adRef = new AlertDialog.Builder(context).setMessage(datat+"\n\n\n\n无需返回即可继续扫描动作").create();
        adRef.show();
    }

}
