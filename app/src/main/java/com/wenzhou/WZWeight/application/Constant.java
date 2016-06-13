package com.wenzhou.WZWeight.application;

import com.wenzhou.WZWeight.sqlite.DataBaseAdapter;

public class Constant {
	//public static final String serverUrl = "http://124.205.167.4:8030/Region.asmx";
	public static final String serverUrl = "http://118.145.16.201:8922/Data.asmx";
	
	public static final String login = serverUrl + "/GetChildrenList";
	
	public static final String aboutus = serverUrl + "/GetAboutUs";
	
	public static final String getregioninfo = serverUrl + "/GetRegionInfo";
	public static final String getrubbishsourceinfo = serverUrl + "/GetRubbishSourceInfo";
	public static final String getcartypeinfo = serverUrl + "/GetCarTypeInfo";
	public static final String getpoundinfo = serverUrl + "/GetPoundInfo";
	public static final String getclientinfo = serverUrl + "/GetClientInfo";
	public static final String getrubbishtypeinfo = serverUrl + "/GetRubbishTypeInfo";

	public static final String yesterdayweight = serverUrl + "/YesterdayWeight";
	public static final String getweightdetail = serverUrl + "/GetWeightDetail";
	
	public static final String getcardetail = serverUrl + "/GetCarDetail";
	public static final String insertcar = serverUrl + "/InsertCar";
	public static final String editcar = serverUrl + "/UpdateCar";
	public static final String deletecar = serverUrl + "/DeleteCar";
	
	public static final String getcarddetail = serverUrl + "/GetCardDetail";
	public static final String insertcard = serverUrl + "/InsertCard";
	public static final String editcard = serverUrl + "/UpdateCard";
	public static final String deletecard = serverUrl + "/DeleteCard";
	public static final String getnocardcar = serverUrl + "/GetNoCardCar";
	
	public static final int USER_LOAD = 1;
	public static final int USER_RESPONSE = 2;
	public static final int TASK_INSERT = 3;
	public static final int TASK_INSERT_RESPONSE = 4;
	public static final int TASK_GET = 5;
	public static final int TASK_GET_RESPONSE = 6;
	public static final int TASK_GET_DETAIL = 7;
	public static final int TASK_GET_DETAIL_RESPONSE = 8;
	public static final int USERS_UPDATE = 9;
	public static final int USERS_UPDATE_RESPONSE = 10;
	public static final int TASK_CHECK = 11;
	public static final int TASK_CHECK_RESPONSE = 12;
	public static final int BACKGROUND_TASK_LIST_GET = 13;
	public static final int BACKGROUND_TASK_LIST_GET_REESPONSE = 14;
	public static final int NEW_CHILD_TASK = 15;
	public static final int NEW_CHILD_TASK_RESPONSE = 16;
	public static final String EDIT = "com.ningbo.OASystem.edit";
	public static final String PLAN = "com.ningbo.OASystem.plan";
	public static final String INSERT = "com.ningbo.OASystem.insert";

	public static final int index_table1 = 1;
	public static final int index_table2 = 2;
	public static final int index_table3 = 3;
	public static int countNew;

	static DataBaseAdapter mDataBaseAdapter;

	public static final long PULLPARIOD = 1000 * 60 * 10;
	public static String session;

	public static void setSession(String s) {
		session = s;
	}

	public static String getSession() {
		return session;
	}

	public static String author;

	public static void setAuthor(String s) {
		author = s;
	}

	public static String getAuthor() {
		return author;
	}

	public static String authorId;

	public static void setAuthorId(String s) {
		authorId = s;
	}

	public static String getAuthorId() {
		return authorId;
	}
	//my cache
	public static String UserName = "";

	public static void setUserName(String s) {
		UserName = s;
	}

	public static String getUserName() {
		return UserName;
	}
	
	public static String UserClass = "";

	public static void setUserClass(String s) {
		UserClass = s;
	}

	public static String getUserClass() {
		return UserClass;
	}
	
	public static String AccountName = "";

	public static void setAccountName(String s) {
		AccountName = s;
	}

	public static String getAccountName() {
		return AccountName;
	}

	public static String SessionID = "";

	public static void setSessionID(String s) {
		SessionID = s;
	}

	public static String getSessionID() {
		return SessionID;
	}
	public static final String[] task_tyoe = { "鏈敓鏂ゆ嫹閿燂�?", "閿熸枻鎷烽敓鏂ゆ嫹閿燂拷" };

	public static final String[] sms = { "閿熸枻鎷�?", "閿熸枻鎷�?" };

	public static String[] userArray;
	public static String[] userIdArray;

}
