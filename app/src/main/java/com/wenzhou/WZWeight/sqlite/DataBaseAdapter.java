package com.wenzhou.WZWeight.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;

public class DataBaseAdapter {
	private static final String TAG = "DataBaseAdapter";
	private Context mContext = null;
	private SQLiteDatabase mSQLiteDataBase = null;
	private DBHelper mDBHelper = null;

	public DataBaseAdapter(Context context) {
		mContext = context;
	}

	public void open() throws SQLException {
		mDBHelper = new DBHelper(mContext);
		if (mSQLiteDataBase == null) {
			mSQLiteDataBase = mDBHelper.getWritableDatabase();
		} else {
			mSQLiteDataBase.close();
			mSQLiteDataBase = mDBHelper.getWritableDatabase();
		}
	}

	public void close() {
		mSQLiteDataBase.close();
		mDBHelper.close();

	}

	public void delete() {
		mDBHelper.onUpgrade(mSQLiteDataBase, 1, 2);
	}

	public void deleteTable(int index) {

		mDBHelper.onUpgradeTable(mSQLiteDataBase, index, 1, 2);
	}


	public long insertData(int index, ContentValues values) {

		if (index == Constant.index_table1) {
			return mSQLiteDataBase.insert(DBHelper.TASKS_TABLE_1,
					InfoColumn._ID, values);
		} else if (index == Constant.index_table2) {
			return mSQLiteDataBase.insert(DBHelper.TASKS_TABLE_2,
					InfoColumn._ID, values);
		} else if (index == Constant.index_table3) {
			long intTemp = mSQLiteDataBase.insert(DBHelper.TASKS_TABLE_3,
					InfoColumn._ID, values);
			return intTemp;
		} else {
			MyLog.e(TAG, "wrong index in insert");
			return index;
		}
	}

	public boolean deleteData(int index, String rowId) {

		if (index == Constant.index_table2) {
			MyLog.e(TAG, "wrong index in delete");
			return false;
		} else if (index == Constant.index_table1) {
			return mSQLiteDataBase.delete(DBHelper.TASKS_TABLE_1,
					InfoColumn.CALENDAR_DETAIL_ID + "=" + rowId, null) > 0;
		} else if (index == Constant.index_table3) {
			return mSQLiteDataBase.delete(DBHelper.TASKS_TABLE_3,
					InfoColumn.CALENDAR_DETAIL_ID + "=" + rowId, null) > 0;
		} else {
			return false;
		}
	}




	public Cursor fetchAllData(int index, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		Cursor mCursor;

		if (index == Constant.index_table1) {
			mCursor = mSQLiteDataBase.query(DBHelper.TASKS_TABLE_1, projection,
					selection, selectionArgs, null, null, sortOrder);

		} else if (index == Constant.index_table2) {
			mCursor = mSQLiteDataBase.query(DBHelper.TASKS_TABLE_2, projection,
					selection, selectionArgs, null, null, sortOrder);
		} else {
			mCursor = mSQLiteDataBase.query(DBHelper.TASKS_TABLE_3, projection,
					selection, selectionArgs, null, null, sortOrder);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}



	public Cursor fetchData(int index, long rowId, String[] projection,
			String sortOrder) {
		Cursor mCursor;

		if (index == Constant.index_table1) {
			mCursor = mSQLiteDataBase.query(true, DBHelper.TASKS_TABLE_1,
					projection, InfoColumn._ID + "=" + rowId, null, null, null,
					sortOrder, null);

		} else if (index == Constant.index_table2) {
			mCursor = mSQLiteDataBase.query(true, DBHelper.TASKS_TABLE_2,
					projection, InfoColumn._ID + "=" + rowId, null, null, null,
					sortOrder, null);
		} else {
			mCursor = mSQLiteDataBase.query(true, DBHelper.TASKS_TABLE_3,
					projection, InfoColumn._ID + "=" + rowId, null, null, null,
					sortOrder, null);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}

	public boolean updateData(int index, long rowId, ContentValues values) {

		if (index == Constant.index_table1) {
			return mSQLiteDataBase.update(DBHelper.TASKS_TABLE_1, values,
					InfoColumn._ID + "=" + rowId, null) > 0;
		} else if (index == Constant.index_table2) {
			return mSQLiteDataBase.update(DBHelper.TASKS_TABLE_2, values,
					InfoColumn._ID + "=" + rowId, null) > 0;
		} else if (index == Constant.index_table3) {
			return mSQLiteDataBase.update(DBHelper.TASKS_TABLE_3, values,
					InfoColumn._ID + "=" + rowId, null) > 0;
		} else {
			MyLog.e(TAG, "wrong index in update");
			return false;
		}

	}

	public boolean updateData(int index, String itemId, ContentValues values) {

		if (index == Constant.index_table1) {
			return mSQLiteDataBase.update(DBHelper.TASKS_TABLE_1, values,
					InfoColumn.CALENDAR_DETAIL_ID + "=" + itemId, null) > 0;
		} else if (index == Constant.index_table2) {
			return mSQLiteDataBase.update(DBHelper.TASKS_TABLE_2, values,
					InfoColumn.CALENDAR_DETAIL_ID + "=" + itemId, null) > 0;
		} else if (index == Constant.index_table3) {
			return mSQLiteDataBase.update(DBHelper.TASKS_TABLE_3, values,
					InfoColumn.CALENDAR_DETAIL_ID + "=" + itemId, null) > 0;
		} else {
			MyLog.e(TAG, "wrong index in update");
			return false;
		}

	}

}
