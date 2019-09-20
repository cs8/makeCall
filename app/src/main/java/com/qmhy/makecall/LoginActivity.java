package com.qmhy.makecall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;


import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    private String token ="";

    AsyncHttpClient client = new AsyncHttpClient();
    EditText editTextUsrName;
    EditText editTextPassWord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        editTextUsrName=(EditText)findViewById(R.id.ed_1) ;
        editTextPassWord=(EditText)findViewById(R.id.ed_password) ;

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextUsrName.getText().length()==0||editTextPassWord.getText().length()==0){
                    Toast.makeText(LoginActivity.this,"请填写登录信息",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    Toast.makeText(LoginActivity.this,"请打开权限",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Settings.ACTION_SETTINGS);
                    LoginActivity.this.startActivity(i);
                    return;
                }


                login();




            }
        });

    }

    private void  login(){
        RequestParams params=new RequestParams();
        params.put("phone",editTextUsrName.getText().toString());
        params.put("password",Md5Util.md5(editTextPassWord.getText().toString()).toLowerCase());

        client.get("http://psys.51qmhy.com/index.php/api/user/login/",params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //用Toast显示是否请求成功
                Toast.makeText(LoginActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                      if("1".equals(jsonObject.get("code").toString())){
                          token=jsonObject.get("token").toString();
                          startActivity(new Intent(LoginActivity.this,MainActivity.class)
                                  .putExtra("token",token)
                                  .putExtra("login_phone",editTextUsrName.getText().toString()));
                      }else{
                          Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                      }



//                    Toast.makeText(LoginActivity.this,jsonObject.get("token").toString(),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                //打印获取到的信息
                Log.e( "onSuccess: ", "-->"+responseString);

            }
        });
    }

}
