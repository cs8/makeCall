package com.qmhy.makecall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    AsyncHttpClient client = new AsyncHttpClient();
    Timer timer ;
    public String token;
    public String login_phone;

    public String lastCallTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        token = getIntent().getStringExtra("token");
        login_phone= getIntent().getStringExtra("login_phone");
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出
                finish();
            }
        });


    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                getData();
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // (1) 使用handler发送消息
                Message message = new Message();
                message.what = 0;
                mHandler.sendMessage(message);
            }
        }, 0, 800);//


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(timer!=null)
        timer.cancel();
        timer=null;
    }

    void getData() {
        RequestParams params = new RequestParams();
        params.put("token", token);
        params.put("login_phone", login_phone);

        client.get("http://psys.51qmhy.com/index.php/api/user/getBhPhone/", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //用Toast显示是否请求成功
                Toast.makeText(MainActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_SHORT).show();
                //打印获取到的信息

                Log.e("onSuccess: ", responseString + "++++++++++"+(System.currentTimeMillis()/1000));

                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    Long time = Long.parseLong(jsonData.get("on_time").toString());
                    //筛选重复的
                    if(lastCallTime.equals(jsonData.get("on_time").toString())){
                        return;
                    }

//                Log.e("onSuccess: ", time + "++++++++++"+(System.currentTimeMillis()/1000));
                   //4秒内有效
                    if (time+10>(System.currentTimeMillis()/1000)) {
//                     Log.e(": ", "++++++进入");
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this,"请打开权限",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(Settings.ACTION_SETTINGS);
                            MainActivity.this.startActivity(i);
                            return;
                        }
                        //标记请求过
                        makephoneCall(jsonData.get("phone").toString(),jsonData.get("on_time").toString());
                        Intent intentPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + jsonData.get("phone").toString()));
                        intentPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intentPhone);
                        lastCallTime=jsonData.get("on_time").toString();
                       }

                     } catch (JSONException e) {
                         e.printStackTrace();
                     }

                 }

         });

     }

   private void makephoneCall(String bh_phone,String on_time) {
       RequestParams params=new RequestParams();
       params.put("login_phone",login_phone);
       params.put("bh_phone",bh_phone);
       params.put("on_time",on_time);


       client.get("http://psys.51qmhy.com/index.php/api/user/updateBhPhoneState/",params, new TextHttpResponseHandler() {
           @Override
           public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
           }

           @Override
           public void onSuccess(int statusCode, Header[] headers, String responseString) {
               //打印获取到的信息

           }
       });
   }




}
