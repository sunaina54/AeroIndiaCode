package com.aero.view.activity;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aero.R;
import com.aero.Utility.AppUtility;
import com.aero.custom.utility.AppConstant;
import com.aero.custom.utility.AppUtilityFunction;
import com.aero.db.CommonDatabaseAero;
import com.aero.db.DatabaseOpration;
import com.aero.pojos.generic.ServiceModel;
import com.aero.pojos.response.AnnouncementModel;
import com.aero.pojos.response.EventModel;
import com.aero.pojos.response.NoticeBoardItemModel;
import com.aero.pojos.response.NoticeBoardResponseModel;
import com.aero.pojos.response.UpcomingEventsResponse;
import com.android.volley.VolleyError;
import com.customComponent.CustomAlert;
import com.customComponent.Networking.CustomVolleyGet;
import com.customComponent.Networking.VolleyTaskListener;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    RelativeLayout backLayout;
    private TextView headerTV;
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<NoticeBoardItemModel> data = new ArrayList<>();
    private ArrayList<NoticeBoardItemModel> noticeBoardItemModels = new ArrayList<>();

    private AnnouncementAdapter adapter;
    private NoticeBoardResponseModel noticeBoardResponseModel;
    private SwipeRefreshLayout eventRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);
        context = this;
        getSupportActionBar().hide();
        backLayout = (RelativeLayout) findViewById(R.id.backLayout);
        headerTV = (TextView) findViewById(R.id.headerTV);
        headerTV.setText(getResources().getString(R.string.announcement));
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        data = new ArrayList<NoticeBoardItemModel>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AnnouncementAdapter(context, data);
        recyclerView.setAdapter(adapter);

        eventRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.eventRefreshLayout);
        eventRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (AppUtilityFunction.isNetworkAvailable(context)) {
/*
                    data= DatabaseOpration.getEventData(context);
                    if(data.size()>0)
                    {

                        adapter = new EventsAdapter(context, data,latitude,longitude);
                        recyclerView.setAdapter(adapter);
                    }*/
                    getNoticeBoardResult();
                } else {
                    Toast.makeText(context, "Please connect internet to refresh data", Toast.LENGTH_LONG).show();
                }
                eventRefreshLayout.setRefreshing(false);
            }
        });


        if (AppUtilityFunction.isNetworkAvailable(context)) {

            noticeBoardItemModels = DatabaseOpration.getNoticeBoardData(context);
            if (noticeBoardItemModels.size() > 0) {
                for(int i=0;i<noticeBoardItemModels.size();i++) {
                    data.add(new NoticeBoardItemModel(noticeBoardItemModels.get(i).getId(),
                            noticeBoardItemModels.get(i).getTitle(), noticeBoardItemModels.get(i).getLatitude(),
                            noticeBoardItemModels.get(i).getLongitude()
                            , noticeBoardItemModels.get(i).getVenue(), noticeBoardItemModels.get(i).getDateTime()));
                }
                adapter.notifyDataSetChanged();
            }
            getNoticeBoardResult();
        } else {
            noticeBoardItemModels = DatabaseOpration.getNoticeBoardData(context);
            if (noticeBoardItemModels.size() > 0) {
                for(int i=0;i<noticeBoardItemModels.size();i++) {
                    data.add(new NoticeBoardItemModel(noticeBoardItemModels.get(i).getId(),
                            noticeBoardItemModels.get(i).getTitle(), noticeBoardItemModels.get(i).getLatitude(),
                            noticeBoardItemModels.get(i).getLongitude()
                            , noticeBoardItemModels.get(i).getVenue(), noticeBoardItemModels.get(i).getDateTime()));
                }
                adapter.notifyDataSetChanged();
            }
        }

    }


    private void getNoticeBoardResult() {
        // showHideProgressDialog(true);
        VolleyTaskListener taskListener = new VolleyTaskListener() {
            @Override
            public void postExecute(String response) {
                noticeBoardResponseModel = NoticeBoardResponseModel.create(response);
                Log.d("NOTICEBOARD resp:", noticeBoardResponseModel.serialize());
                if (noticeBoardResponseModel != null) {


                    if (noticeBoardResponseModel.isStatus()) {
                        if (noticeBoardResponseModel.getNoticeBoardList() != null) {
                            if (noticeBoardResponseModel.getNoticeBoardList().size() > 0) {
                                data.clear();
                                for (int i = 0; i < noticeBoardResponseModel.getNoticeBoardList().size(); i++) {
                                    data.add(new NoticeBoardItemModel(noticeBoardResponseModel.getNoticeBoardList().get(i).getId(),
                                            noticeBoardResponseModel.getNoticeBoardList().get(i).getTitle(), noticeBoardResponseModel.getNoticeBoardList().get(i).getLatitude(),noticeBoardResponseModel.getNoticeBoardList().get(i).getLongitude()
                                    ,noticeBoardResponseModel.getNoticeBoardList().get(i).getVenue(),noticeBoardResponseModel.getNoticeBoardList().get(i).getDateTime()));

                                    NoticeBoardItemModel noticeBoardItemModel = CommonDatabaseAero.updateNoticeBoardList(context, noticeBoardResponseModel.getNoticeBoardList().get(i));
                                    if (noticeBoardItemModel == null) {
                                        CommonDatabaseAero.saveNoticeboardData(context, noticeBoardResponseModel.getNoticeBoardList().get(i));
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            } else {
                                CustomAlert.alertOkWithFinish(context, "There is no announcement available for now");

                            }
                        } else {
                            CustomAlert.alertWithOk(context, "There is no announcement available for now");

                        }
                    }
                    //  showHideProgressDialog(false);

                }
            }


            @Override
            public void onError(VolleyError error) {

                //  showHideProgressDialog(false);
                if (error.getMessage() != null) {
                    if (error.getMessage().contains("java.net.UnknownHostException")) {
                        CustomAlert.alertWithOk(context, getResources().getString(R.string.internet_connection_message));

                    } else {
                        CustomAlert.alertWithOk(context, getResources().getString(R.string.server_issue));

                    }
                } else {
                    CustomAlert.alertWithOk(context, getResources().getString(R.string.server_issue));

                }


            }
        };
        CustomVolleyGet volley = new CustomVolleyGet(taskListener, null, AppConstant.GET_NOTICEBOARD, context);
        volley.execute();
    }


    public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.MyViewHolder> {

        private ArrayList<NoticeBoardItemModel> dataSet;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView titleTv, dateTv, venuTv;

            public MyViewHolder(View itemView) {
                super(itemView);
                this.titleTv = (TextView) itemView.findViewById(R.id.titleTv);
                this.dateTv = (TextView) itemView.findViewById(R.id.dateTv);
                this.venuTv = (TextView) itemView.findViewById(R.id.venuTv);

            }


        }

        public void addAll(List<NoticeBoardItemModel> list) {

            dataSet.addAll(list);
            notifyDataSetChanged();
        }

        public AnnouncementAdapter(Context context, ArrayList<NoticeBoardItemModel> data) {

            this.dataSet = data;
            this.context = context;

        }

        @Override
        public AnnouncementAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.announcementitem_layout, parent, false);

            //view.setOnClickListener(MainActivity.myOnClickListener);

            AnnouncementAdapter.MyViewHolder myViewHolder = new AnnouncementAdapter.MyViewHolder(view);
            return myViewHolder;
        }


        @Override
        public void onBindViewHolder(final AnnouncementAdapter.MyViewHolder holder, final int listPosition) {


            final NoticeBoardItemModel serviceModel = dataSet.get(listPosition);
            holder.titleTv.setText(serviceModel.getTitle());
            if (serviceModel.getDateTime() != 0) {
                String date = AppUtility.getDate(serviceModel.getDateTime());
                holder.dateTv.setText(date);
            }

            holder.venuTv.setText(serviceModel.getVenue());
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        public void clearDataSource() {

            dataSet.clear();
            notifyDataSetChanged();
        }

    }

}
