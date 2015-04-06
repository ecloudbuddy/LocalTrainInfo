package com.app.ashish.localtraininfo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.app.ashish.localtraininfo.R;
import com.app.ashish.localtraininfo.adapters.TrainScheduleListAdapter;
import com.app.ashish.localtraininfo.bean.WebServiceCallType;
import com.app.ashish.localtraininfo.util.DataShareSingleton;
import com.app.ashish.localtraininfo.util.RailInfoUtil;
import com.app.ashish.localtraininfo.util.WebServiceCall;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by ashis_000 on 4/5/2015.
 */
public class SplashScreenActivity extends ActionBarActivity {
    private DataShareSingleton appData = DataShareSingleton.getInstance();
    private String fromStn = "";
    private String toStn = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        fromStn = getIntent().getExtras().getString("from");
        toStn = getIntent().getExtras().getString("to");

        new WebServiceCall().execute();
    }

    public class WebServiceCall extends AsyncTask<Void, Void, String> {

        private DataShareSingleton commonData = DataShareSingleton.getInstance();
        private ProgressDialog mProgressDialog = null;
        private Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet(DataShareSingleton.getInstance().getUrl());
            String text = null;
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                text = getASCIIContentFromEntity(entity);
            } catch (Exception e) {
                return e.getLocalizedMessage();
            }
            return text;
        }

        @Override
        protected void onPostExecute(String results) {
            if (results != null) {

//                List<String> trainScheduleArray = RailInfoUtil.parseTrainScheduleDetails(results);
//                commonData.setTrainScheduleArray(trainScheduleArray);

                if(commonData.getWebServiceCallType() == WebServiceCallType.ALL_STATION_NAME_CALL) {
                    if (commonData.getAllStnList() == null) {
                        List<String> allStnMap = RailInfoUtil.parseStationDetails(results);
                        commonData.setAllStnList(allStnMap);
                    }
                } else if(commonData.getWebServiceCallType() == WebServiceCallType.TRAIN_SCHEDULE_CAL) {
                    List<String> trainScheduleArray = RailInfoUtil.parseTrainScheduleDetails(results);
                    commonData.setTrainScheduleArray(trainScheduleArray);

                    setContentView(R.layout.train_schedule);
                    EditText fromTxt = (EditText) findViewById(R.id.fromStn);
                    fromTxt.setText(fromStn);
                    fromTxt.setEnabled(false);
                    EditText toTxt = (EditText) findViewById(R.id.toStn);
                    toTxt.setText(toStn);
                    toTxt.setEnabled(false);
                    Button btn = (Button) findViewById(R.id.search);
                    btn.setText("Back");
                    ((TextView)findViewById(R.id.scheduleHeader)).setVisibility(View.VISIBLE);
                    ((ListView) findViewById(R.id.stnListView)).setVisibility(View.GONE);

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

                    ListView scheduleView = (ListView)findViewById(R.id.scheduleView);
                    TrainScheduleListAdapter trainScheduleAdapter = new TrainScheduleListAdapter(getApplicationContext(), commonData.getTrainScheduleArray());
                    scheduleView.setAdapter(trainScheduleAdapter);
                }

                // close this activity
//                finish();
                //DataShareSingleton.getInstance().getEditText().setText(results);
            }
        }

        protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
            InputStream in = entity.getContent();

            StringBuffer out = new StringBuffer();
            int n = 1;
            while (n>0) {
                byte[] b = new byte[4096];
                n =  in.read(b);
                if (n>0) out.append(new String(b, 0, n));
            }
            return out.toString();
        }
    }

    public void liveStatusHandler(View v) {
        //get the row the clicked button is in
        LinearLayout vwParentRow = (LinearLayout)v.getParent();

        TextView scheduleDtls = (TextView)vwParentRow.getChildAt(0);
        Button liveStatusBtn = (Button)vwParentRow.getChildAt(1);
        String scheduleStr = scheduleDtls.getText().toString();
        String trainNo = "0";
        if(scheduleStr != null && scheduleStr.split(" ").length > 0) {
            trainNo = scheduleStr.split(" ")[0].trim();
        }
        liveStatusBtn.setText(trainNo);
    }
}
