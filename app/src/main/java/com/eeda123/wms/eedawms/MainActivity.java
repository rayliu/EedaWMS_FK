package com.eeda123.wms.eedawms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eeda123.wms.eedawms.model.DbHelper;
import com.eeda123.wms.eedawms.model.GateInDao;
import com.eeda123.wms.eedawms.model.GateInRecord;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


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
                try {
                    GateInDao gateInDao=new GateInDao(MainActivity.this);
                    ArrayList<HashMap<String, ?>> listdata = gateInDao.getList();


                    String lob = null;
                    for (int index = 0; index < listdata.size();) {
                        HashMap<String, ?> gateInRecord = listdata.get(index);
                        lob = (String)gateInRecord.get("qr_code");
                        break;
                    }
//                    if (Constants.Common.OCEAN_LOB.equals(lob)) {
//
//                        file = new File(exportDir, Constants.FileNm.FILE_OFS + currentDateString + ".csv");
//                    } else {
//                        file = new File(exportDir, Constants.FileNm.FILE_AFS + currentDateString + ".csv");
//                    }
                    File file = new File(exportDir,  "入库记录_"+currentDateString + ".csv");
                    file.createNewFile();
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(file));


                    // this is the Column of the table and same for Header of CSV
                    // file
//                    if (Constants.Common.OCEAN_LOB.equals(lob)) {
//                        csvWrite.writeNext(Constants.FileNm.CSV_O_HEADER);
//                    }else{
//                        csvWrite.writeNext(Constants.FileNm.CSV_A_HEADER);
//                    }
                    String arrStr1[] = { "SR.No", "CUTSOMER NAME", "PROSPECT", "PORT OF LOAD", "PORT OF DISCHARGE" };
                    csvWrite.writeNext(arrStr1);

                    if (listdata.size() > 0) {
                        for (int index = 0; index < listdata.size(); index++) {
                            HashMap<String, ?> sa = listdata.get(index);
                            String pol;
                            String pod;
//                            if (Constants.Common.OCEAN_LOB.equals(sa.getLob())) {
//                                pol = sa.getPortOfLoadingOENm();
//                                pod = sa.getPortOfDischargeOENm();
//                            } else {
//                                pol = sa.getAirportOfLoadNm();
//                                pod = sa.getAirportOfDischargeNm();
//                            }
                            int srNo = index;
                            String arrStr[] = { String.valueOf(sa.get("id")), String.valueOf(sa.get("qr_code")),
                                    String.valueOf(sa.get("part_no")), String.valueOf(sa.get("quantity")) };
                            csvWrite.writeNext(arrStr);
                        }
                        success = true;
                    }
                    csvWrite.close();
                    Toast.makeText(getApplicationContext(), "所有文件已导出："+exportDir, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e("MainActivity", e.getMessage(), e);
                    return success;
                }
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

}
