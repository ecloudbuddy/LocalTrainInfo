package com.app.ashish.localtraininfo.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.EditText;

import com.app.ashish.localtraininfo.activity.MainActivity;
import com.app.ashish.localtraininfo.bean.WebServiceCallType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ashis_000 on 4/4/2015.
 */
public class WebServiceCall extends AsyncTask<Void, Void, String> {

    private DataShareSingleton commonData = DataShareSingleton.getInstance();
    private ProgressDialog mProgressDialog = null;
    private Context context;

    public WebServiceCall(Context context) {
        this.context = context;
    }

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

            if(commonData.getWebServiceCallType() == WebServiceCallType.ALL_STATION_NAME_CALL) {
                if (commonData.getAllStnList() == null) {
                    List<String> allStnMap = RailInfoUtil.parseStationDetails(results);
                    commonData.setAllStnList(allStnMap);
                }
            } else if(commonData.getWebServiceCallType() == WebServiceCallType.TRAIN_SCHEDULE_CAL) {
                List<String> trainScheduleArray = RailInfoUtil.parseTrainScheduleDetails(results);
                commonData.setTrainScheduleArray(trainScheduleArray);
            }
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
