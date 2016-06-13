package com.wenzhou.WZWeight.application;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.wenzhou.WZWeight.log.MyLog;

public class HttpClientUtil {
	private static final String TAG = "HttpClientUtil";

	private String url;
	private List<NameValuePair> params;

	public HttpClientUtil(String httpUrl, final List<NameValuePair> params1) {

		url = httpUrl;
		params = params1;

	}

	public JSONObject httpClientCreate() {
		try {

			HttpPost httpPostRequest = new HttpPost(url);
			MyLog.d(TAG, "url is" + url);

			httpPostRequest.addHeader("charset", HTTP.UTF_8);

			HttpEntity httpEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			MyLog.d(TAG, "params is" + params.toString());

			httpPostRequest.setEntity(httpEntity);

			DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

			defaultHttpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 8000);


			defaultHttpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 8000);

			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpPostRequest);

			MyLog.d(TAG, "" + httpResponse.getStatusLine().getStatusCode());

			if (httpResponse.getStatusLine().getStatusCode() == 200) {


				String response = EntityUtils
						.toString(httpResponse.getEntity());

				MyLog.d(TAG, "response ori is :" + response);

				response = response.replaceAll("\r\n|\n\r|\r|\n", "");





				JSONObject item = new JSONObject(response);
				return item;

			} else {
				return null;
			}

		} catch (Exception e) {

			e.printStackTrace();
			MyLog.d(TAG, e.toString());
			return null;

		}

	}

}
