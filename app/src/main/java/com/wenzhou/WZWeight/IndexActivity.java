package com.wenzhou.WZWeight;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.wenzhou.WZWeight.R;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

public class IndexActivity extends Activity {
	private View search;
	private View card;
	private View car;
	private View tool;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.index);

		search = findViewById(R.id.linear_search);
		card = findViewById(R.id.linear_card);
		car = findViewById(R.id.linear_car);
		tool = findViewById(R.id.linear_tool);

		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(IndexActivity.this, SearchActivity.class));
				IndexActivity.this.finish();
			}
		});

		card.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(IndexActivity.this, CardActivity.class));
				IndexActivity.this.finish();
			}
		});

		car.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(IndexActivity.this, CarActivity.class));
				IndexActivity.this.finish();
			}
		});

		tool.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(IndexActivity.this, ToolActivity.class));
				IndexActivity.this.finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			AlertDialog isExit = new AlertDialog.Builder(this).create();

			isExit.setTitle("ϵͳ��ʾ");

			isExit.setMessage("ȷ��Ҫ�˳���");

			isExit.setButton("ȷ��", listener);
			isExit.setButton2("ȡ��", listener);

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
}
