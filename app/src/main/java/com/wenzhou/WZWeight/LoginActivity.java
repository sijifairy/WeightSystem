package com.wenzhou.WZWeight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    private static final String TAG = "Activity_load";
    ProgressDialog dialogMine;
    EditText editText_user;
    EditText editText_word;
    Button button_enter;
    CheckBox mCheckRemember;
    JSONObject obj = null;
    String session;
    String userName;
    String passWord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loadnew);

        editText_user = (EditText) findViewById(R.id.editText_load1);
        editText_word = (EditText) findViewById(R.id.editText_load2);
        button_enter = (Button) findViewById(R.id.button1);
        mCheckRemember = (CheckBox) findViewById(R.id.check_login_rememberpwd);
        Log.i(TAG, "test 0");
        editText_word.setTransformationMethod(PasswordTransformationMethod.getInstance());


        editText_word.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    doGetInterface();
                    return true;
                }
                return false;
            }
        });

        userName = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("userName", "");
        passWord = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("passWord", "");
        if (!userName.equals("")) {
            editText_user.setText(userName);
        }
        if (!passWord.equals("")) {
            editText_word.setText(passWord);
        }


        button_enter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetInterface();
            }
        });
    }

    private void doGetInterface() {
        obj = new JSONObject();
        try {
            obj.put("AccountName", editText_user.getText().toString());
        } catch (JSONException e) {
            MyLog.e(TAG, e.getMessage());
        }
        if (checkNetWorkStatus()) {
            new GetInterfaceTask().execute(Constant.getInterfaceUrl);
        } else {
            showNetworkDialog();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setTitle("系统提示");
            isExit.setMessage("确定要退出吗");
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);

            isExit.show();
        }
        return false;

    }


    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };

    class GetInterfaceTask extends AsyncTask<String, Intent, Boolean> {
        private String errorInfo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogMine = new ProgressDialog(LoginActivity.this);
            dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialogMine.setTitle("请等待...");
            dialogMine.setMessage("正在登录，请稍后...");
            dialogMine.setIcon(R.drawable.ic);
            dialogMine.setCancelable(true);
            dialogMine.setIndeterminate(false);
            dialogMine.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", obj.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    Constant.serverUrl = item.getString("InterfaceAddress");
                    return true;
                } else {
                    errorInfo = item.getString("ErrorInfo");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                MyLog.d(TAG, e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean == false) {
                dialogMine.dismiss();
                if (errorInfo == null) {
                    errorInfo = "登录失败，请联系管理员！";
                }
                Toast.makeText(LoginActivity.this, errorInfo, Toast.LENGTH_LONG).show();
                button_enter.setClickable(true);
            } else {
                obj = new JSONObject();
                try {
                    obj.put("AccountName", editText_user.getText().toString());
                    obj.put("Password", editText_word.getText().toString());
                } catch (JSONException e) {

                    MyLog.e(TAG, e.getMessage());
                }
                if (checkNetWorkStatus()) {
                    new ConnectTask().execute(Constant.serverUrl + Constant.login);
                } else {
                    showNetworkDialog();
                }
            }
        }
    }

    class ConnectTask extends AsyncTask<String, Integer, Boolean> {
        String name;
        String detail;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", obj.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    String UserName = item.getString("UserName");
                    name = UserName;
                    String sessionId = item.getString("SessionID");
                    String AccountName = item.getString("AccountName");
                    String UserClass = item.getString("UserClass");
                    Constant.setAccountName(AccountName);
                    Constant.setSessionID(sessionId);
                    Constant.setUserName(UserName);
                    getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("userClass", UserClass).commit();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

                e.printStackTrace();
                MyLog.d(TAG, e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result == true) {
                dialogMine.dismiss();

                getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("userName", editText_user.getText().toString()).commit();
                if (mCheckRemember.isChecked()) {
                    getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("passWord", editText_word.getText().toString()).commit();
                } else {
                    getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("passWord", "").commit();
                }
                getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("session", session).commit();

                Toast.makeText(LoginActivity.this, "欢迎您，" + name, Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, IndexActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                LoginActivity.this.finish();
            } else {
                if (detail == null) {
                    detail = "登录失败，请联系管理员！";
                }
                Toast.makeText(LoginActivity.this, detail, Toast.LENGTH_LONG).show();
                button_enter.setClickable(true);
            }
        }

    }


    private boolean checkNetWorkStatus() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobileState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifiState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (mobileState == State.CONNECTED || mobileState == State.CONNECTING)
            return true;
        if (wifiState == State.CONNECTED || wifiState == State.CONNECTING)
            return true;
        return false;
    }

    private void showNetworkDialog() {
        new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string.netDialog).setMessage(R.string.netMessage)
                .setIcon(android.R.drawable.ic_dialog_info).setPositiveButton(R.string.netSetting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        }).setNegativeButton(R.string.netExit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startActivity(startMain);
                LoginActivity.this.finish();
            }
        }).show();
    }
}
