package com.wenzhou.WZWeight;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.wenzhou.WZWeight.adapter.ExpandableAdapter;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;
import com.wenzhou.WZWeight.sqlite.DataBaseAdapter;
import com.wenzhou.WZWeight.sqlite.InfoColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandListDemo extends Activity {

	// private ArrayList<String> groupArray;
	// private ArrayList<List<String>> childArray;

	private List<Map<String, Object>> groupArray = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> tempArray = new ArrayList<Map<String, Object>>();
	private List<List<Map<String, Object>>> childArray = new ArrayList<List<Map<String, Object>>>();
	private String[] isParent;
	DataBaseAdapter mDataBaseAdapter;
	Cursor mCursorGroup;
	Cursor mCursorChild;

	private ExpandableListView expandableListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_expandlist_demo);

		expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

	}

	@Override
	protected void onResume() {

		super.onResume();

		if (mCursorGroup != null) {
			mCursorGroup.close();
			mCursorGroup = null;
		}

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;

		}

		mDataBaseAdapter = new DataBaseAdapter(ExpandListDemo.this);
		mDataBaseAdapter.open();


		mCursorGroup = mDataBaseAdapter.fetchAllData(Constant.index_table2,
				InfoColumn.PROJECTION, "user_name=?" + "AND state=?"
						+ " AND accept_time <> ?" + " AND ParentID = ?",
				new String[] { "������", "0", "null", "null" }, InfoColumn.DAYS
						+ " ASC");

		MyLog.d("ExpandListDemo", "" + mCursorGroup.getCount());

		setDataForGroup();
		setDataForChild();

		ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
		expandableListView.setAdapter(new ExpandableAdapter(
				ExpandListDemo.this, groupArray, childArray));

		expandableListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {





				return false;
			}
		});

		expandableListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				Toast.makeText(ExpandListDemo.this,
						"expandableListView " + childPosition,
						Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		expandableListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {

						Toast.makeText(ExpandListDemo.this,
								"expandableListView long " + arg2,
								Toast.LENGTH_SHORT).show();
						return true;
					}
				});

	}

	@Override
	protected void onPause() {

		super.onPause();
		if (mCursorGroup != null) {
			mCursorGroup.close();
			mCursorGroup = null;
		}

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;

		}

	}

	private void setDataForGroup() {

		isParent = new String[mCursorGroup.getCount()];

		Map<String, Object> map;
		if (!groupArray.isEmpty()) {
			groupArray.clear();
		}

		if (mCursorGroup.moveToFirst()) {

			if (mCursorGroup.getCount() > 0) {
				int i = 0;

				do {
					map = new HashMap<String, Object>();
					map.put("accept", mCursorGroup
							.getString(InfoColumn.ACCEPT_TIME_COLUMN));
					String id = mCursorGroup
							.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
					map.put("id", id);
					map.put("date",
							mCursorGroup.getString(InfoColumn.OVER_TIME_COLUMN));
					map.put("type",
							mCursorGroup.getString(InfoColumn.TASK_TYPE_COLUMN));
					map.put("name",
							mCursorGroup.getString(InfoColumn.USER_NAME_COLUMN));
					map.put("day",
							mCursorGroup.getString(InfoColumn.DAYS_COLUMN));
					map.put("state",
							mCursorGroup.getString(InfoColumn.STATE_COLUMN));

					if (mCursorGroup.getString(InfoColumn.IS_SPLIT_COLUMN)
							.equals("true")) {
						isParent[i] = id;
						map.put("title",
								mCursorGroup.getString(InfoColumn.TITLE_COLUMN)
										+ "(���Ҫ��)");
						i++;
					} else {
						isParent[i] = "null";
						map.put("title",
								mCursorGroup.getString(InfoColumn.TITLE_COLUMN));
						i++;
					}
					groupArray.add(map);
				} while (mCursorGroup.moveToNext());
			}

		}

	}

	private void setDataForChild() {
		Map<String, Object> map;

		if (!tempArray.isEmpty()) {
			tempArray.clear();
		}
		if (!childArray.isEmpty()) {
			childArray.clear();
		}

		for (int i = 0; i < mCursorGroup.getCount(); i++) {
			MyLog.d("isParent", "isParent is " + isParent[i]);
		}

		for (int i = 0; i < mCursorGroup.getCount(); i++) {
			if (!tempArray.isEmpty()) {
				tempArray.clear();
			}

			if (isParent[i].equals("null")) {
				MyLog.d("null", "" + i);
				tempArray = new ArrayList<Map<String, Object>>();

			} else {
				tempArray = new ArrayList<Map<String, Object>>();

				String parentId = isParent[i];

				mCursorChild = mDataBaseAdapter.fetchAllData(
						Constant.index_table2, InfoColumn.PROJECTION,
						"user_name=?" + "AND state=?" + " AND accept_time <> ?"
								+ " AND ParentID = ?", new String[] { "������",
								"0", "null", parentId }, InfoColumn.DAYS
								+ " ASC");

				for (int j = 0; j < mCursorChild.getCount(); j++) {

					map = new HashMap<String, Object>();
					map.put("title",
							mCursorChild.getString(InfoColumn.TITLE_COLUMN));
					map.put("accept", mCursorChild
							.getString(InfoColumn.ACCEPT_TIME_COLUMN));
					String id = mCursorChild
							.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
					map.put("id", id);
					map.put("date",
							mCursorChild.getString(InfoColumn.OVER_TIME_COLUMN));
					map.put("type",
							mCursorChild.getString(InfoColumn.TASK_TYPE_COLUMN));
					map.put("name",
							mCursorChild.getString(InfoColumn.USER_NAME_COLUMN));
					map.put("day",
							mCursorChild.getString(InfoColumn.DAYS_COLUMN));
					map.put("state",
							mCursorChild.getString(InfoColumn.STATE_COLUMN));
					tempArray.add(map);
				}

			}
			childArray.add(tempArray);




		}

	}

}
