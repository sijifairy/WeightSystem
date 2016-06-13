package com.wenzhou.WZWeight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolActivity extends Activity {
    private static final String TAG = "Activity_Tool";

    private static List<Map<String, Object>> yesterdayData = null;
    private TextView back;
    private Button logout;
    private View contact;
    private View update;
    private View repair;
    String str_address;
    String str_phone;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tool);

        back = (TextView) findViewById(R.id.tool_textview_top);
        logout = (Button) findViewById(R.id.button_logout);
        contact = findViewById(R.id.linear_contact);
        update = findViewById(R.id.linear_update);
        repair = findViewById(R.id.linear_repair);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToolActivity.this, IndexActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ToolActivity.this.finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToolActivity.this, LoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                ToolActivity.this.finish();
            }
        });

        repair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "18618488891"));

                ToolActivity.this.startActivity(intent);
            }
        });


        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDialogUI(ToolActivity.this);
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ToolActivity.this, "当前已是最新版本.", Toast.LENGTH_SHORT).show();
            }
        });

        new YesterdayWeightTask().execute(Constant.serverUrl + Constant.aboutus);
    }


    class YesterdayWeightTask extends AsyncTask<String, Integer, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Boolean doInBackground(String... params) {

            try {
                MyLog.d(TAG, "params[0] is :" + params[0]);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("SessionID", Constant.getSessionID());
                    MyLog.i(TAG, "obj" + obj.toString());
                } catch (JSONException e) {
                    MyLog.e(TAG, e.getMessage());
                }
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", obj.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    yesterdayData = getYersterdayData(item.getString("TableJson"));
                    str_address = yesterdayData.get(0).get("Address").toString();
                    str_phone = yesterdayData.get(0).get("Phone").toString();
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

        }
    }


    private List<Map<String, Object>> getYersterdayData(String tablejson) {
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        try {
            tablejson = tablejson.replaceAll("\\\"", "\"");
            JSONArray item = new JSONArray(tablejson);

            for (int i = 0; i < item.length(); i++) {
                JSONObject obj = item.getJSONObject(i);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("Address", obj.getString("Address"));
                map.put("Phone", obj.getString("Phone"));
                dataList.add(map);
            }
            return dataList;
        } catch (Exception e) {

            e.printStackTrace();
            MyLog.d(TAG, e.toString());
            return new ArrayList<Map<String, Object>>();
        }
    }

    private void showContactDialogUI(Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View contactView = inflater.inflate(R.layout.tool_contact, null);
        final TextView address = (TextView) contactView.findViewById(R.id.linear_item_Address);
        final TextView phone = (TextView) contactView.findViewById(R.id.linear_item_Phone);
        address.setText(str_address);
        phone.setText(str_phone);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("关于我们");
        builder.setView(contactView);
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(ToolActivity.this, IndexActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ToolActivity.this.finish();
        }
        return false;
    }
}
