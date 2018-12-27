package com.aero.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.R;
import com.aero.custom.utility.AppConstant;
import com.aero.custom.utility.AppUtilityFunction;
import com.aero.db.CommonDatabaseAero;
import com.aero.pojos.request.CreateProfileRequestModel;
import com.aero.pojos.request.QRCodeRequestModel;
import com.aero.pojos.request.ReportUsRequestModel;
import com.aero.pojos.response.CreateProfileResponseModel;
import com.aero.pojos.response.LoginResponse;
import com.aero.pojos.response.QRCodeResponseModel;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.customComponent.CustomAlert;
import com.customComponent.Networking.CustomVolley;
import com.customComponent.Networking.VolleyTaskListener;
import com.customComponent.utility.ProjectPrefrence;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by SUNAINA on 27-11-2018.
 */

public class WriteFeedbackActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, ZXingScannerView.ResultHandler {
    private WriteFeedbackActivity activity;
    private Context context;
    private LoginResponse loginResponse;
    private RelativeLayout backLayout;
    private TextView headerTV;
    private Button submitBTN, okBTN;
    String qrcodeNumber = "", serviceName = "", zone = "", serviceTag = "";
    private RatingBar ratingBar;
    private TextView selectedExhibitorTv;
    String[] writeForArr = {"-Select-", "Exhibitor", "Seminar", "Conference", "Others"};
    public static String TAG = "TAG";
    String[] serviceArr = {"-Select Service-", "Toilet", "WiFi"};

    String[] commnetArr = {};
/*    String[] commnetArr = {"Washroom needs cleaning", "Washroom drain is clogged", "Washroom has foul smell", "Access area to washroom needs cleaning", "Refill hand soap", "Refill paper towels"
            , "No water available", "Water tap is leaking", "Door lock is broken",
            "Light does not work", "Washroom is nice & clean,Great Job!"};*/
    private String selectedCmnt;

    //
      /*View*/
    RelativeLayout lnr_scan;
    TextView txt_scan_gallery;
    ViewGroup contentFrame;
    private Spinner cmntSpinner;
    /*Scan QR Code*/
    private ZXingScannerView mScannerView;
    int GALLARY_CODE = 1;
    int CROPING_CODE = 2;
    private static final int PERMISSION_REQUEST_CODE = 1000, STORAGE_CAMERA_REQUST_CODE = 2000;

    /*Image*/
    private Uri mImageCaptureUri;
    private File outPutFile = null;
    String path = "";
    Bitmap bitmap;
    File file;
    String pay_num = "";
    // PushPaymentData qrcode;

    /*Get Data*/
    String accountName = "", account;
    boolean isQrcodeText = false;
    private TextView serviceNameTv, zoneTV, serviceIssueTV;
    private EditText qrcodeNumberET;
    private LinearLayout ratingLL, zoneLL, serviceNameLL;
    private QRCodeResponseModel qrCodeResponseModel, reportUsResponseModel;
    private CustomVolley volley;
    private long rating = 0;
    private EditText commentET;
    private TextView rescan;
    private Dialog dialog;
    String selectedOption = "", scanStatus = "";
    private Spinner spin, serviceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_feedback);
        activity = this;
        context = this;
        setupScreen();
    }

    private void setupScreen() {
        getSupportActionBar().hide();
        loginResponse = LoginResponse.create(ProjectPrefrence.getSharedPrefrenceData(AppConstant.PROJECT_PREF, AppConstant.LOGIN_DETAILS, context));
        backLayout = (RelativeLayout) findViewById(R.id.backLayout);
        lnr_scan = (RelativeLayout) findViewById(R.id.lnr_scan);
        submitBTN = (Button) findViewById(R.id.submitBTN);
        okBTN = (Button) findViewById(R.id.okBTN);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        headerTV = (TextView) findViewById(R.id.headerTV);
        zoneTV = (TextView) findViewById(R.id.zoneTV);
        serviceIssueTV = (TextView) findViewById(R.id.serviceIssueTV);
        qrcodeNumberET = (EditText) findViewById(R.id.qrcodeNumberET);
        rescan = (TextView) findViewById(R.id.rescan);
        rescan.setPaintFlags(rescan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        selectedExhibitorTv = (TextView) findViewById(R.id.selectedExhibitorTv);
        headerTV.setText("Report Us");

        contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        //headerTV.setText("Feedback");
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        serviceNameTv = (TextView) findViewById(R.id.serviceNameTv);
        commentET = (EditText) findViewById(R.id.commentET);
        // commentET.setVisibility(View.GONE);
        zoneLL = (LinearLayout) findViewById(R.id.zoneLL);
        serviceNameLL = (LinearLayout) findViewById(R.id.serviceNameLL);
        ratingLL = (LinearLayout) findViewById(R.id.ratingLL);
        ratingLL.setVisibility(View.GONE);
     spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, writeForArr);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);




        serviceSpinner = (Spinner) findViewById(R.id.serviceSpinner);
        serviceSpinner.setEnabled(true);


        final ArrayAdapter serviceAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceArr);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        serviceSpinner.setAdapter(serviceAdapter);

            serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view,
                                           int position, long id) {

                        if (position == 0) {

                        } else if (position == 1) {
                            String service = serviceSpinner.getSelectedItem().toString();
                            scanStatus = "Manual";
                            ratingLL.setVisibility(View.VISIBLE);
                            zoneLL.setVisibility(View.GONE);
                            serviceNameLL.setVisibility(View.GONE);
                            serviceTag=service;
                            parseQRCode(service);


                        } else if (position == 2) {
                            String service = serviceSpinner.getSelectedItem().toString();
                            scanStatus = "Manual";
                            ratingLL.setVisibility(View.VISIBLE);
                            zoneLL.setVisibility(View.GONE);
                            serviceNameLL.setVisibility(View.GONE);
                            serviceTag=service;
                            parseQRCode(service);
                        }



                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // TODO Auto-generated method stub

                }
            });



        cmntSpinner = (Spinner) findViewById(R.id.commentSpinner);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter comments = new ArrayAdapter(this, android.R.layout.simple_spinner_item, commnetArr);
        comments.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        cmntSpinner.setAdapter(comments);
        cmntSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
               /*     if (position == 12) {
                        commentET.setVisibility(View.VISIBLE);
                    } else {
               *//*     selectedCmnt=cmntSpinner.getSelectedItem().toString();
                    commentET.setVisibility(View.GONE);*//*

                    }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });

        serviceIssueTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList itemsSelected = new ArrayList();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Report Washroom");
                builder.setMultiChoiceItems(commnetArr, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedItemId,
                                                boolean isSelected) {
                                if (isSelected) {
                                    String value = commnetArr[selectedItemId];
                                    selectedOption = selectedOption + "," + value;
                                    itemsSelected.add(selectedItemId);

                                    Log.d("Items code select", selectedOption);
                                } else if (itemsSelected.contains(selectedItemId)) {
                                    itemsSelected.remove(Integer.valueOf(selectedItemId));

                                }
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                ListView list = ((AlertDialog) dialog).getListView();
                                // make selected item in the comma seprated string<br />
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < list.getCount(); i++) {
                                    boolean checked = list.isItemChecked(i);
                                    if (checked) {
                                        if (stringBuilder.length() > 0)
                                            stringBuilder.append(",");
                                        stringBuilder.append(list.getItemAtPosition(i));

                                    }
                                }

                                if (stringBuilder.toString().length() > 0) {
                                    serviceIssueTV.setText(stringBuilder);
                                } else {
                                    stringBuilder.setLength(0);
                                    serviceIssueTV.setText("Select Options");
                                }
                                //Your logic when OK button is clicked
                                //  Log.d("Items code",selectedOption);
                                // serviceIssueTV.setText(selectedOption.replaceFirst(",",""));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                dialog = builder.create();
                dialog.show();
            }
        });


        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rating = 0;
                selectedCmnt = commentET.getText().toString();

              /*  if (cmntSpinner.getSelectedItem().toString().equalsIgnoreCase("-Select option-")) {
                    CustomAlert.alertWithOk(context, "Please select your option");
                    return;

                } else if (cmntSpinner.getSelectedItem().toString().equalsIgnoreCase("Other")) {
                    selectedCmnt = commentET.getText().toString();
                } else {
                    selectedCmnt = cmntSpinner.getSelectedItem().toString();
                }*/

              if(qrcodeNumberET.getText().toString().equalsIgnoreCase("")){
                  CustomAlert.alertWithOk(context, "Please enter QR Code number.");
                  return;
              }

                if(serviceIssueTV.getText().toString().equalsIgnoreCase("")
                        || serviceIssueTV.getText().toString().equalsIgnoreCase("-Select option-")){
                    CustomAlert.alertWithOk(context, "Please select service issue.");
                    return;
                }

                if (selectedCmnt.equalsIgnoreCase("")) {
                    CustomAlert.alertWithOk(context, "Please enter comment to submit report.");
                    return;
                } else {
                    prepareSubmitReportData();

                }


            }
        });
        rescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingLL.setVisibility(View.GONE);
                rescan.setVisibility(View.GONE);
                lnr_scan.setVisibility(View.VISIBLE);
                serviceIssueTV.setText("-Select option-");
                qrcodeNumberET.setText("");
                qrcodeNumberET.setHint("QR Code Number");
            }
        });

        okBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanStatus = "Manual";
                ratingLL.setVisibility(View.VISIBLE);
                zoneLL.setVisibility(View.GONE);
                serviceNameLL.setVisibility(View.GONE);

            }
        });
    }

    private void prepareSubmitReportData() {
        //    if (qrCodeResponseModel != null) {
        ReportUsRequestModel reportUsRequestModel = new ReportUsRequestModel();
        reportUsRequestModel.setComment(selectedCmnt);
        if (loginResponse != null && loginResponse.getCompany() != null &&
                loginResponse.getCompany().getCompanyId() != 0) {
            reportUsRequestModel.setCreatedBy(loginResponse.getCompany().getCompanyId());
        }
        // reportUsRequestModel.setRating(rating);

           /* if (qrCodeResponseModel.getQrcodeNumber() != null
                    && !qrCodeResponseModel.getQrcodeNumber().equalsIgnoreCase("")) {
                reportUsRequestModel.setQrcodeNumber(qrCodeResponseModel.getQrcodeNumber());
            }*/
        reportUsRequestModel.setQrcodeNumber(qrcodeNumberET.getText().toString());
        Long dateTime = Calendar.getInstance().getTimeInMillis();
        reportUsRequestModel.setDateTime(dateTime);
        reportUsRequestModel.setScanStatus(scanStatus);
        reportUsRequestModel.setServiceIssue(serviceIssueTV.getText().toString());
        reportUsRequestModel.setServiceName(serviceName);
        reportUsRequestModel.setCategory(serviceTag);
            /*if (qrCodeResponseModel.getServiceCode() != null
                    && !qrCodeResponseModel.getServiceCode().equalsIgnoreCase("")) {
                reportUsRequestModel.setServiceCode(qrCodeResponseModel.getServiceCode());
            }*/
        if (AppUtilityFunction.isNetworkAvailable(context)) {
            Log.d("ReportUs request db:", reportUsRequestModel.serialize());
            submitReportUsData(reportUsRequestModel);
        } else {
            Log.d("ReportUs request db:", reportUsRequestModel.serialize());
            long value = CommonDatabaseAero.saveServiceComplaintRequest(context, reportUsRequestModel);
            if (value != 0) {
                CustomAlert.alertOkWithFinish(context, "Your report has been saved. It will be synced to server when you come online.");
            } else {
                CustomAlert.alertWithOk(context, "Failed");
            }
        }
        // }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        //Toast.makeText(getApplicationContext(),writeForArr[position] , Toast.LENGTH_LONG).show();
        if (position == 1) {
            Intent theIntent = new Intent(context, ExhibitorListActivity.class);
            startActivityForResult(theIntent, 55);
        } else {

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 55) {
            if (data != null) {
                selectedExhibitorTv.setVisibility(View.VISIBLE);
                String name = data.getStringExtra("NAME");
                selectedExhibitorTv.setText("Selected Exhibitor - " + name);
            }
        }

        /*if (requestCode == 13) {
            CallCamera();
        }*/

        // qr code result

    }

    public void CameraPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            } else {
                CallCamera();
            }
        } else {
            CallCamera();
        }
    }

    private void CallCamera() {
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        pay_num = result.getText();

        //    Toast.makeText(context, pay_num, Toast.LENGTH_LONG).show();

        if (pay_num != null && !pay_num.equalsIgnoreCase("")) {
            String[] qrcodeParam = pay_num.split("-");
            ratingLL.setVisibility(View.VISIBLE);
            okBTN.setVisibility(View.GONE);
            serviceSpinner.setSelection(0);
            serviceSpinner.setEnabled(false);
            rescan.setVisibility(View.VISIBLE);
            scanStatus = "QR Code";
            if (qrcodeParam != null) {
                if (qrcodeParam[0] != null && !qrcodeParam[0].equalsIgnoreCase("")) {
                    qrcodeNumber = qrcodeParam[0];
                    qrcodeNumberET.setText(qrcodeNumber);
                    qrcodeNumberET.setEnabled(false);
                }

                if (qrcodeParam[1] != null && !qrcodeParam[1].equalsIgnoreCase("")) {
                    serviceName = qrcodeParam[1];
                    serviceNameTv.setText(serviceName);
                }

                if (qrcodeParam[2] != null && !qrcodeParam[2].equalsIgnoreCase("")) {
                    zone = qrcodeParam[2];
                    zoneTV.setText(zone);
                }

                if (qrcodeParam[3] != null && !qrcodeParam[3].equalsIgnoreCase("")) {
                    serviceTag = qrcodeParam[3];

                }
            }

            // String qrcodeNumber = "", latitude = "", longitude = "";
            /*if (qrcodeParam != null) {
                if (qrcodeParam[0] != null && !qrcodeParam[0].equalsIgnoreCase("")) {
                    qrcodeNumber = qrcodeParam[0];
                }
                if (qrcodeParam[1] != null && !qrcodeParam[1].equalsIgnoreCase("")) {
                    String[] latLong = qrcodeParam[1].split(",");
                    latitude = latLong[0];
                    longitude = latLong[1];
                }

                prepareQRCodeRequest(qrcodeNumber, latitude, longitude);
            }*/
        }
        //  isQrcodeText = pay_num.contains("60019999");

        parseQRCode(serviceTag);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(WriteFeedbackActivity.this);
            }
        }, 1000);
    }

    public String loadServiceComplaintJSONFromAsset() {
        String json = null;
        try {
            InputStream is = activity.getAssets().open("ServiceComplaint.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void parseQRCode(String code) {
        try {

            JSONObject jsonObject = new JSONObject(loadServiceComplaintJSONFromAsset());
            if (code.equalsIgnoreCase("Toilet")) {
                JSONArray jsonArray = jsonObject.getJSONArray("toiletList");
                String[] toiletArray = new String[jsonArray.length()];
                for (int i = 0, count = jsonArray.length(); i < count; i++) {
                    try {
                        String jsonString = jsonArray.getString(i);
                        toiletArray[i] = jsonString.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                commnetArr=toiletArray;

            } else if (code.equalsIgnoreCase("WiFi")) {
                JSONArray jsonArray = jsonObject.getJSONArray("wifiList");
                String[] wifiArray = new String[jsonArray.length()];
                for (int i = 0, count = jsonArray.length(); i < count; i++) {
                    try {
                        String jsonString = jsonArray.getString(i);
                        wifiArray[i] = jsonString.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                commnetArr=wifiArray;
            }

        } catch (Exception e) {
            // CM.ShowDialogNoInternet(activity, activity.getResources().getString(R.string.invalid_qr_code), false);
        }

    }

    private void prepareQRCodeRequest(String qrcodeNumber, String latitude, String longitude) {
        QRCodeRequestModel requestModel = new QRCodeRequestModel();
        requestModel.setLatitude(latitude);
        requestModel.setLongitude(longitude);
        requestModel.setQrcodeNumber(qrcodeNumber);
        if (loginResponse != null && loginResponse.getUser() != null &&
                loginResponse.getUser().getUserId() != null) {
            requestModel.setUserId(loginResponse.getUser().getUserId());
        }
        getQRCodeData(requestModel);

    }

    private void getQRCodeData(QRCodeRequestModel request) {
        VolleyTaskListener taskListener = new VolleyTaskListener() {
            @Override
            public void postExecute(String response) {
                qrCodeResponseModel = QRCodeResponseModel.create(response);
                if (qrCodeResponseModel.isStatus()) {
                    if (qrCodeResponseModel != null && qrCodeResponseModel.getServiceName() != null) {
                        ratingLL.setVisibility(View.VISIBLE);
                        rescan.setVisibility(View.VISIBLE);
                        lnr_scan.setVisibility(View.GONE);
                        setProfileData();
                    }
                    /*ProjectPrefrence.saveSharedPrefrenceData(AppConstant.PROJECT_PREF, AppConstant.LOGIN_DETAILS, loginResponse.serialize(), activity);

                    Intent intent=new Intent(activity,DashboardActivity.class);
                    startActivity(intent);*/
                    // dialog.dismiss();
                    //  Toast.makeText(context, "Create Profile Successfully.", Toast.LENGTH_LONG).show();


                    return;
                } else {

                    Toast.makeText(context, qrCodeResponseModel.getErrorMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            @Override
            public void onError(VolleyError error) {
                if (error != null) {
                    // String s = new String(error.networkResponse.data);
                    //  Log.d("ERROR MSG", s);
                    if (error instanceof TimeoutError) {
                        CustomAlert.alertWithOk(context, context.getResources().getString(R.string.timeout_issue));
                    } else if (AppUtilityFunction.isServerProblem(error)) {
                        // Toast.makeText(getApplicationContext(),R.string.LOGIN_FAILED,Toast.LENGTH_LONG).show();

                        CustomAlert.alertWithOk(context, context.getResources().getString(R.string.server_issue));
                    } else if (AppUtilityFunction.isNetworkProblem(error)) {
                        CustomAlert.alertWithOk(context, context.getResources().getString(R.string.IO_ERROR));
                    }
                }
            }
        };
        volley = new CustomVolley(taskListener, "Please wait..", AppConstant.GET_QR_CODE, request.serialize(), null, null, context);
        volley.execute();
    }

    private void setProfileData() {
        if (qrCodeResponseModel != null && qrCodeResponseModel.getServiceName() != null) {
            serviceNameTv.setText(qrCodeResponseModel.getServiceName());
        }
    }

    private void submitReportUsData(ReportUsRequestModel request) {
        VolleyTaskListener taskListener = new VolleyTaskListener() {
            @Override
            public void postExecute(String response) {
                reportUsResponseModel = QRCodeResponseModel.create(response);
                if (reportUsResponseModel.isStatus()) {
                    CustomAlert.alertOkWithFinish(context, "Your report has been submitted Successfully.");
                    // finish();
                } else {
                    Toast.makeText(context, reportUsResponseModel.getErrorMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            @Override
            public void onError(VolleyError error) {
                if (error != null) {
                    // String s = new String(error.networkResponse.data);
                    //  Log.d("ERROR MSG", s);
                    if (error instanceof TimeoutError) {
                        CustomAlert.alertWithOk(context, context.getResources().getString(R.string.timeout_issue));
                    } else if (AppUtilityFunction.isServerProblem(error)) {
                        // Toast.makeText(getApplicationContext(),R.string.LOGIN_FAILED,Toast.LENGTH_LONG).show();

                        CustomAlert.alertWithOk(context, context.getResources().getString(R.string.server_issue));
                    } else if (AppUtilityFunction.isNetworkProblem(error)) {
                        CustomAlert.alertWithOk(context, context.getResources().getString(R.string.IO_ERROR));
                    }
                }
            }
        };
        volley = new CustomVolley(taskListener, "Please wait..", AppConstant.SUBMIT_REPORT, request.serialize(), null, null, context);
        volley.execute();
    }

 /*   private void parseQRCode(String code) {
        try {

           // qrcode = Parser.parseWithoutTagValidation(code);
            qrcode.validate();
            JSONArray jarray = new JSONArray(loadJSONFromAsset());
            String currency = "";
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jb = (JSONObject) jarray.get(i);
                if (qrcode.getTransactionCurrencyCode().equalsIgnoreCase(jb.optString("NumericCode"))) {
                    currency = jb.optString("AlphabeticCode");
                }
            }



        }catch(Exception e){
            //CM.ShowDialogNoInternet(activity, activity.getResources().getString(R.string.invalid_qr_code), false);
        }

    }*/


    @Override
    public void onResume() {
        super.onResume();
        CameraPermission(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}
