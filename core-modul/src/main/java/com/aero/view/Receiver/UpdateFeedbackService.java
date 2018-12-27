package com.aero.view.Receiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.aero.R;
import com.aero.custom.utility.AppConstant;
import com.aero.custom.utility.AppUtilityFunction;
import com.aero.db.DatabaseHelpers;
import com.aero.db.DatabaseOpration;
import com.aero.pojos.request.ContactEmailRequestModel;
import com.aero.pojos.request.ReportUsRequestModel;
import com.aero.pojos.request.SubmitFeedbackRequest;
import com.aero.pojos.response.ContactEmailResponseModel;
import com.aero.pojos.response.GenericResponse;
import com.aero.pojos.response.LoginResponse;
import com.aero.pojos.response.QRCodeResponseModel;
import com.android.volley.NetworkResponse;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.customComponent.CustomAlert;
import com.customComponent.CustomAsyncTask;
import com.customComponent.Networking.CustomVolley;
import com.customComponent.Networking.CustomVolleyGet;
import com.customComponent.Networking.VolleyTaskListener;
import com.customComponent.utility.ProjectPrefrence;


import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by psqit on 12/29/2016.
 */
public class UpdateFeedbackService extends Service {
    private double latt, longg;
    private String loginData;
    private DatabaseHelpers dbHelper;
    private LoginResponse loginResponse;
    private String userId, projectNm;
    CustomAsyncTask
            asyncTask;
    File file2;
    private int status;
    private GenericResponse genericResponse;
    private ContactEmailResponseModel contactEmailResponseModel;
    private Context context;
    private QRCodeResponseModel reportUsResponseModel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbHelper = DatabaseHelpers.getInstance(this);
        context = this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        //  loginData = ProjectPrefrence.getSharedPrefrenceData(AppConstant.PROJECT_PREF_DOMAIN, AppConstants.LOGIN_PREF_CONTENT, this);
       /* if(loginData!=null)
        {
            loginResponse= LoginResponse.create(loginData);
            userId=loginResponse.getcAccCode();
            projectNm=loginResponse.getProjectName();
            start(intent);
        }
        else
        {

        }*/
        start(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void start(Intent intent) {
        ArrayList<SubmitFeedbackRequest> requestArrayList = new ArrayList<>();
        requestArrayList = DatabaseOpration.getFeedbackData(context);
        if (requestArrayList != null) {
            if (requestArrayList.size() > 0) {
                for (int i = 0; i < requestArrayList.size(); i++) {
                    SubmitFeedbackRequest submitFeedbackRequest = requestArrayList.get(i);
                    submitFeedback(submitFeedbackRequest);

                }
            }
        }

        ArrayList<ContactEmailRequestModel> contactEmailRequestModel=new ArrayList<>();
        contactEmailRequestModel=DatabaseOpration.getContactData(context);
        if(contactEmailRequestModel!=null){
            if(contactEmailRequestModel.size()>0){
                for (int i = 0; i < contactEmailRequestModel.size(); i++) {
                    ContactEmailRequestModel contactEmailRequestModel1 = contactEmailRequestModel.get(i);
                    sendContactRequest(contactEmailRequestModel1);

                }
            }
        }

        ArrayList<ReportUsRequestModel> reportUsRequestModels=new ArrayList<>();
        reportUsRequestModels=DatabaseOpration.getServiceComplaint(context);
        if(reportUsRequestModels!=null){
            if(reportUsRequestModels.size()>0){
                for (int i = 0; i < reportUsRequestModels.size(); i++) {
                    ReportUsRequestModel requestModel = reportUsRequestModels.get(i);
                    submitReportUsData(requestModel);

                }
            }
        }
    }


    private void submitFeedback(final SubmitFeedbackRequest request) {

        VolleyTaskListener taskListener = new VolleyTaskListener() {
            @Override
            public void postExecute(String response) {
                genericResponse = GenericResponse.create(response);

                if (genericResponse.isStatus()) {
                    Toast.makeText(context, "Your feedback submitted successfully.", Toast.LENGTH_LONG).show();
                    if (request.getFeedbackId() != 0) {
                        DatabaseOpration.deleteFeedbackData(context, request.getFeedbackId());

                    }

                } else {
                    if (genericResponse.getErrorMessage() != null) {
                        Toast.makeText(context, genericResponse.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }

            @Override
            public void onError(VolleyError error) {


                if (error != null) {
                    // String s = new String(error.networkResponse.data);
                    //  Log.d("ERROR MSG", s);
                    if (error instanceof TimeoutError) {
                        CustomAlert.alertWithOk(context, getResources().getString(R.string.timeout_issue));
                    } else if (AppUtilityFunction.isServerProblem(error)) {
                        // Toast.makeText(getApplicationContext(),R.string.LOGIN_FAILED,Toast.LENGTH_LONG).show();

                        CustomAlert.alertWithOk(context, getResources().getString(R.string.server_issue));
                    } else if (AppUtilityFunction.isNetworkProblem(error)) {
                        CustomAlert.alertWithOk(context, getResources().getString(R.string.IO_ERROR));
                    }
                }
            }
        };

        Log.d("Feedback Request:", request.serialize());
        CustomVolley volley = new CustomVolley(taskListener, null, AppConstant.SUBMITFEEDBACK_API, request.serialize(), null, null, context);
        volley.execute();
    }


    private void sendContactRequest(final ContactEmailRequestModel request) {
        VolleyTaskListener taskListener = new VolleyTaskListener() {
            @Override
            public void postExecute(String response) {
                contactEmailResponseModel = ContactEmailResponseModel.create(response);
                if (contactEmailResponseModel.isStatus()) {

                    if (request.getContactExhibitorId() != 0) {
                        DatabaseOpration.deleteContactData(context, request.getContactExhibitorId());

                    }
                    Toast.makeText(context, "Contact request send successfully.", Toast.LENGTH_LONG).show();
                    //return;
                } else {

                    Toast.makeText(context, contactEmailResponseModel.getErrorMessage(), Toast.LENGTH_LONG).show();
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

        Log.d("Contact Request:",request.serialize());
        CustomVolley  volley = new CustomVolley(taskListener, null, AppConstant.CONTACT_EMAIL, request.serialize(), null, null, context);
        volley.execute();
    }

    private void submitReportUsData(final ReportUsRequestModel request) {
        VolleyTaskListener taskListener = new VolleyTaskListener() {
            @Override
            public void postExecute(String response) {
                reportUsResponseModel = QRCodeResponseModel.create(response);
                if (reportUsResponseModel.isStatus()) {
                    if (request.getComplaintId() != 0) {
                        DatabaseOpration.deleteServiceComplaintData(context, request.getComplaintId());

                    }
                    Toast.makeText(context, "Your report has been submitted Successfully.", Toast.LENGTH_LONG).show();
                    // finish();
                } else {
                    Toast.makeText(context, reportUsResponseModel.getErrorMessage(), Toast.LENGTH_LONG).show();

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
        CustomVolley volley = new CustomVolley(taskListener, null, AppConstant.SUBMIT_REPORT, request.serialize(), null, null, context);
        volley.execute();
    }

}
