package com.wenzhou.WZWeight.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wenzhou.WZWeight.application.Constant;

public class DBHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "oatask.db";
	public static final int DATABASE_VERSION = 2;
	public static final String TASKS_TABLE_1 = "taskByMe";
	public static final String TASKS_TABLE_2 = "taskToMe";
	public static final String TASKS_TABLE_3 = "user";

	private static final String DATABASE_CREATE_TABLE1 = "CREATE TABLE IF NOT EXISTS "
			+ TASKS_TABLE_1
			+ " ("
			+ InfoColumn._ID
			+ " integer primary key autoincrement,"
			+ InfoColumn.CALENDAR_DETAIL_ID
			+ " text,"
			+ InfoColumn.CALENDAR_INDEX_ID
			+ " text,"
			+ InfoColumn.AUTHOR
			+ " text,"
			+ InfoColumn.AUTHOR_NAME
			+ " text,"
			+ InfoColumn.START_TIME
			+ " text,"
			+ InfoColumn.AWOKE_TIME
			+ " text,"
			+ InfoColumn.MEMO
			+ " text,"
			+ InfoColumn.LINKMAN
			+ " text,"
			+ InfoColumn.LINK_MODE
			+ " text,"
			+ InfoColumn.GOTO_ADDRESS
			+ " text,"
			+ InfoColumn.STATE
			+ " text,"
			+ InfoColumn.IMPORTANT
			+ " text,"
			+ InfoColumn.OVER_TIME
			+ " datetime,"
			+ InfoColumn.AWOKE_TIMELICE
			+ " text,"
			+ InfoColumn.USER_ID
			+ " text,"
			+ InfoColumn.USER_NAME
			+ " text,"
			+ InfoColumn.TASK_TYPE
			+ " text,"
			+ InfoColumn.TASK_TYPE_DETAIL
			+ " text,"
			+ InfoColumn.TITLE
			+ " text,"
			+ InfoColumn.ACCEPT_TIME
			+ " text,"
			+ InfoColumn.CAL_TYPE
			+ " text,"
			+ InfoColumn.CREAT_TIME
			+ " text,"
			+ InfoColumn.DONE_TIME
			+ " text,"
			+ InfoColumn.PARENT_ID
			+ " text,"
			+ InfoColumn.ATTACHMENT_LIST
			+ " text,"
			+ InfoColumn.ISTRANSFERED
			+ " text,"
			+ InfoColumn.DAYS
			+ " text,"
			+ InfoColumn.IS_NEED_MSG
			+ " text,"
			+ InfoColumn.IS_SPLIT
			+ " text," + InfoColumn.PARENT_ID_NEW + " text);";

	private static final String DATABASE_CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS "
			+ TASKS_TABLE_2
			+ " ("
			+ InfoColumn._ID
			+ " integer primary key autoincrement,"
			+ InfoColumn.CALENDAR_DETAIL_ID
			+ " text,"
			+ InfoColumn.CALENDAR_INDEX_ID
			+ " text,"
			+ InfoColumn.AUTHOR
			+ " text,"
			+ InfoColumn.AUTHOR_NAME
			+ " text,"
			+ InfoColumn.START_TIME
			+ " text,"
			+ InfoColumn.AWOKE_TIME
			+ " text,"
			+ InfoColumn.MEMO
			+ " text,"
			+ InfoColumn.LINKMAN
			+ " text,"
			+ InfoColumn.LINK_MODE
			+ " text,"
			+ InfoColumn.GOTO_ADDRESS
			+ " text,"
			+ InfoColumn.STATE
			+ " text,"
			+ InfoColumn.IMPORTANT
			+ " text,"
			+ InfoColumn.OVER_TIME
			+ " datetime,"
			+ InfoColumn.AWOKE_TIMELICE
			+ " text,"
			+ InfoColumn.USER_ID
			+ " text,"
			+ InfoColumn.USER_NAME
			+ " text,"
			+ InfoColumn.TASK_TYPE
			+ " text,"
			+ InfoColumn.TASK_TYPE_DETAIL
			+ " text,"
			+ InfoColumn.TITLE
			+ " text,"
			+ InfoColumn.ACCEPT_TIME
			+ " text,"
			+ InfoColumn.CAL_TYPE
			+ " text,"
			+ InfoColumn.CREAT_TIME
			+ " text,"
			+ InfoColumn.DONE_TIME
			+ " text,"
			+ InfoColumn.PARENT_ID
			+ " text,"
			+ InfoColumn.ATTACHMENT_LIST
			+ " text,"
			+ InfoColumn.ISTRANSFERED
			+ " text,"
			+ InfoColumn.DAYS
			+ " text,"
			+ InfoColumn.IS_NEED_MSG
			+ " text,"
			+ InfoColumn.IS_SPLIT
			+ " text," + InfoColumn.PARENT_ID_NEW + " text);";

	private static final String DATABASE_CREATE_TABLE3 = "CREATE TABLE IF NOT EXISTS "
			+ TASKS_TABLE_3
			+ " ("
			+ InfoColumn._ID
			+ " integer primary key autoincrement,"
			+ InfoColumn.USER_SHOW_ID
			+ " text,"
			+ InfoColumn.NAME
			+ " text,"
			+ InfoColumn.DEPT_ID
			+ " text," + InfoColumn.PINYIN + " text);";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(DATABASE_CREATE_TABLE1);
		db.execSQL(DATABASE_CREATE_TABLE2);
		db.execSQL(DATABASE_CREATE_TABLE3);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_1);
		db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_2);
		db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_3);
		onCreate(db);

	}

	public void onUpgradeTable(SQLiteDatabase db, int index, int oldVersion,
			int newVersion) {

		if (index == Constant.index_table1) {
			db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_1);
			db.execSQL(DATABASE_CREATE_TABLE1);
		} else if (index == Constant.index_table2) {
			db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_2);
			db.execSQL(DATABASE_CREATE_TABLE2);
		} else if (index == Constant.index_table3) {
			db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_3);
			db.execSQL(DATABASE_CREATE_TABLE3);
		} else {

		}

	}

}
