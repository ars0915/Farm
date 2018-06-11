package com.example.user.akira;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
    static String retStr = new String();
    private ImageView mwater;
    private ImageView mplant;
    static float ydpi;
    static float plant_height = 8;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        swh = (Switch) findViewById(R.id.switch1);
        mwater = (ImageView) findViewById(R.id.imageView);
        mplant = (ImageView) findViewById(R.id.imageView2);

        swh.setOnCheckedChangeListener(SHListener);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ydpi = dm.ydpi;
    }


    @Override
    protected void onResume(){
        super.onResume();
        Draw_plant();
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
                  Response response = client.newCall(request).execute();
                  String getStr = response.body().string();
                  String[] Str1 = getStr.split(":|\\}");

                  int index = Arrays.asList(Str1).indexOf("{\"value\"");
                  retStr = Str1[index + 1];
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          Draw_waterlevel(retStr);
                          textView.setText(retStr);
                      }
                  });
              } catch (IOException e) {
                  e.printStackTrace();
              }
            }
         },1,5,TimeUnit.SECONDS);
    }

    public void Draw_plant(){
        int height = cm2px(plant_height);
        android.view.ViewGroup.LayoutParams playoutParams = mplant.getLayoutParams();
        playoutParams.height = height;
        mplant.setLayoutParams(playoutParams);
        System.out.println("plantpx = " + height);
    }

    public void Draw_waterlevel(String Str){
        float waterlevel_cm = Float.parseFloat(Str);
        int height = cm2px(waterlevel_cm);
        android.view.ViewGroup.LayoutParams layoutParams = mwater.getLayoutParams();
        layoutParams.height = height;
        mwater.setLayoutParams(layoutParams);
        //System.out.printf("Str = %s, ydpi = %f, dp = %f, height = %d%n",Str,ydpi,dp,height);
    }


    public int cm2px(float cm){
        // 150dp = 8cm
        float dp = cm * 18.75f;
        int px_o = Math.round(dp * (ydpi / 160));
        return px_o;
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





