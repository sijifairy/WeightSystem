package com.wenzhou.WZWeight.sqlite;

import android.provider.BaseColumns;

public class InfoColumn implements BaseColumns {

	public InfoColumn() {
	}

	public static final String CALENDAR_DETAIL_ID = "calendar_detail_id";
	public static final String CALENDAR_INDEX_ID = "calendar_index_id";
	public static final String AUTHOR = "author";
	public static final String AUTHOR_NAME = "author_name";
	public static final String START_TIME = "start_time";
	public static final String AWOKE_TIME = "awoke_time";
	public static final String MEMO = "memo";
	public static final String LINKMAN = "linkman";
	public static final String LINK_MODE = "link_mode";
	public static final String GOTO_ADDRESS = "goto_address";
	public static final String STATE = "state";
	public static final String IMPORTANT = "important";
	public static final String OVER_TIME = "over_time";
	public static final String AWOKE_TIMELICE = "awoke_timeslice";
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "user_name";
	public static final String TASK_TYPE = "task_type";
	public static final String TASK_TYPE_DETAIL = "task_type_detail";
	public static final String TITLE = "title";
	public static final String ACCEPT_TIME = "accept_time";
	public static final String CAL_TYPE = "cal_type";
	public static final String CREAT_TIME = "creat_time";
	public static final String DONE_TIME = "done_time";
	public static final String PARENT_ID = "parent_id";
	public static final String ATTACHMENT_LIST = "attachment_list";
	public static final String ISTRANSFERED = "isTransfered";
	public static final String IS_NEED_MSG = "is_need_msg";
	public static final String DAYS = "days";
	public static final String IS_SPLIT = "IsSplit";
	public static final String PARENT_ID_NEW = "ParentID";

	public static final String USER_SHOW_ID = "user_show_id";
	public static final String NAME = "name";
	public static final String DEPT_ID = "dept_id";
	public static final String PINYIN = "pinyin";

	public static final int _ID_COLUMN = 0;
	public static final int CALENDAR_DETAIL_ID_COLUMN = 1;
	public static final int CALENDAR_INDEX_ID_COLUMN = 2;
	public static final int AUTHOR_COLUMN = 3;
	public static final int AUTHOR_NAME_COLUMN = 4;
	public static final int START_TIME_COLUMN = 5;
	public static final int AWOKE_TIME_COLUMN = 6;
	public static final int MEMO_COLUMN = 7;
	public static final int LINKMAN_COLUMN = 8;
	public static final int LINK_MODE_COLUMN = 9;
	public static final int GOTO_ADDRESS_COLUMN = 10;
	public static final int STATE_COLUMN = 11;
	public static final int IMPORTANT_COLUMN = 12;
	public static final int OVER_TIME_COLUMN = 13;
	public static final int AWOKE_TIMELICE_COLUMN = 14;
	public static final int USER_ID_COLUMN = 15;
	public static final int USER_NAME_COLUMN = 16;
	public static final int TASK_TYPE_COLUMN = 17;
	public static final int TASK_TYPE_DETAIL_COLUMN = 18;
	public static final int TITLE_COLUMN = 19;
	public static final int ACCEPT_TIME_COLUMN = 20;
	public static final int CAL_TYPE_COLUMN = 21;
	public static final int CREAT_TIME_COLUMN = 22;
	public static final int DONE_TIME_COLUMN = 23;
	public static final int PARENT_ID_COLUMN = 24;
	public static final int ATTACHMENT_LIST_COLUMN = 25;
	public static final int ISTRANSFERED_COLUMN = 26;
	public static final int IS_NEED_MSG_COLUMN = 27;
	public static final int DAYS_COLUMN = 28;
	public static final int IS_SPLIT_COLUMN = 29;
	public static final int PARENT_ID_NEW_COLUMN = 30;

	public static final int USER_SHOW_ID_COLUMN = 1;
	public static final int NAME_COLUMN = 2;
	public static final int DEPT_ID_COLUMN = 3;

	public static final String[] PROJECTION = { _ID, CALENDAR_DETAIL_ID,
			CALENDAR_INDEX_ID, AUTHOR, AUTHOR_NAME, START_TIME, AWOKE_TIME,
			MEMO, LINKMAN, LINK_MODE, GOTO_ADDRESS, STATE, IMPORTANT,
			OVER_TIME, AWOKE_TIMELICE, USER_ID, USER_NAME, TASK_TYPE,
			TASK_TYPE_DETAIL, TITLE, ACCEPT_TIME, CAL_TYPE, CREAT_TIME,
			DONE_TIME, PARENT_ID, ATTACHMENT_LIST, ISTRANSFERED, IS_NEED_MSG,
			DAYS,IS_SPLIT,PARENT_ID_NEW, };

	public static final String[] PROJECTION_USER = { _ID, USER_SHOW_ID, NAME,
			DEPT_ID, PINYIN, };

}
