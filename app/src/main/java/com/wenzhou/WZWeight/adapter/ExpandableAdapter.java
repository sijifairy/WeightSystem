package com.wenzhou.WZWeight.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.wenzhou.WZWeight.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpandableAdapter extends BaseExpandableListAdapter {
	Activity activity;
	private int days;



	private List<Map<String, Object>> groupArray = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> tempArray = new ArrayList<Map<String, Object>>();
	private List<List<Map<String, Object>>> childArray = new ArrayList<List<Map<String, Object>>>();

	public ExpandableAdapter(Activity a) {
		activity = a;
	}

	public ExpandableAdapter(Activity a, List<Map<String, Object>> groupArray,
			List<List<Map<String, Object>>> childArray) {
		activity = a;
		this.groupArray = groupArray;
		this.childArray = childArray;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return childArray.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		return childArray.get(groupPosition).size();
	}


	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		Context mContext = activity;

		LayoutInflater inflater = LayoutInflater.from(mContext);

		View layout = inflater.inflate(R.layout.listitem, null);

		TextView title = (TextView) layout.findViewById(R.id.linear_item_title);
		TextView accept = (TextView) layout
				.findViewById(R.id.linear_item_accept);
		TextView id = (TextView) layout.findViewById(R.id.linear_item_id);
		TextView date = (TextView) layout.findViewById(R.id.linear_item_date);
		TextView day = (TextView) layout.findViewById(R.id.linear_item_day);
		TextView state = (TextView) layout.findViewById(R.id.linear_item_state);
		TextView name = (TextView) layout.findViewById(R.id.linear_item_name);
		TextView type = (TextView) layout.findViewById(R.id.linear_item_type);
		TextView newText = (TextView) layout.findViewById(R.id.linear_item_new);
		type.setText("������");

		title.setText((String) childArray.get(groupPosition).get(childPosition)
				.get("title"));
		accept.setText((String) childArray.get(groupPosition)
				.get(childPosition).get("accept"));
		id.setText((String) childArray.get(groupPosition).get(childPosition)
				.get("id"));
		date.setText((String) childArray.get(groupPosition).get(childPosition)
				.get("date"));
		day.setText((String) childArray.get(groupPosition).get(childPosition)
				.get("day"));
		state.setText((String) childArray.get(groupPosition).get(childPosition)
				.get("state"));
		name.setText((String) childArray.get(groupPosition).get(childPosition)
				.get("name"));
		layout.setPadding(65, 2, 2, 2);

		if (accept.getText().toString().equals("null")) {
			layout.setBackgroundColor(Color.rgb(162, 202, 250));
			newText.setVisibility(View.VISIBLE);
		} else {
			int index;
			if (state.getText().toString().equals("")) {
				index = 0;
			} else {
				index = Integer.parseInt(state.getText().toString());
			}

			if (day.getText().toString().equals("")) {
				days = 0;
			} else {
				days = Integer.parseInt(day.getText().toString());
			}

			if (index == 1) {

				if (days < 0) {
					layout.setBackgroundColor(Color.rgb(250, 162, 162));
				} else {
					layout.setBackgroundColor(Color.GRAY);
				}

			} else {

				if (days < 0) {
					layout.setBackgroundColor(Color.rgb(250, 162, 162));
				} else if (days >= 3) {
					layout.setBackgroundColor(Color.rgb(190, 250, 162));
				} else {
					layout.setBackgroundColor(Color.rgb(250, 220, 162));
				}
			}
		}
		return layout;
	}


	public Object getGroup(int groupPosition) {
		return groupArray.get(groupPosition);
	}

	public int getGroupCount() {
		return groupArray.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}


	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {


		Context mContext = activity;
		LayoutInflater inflater = LayoutInflater.from(mContext);

		View layout = inflater.inflate(R.layout.listitem, null);

		TextView title = (TextView) layout.findViewById(R.id.linear_item_title);
		TextView accept = (TextView) layout
				.findViewById(R.id.linear_item_accept);
		TextView id = (TextView) layout.findViewById(R.id.linear_item_id);
		TextView date = (TextView) layout.findViewById(R.id.linear_item_date);
		TextView day = (TextView) layout.findViewById(R.id.linear_item_day);
		TextView state = (TextView) layout.findViewById(R.id.linear_item_state);
		TextView name = (TextView) layout.findViewById(R.id.linear_item_name);
		TextView type = (TextView) layout.findViewById(R.id.linear_item_type);
		TextView newText = (TextView) layout.findViewById(R.id.linear_item_new);

		type.setText("������");

		title.setText((String) groupArray.get(groupPosition).get("title"));
		accept.setText((String) groupArray.get(groupPosition).get("accept"));
		id.setText((String) groupArray.get(groupPosition).get("id"));
		date.setText((String) groupArray.get(groupPosition).get("date"));
		day.setText((String) groupArray.get(groupPosition).get("day"));
		state.setText((String) groupArray.get(groupPosition).get("state"));
		name.setText((String) groupArray.get(groupPosition).get("name"));
		layout.setPadding(50, 2, 2, 2);

		if (accept.getText().toString().equals("null")) {
			layout.setBackgroundColor(Color.rgb(162, 202, 250));
			newText.setVisibility(View.VISIBLE);
		} else {

			int index;
			if (state.getText().toString().equals("")) {
				index = 0;
			} else {
				index = Integer.parseInt(state.getText().toString());
			}

			if (day.getText().toString().equals("")) {
				days = 0;
			} else {
				days = Integer.parseInt(day.getText().toString());
			}

			if (index == 1) {

				if (days < 0) {
					layout.setBackgroundColor(Color.rgb(250, 162, 162));
				} else {
					layout.setBackgroundColor(Color.GRAY);
				}

			} else {

				if (days < 0) {
					layout.setBackgroundColor(Color.rgb(250, 162, 162));
				} else if (days >= 3) {
					layout.setBackgroundColor(Color.rgb(190, 250, 162));
				} else {
					layout.setBackgroundColor(Color.rgb(250, 220, 162));
				}
			}
		}
		return layout;

	}


	public TextView getGenericView(String string) {

		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, 64);
		TextView text = new TextView(activity);
		text.setLayoutParams(layoutParams);

		text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

		text.setPadding(50, 0, 0, 0);
		text.setText(string);
		return text;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}