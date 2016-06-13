package com.wenzhou.WZWeight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

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
			// 创建退出对话框
			AlertDialog isExit = new AlertDialog.Builder(this).create();
			// 设置对话框标题
			isExit.setTitle("系统提示");
			// 设置对话框消息
			isExit.setMessage("确定要退出吗? ");
			// 添加选择按钮并注册监听
			isExit.setButton("确定", listener);
			isExit.setButton2("取消", listener);
			// 显示对话框
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
