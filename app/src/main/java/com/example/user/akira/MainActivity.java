package com.example.user.akira;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.String;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.OkHttpClient;



public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Switch swh;
    public int sign;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        swh = (Switch) findViewById(R.id.switch1);

        swh.setOnCheckedChangeListener(SHListener);

    }

    @Override
    protected void onResume(){
        super.onResume();
        GET_Waterlevel();
    }


    public void GET_Waterlevel() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run(){
              OkHttpClient client = new OkHttpClient();
              Request request = new Request.Builder()
                      .url("https://api.mediatek.com/mcs/v2/devices/Dsaqiizj/datachannels/1/datapoints")
                      .header("deviceKey", "I7kHh3w0vcS8n3qM")
                      .build();
              try {
                  final Response response = client.newCall(request).execute();
                  final String getStr = response.body().string();
                  String[] Str1 = getStr.split(":|\\}");

                  int index = Arrays.asList(Str1).indexOf("{\"value\"");
                  final String retStr = Str1[index + 1];
                  System.out.println(Str1[index + 1]);

                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          textView.setText(retStr);
                      }
                  });
              } catch (IOException e) {
                  e.printStackTrace();
              }
            }
         },1,5,TimeUnit.SECONDS);
    }


    Switch.OnCheckedChangeListener SHListener = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                sign = 1;
            } else {
                sign = 0;
            }
            POST_OKHTTP();
        }
    };


    //{datapoints:[{dataChnId: "2", values: {value: 1}}]}
    public class val{
        int value;
    }
    public class datapoint{
        String dataChnId;
        val values = new val();
        datapoint(String id, int value){
            this.dataChnId = id;
            this.values.value = value;
        }
    }
    public class Data{
        List<datapoint> datapoints;
        Data(List<datapoint> dp){
            this.datapoints = dp;
        }
    }
    public void POST_OKHTTP() {
        final String url = "https://api.mediatek.com/mcs/v2/devices/Dsaqiizj/datapoints";
        Gson gson = new Gson();
        datapoint dp = new datapoint("2",sign);
        List<datapoint> ary_dp = Arrays.asList(dp);
        Data post_data = new Data(ary_dp);
        final String post_str = gson.toJson(post_data);

        AsyncTask postTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, post_str);

                Request request = new Request.Builder()
                        .url(url)
                        .header("deviceKey", "I7kHh3w0vcS8n3qM")
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    System.out.println("狀態>>>" + response.code());
                    System.out.println("資料>>>" + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        postTask.execute();

    }


}





