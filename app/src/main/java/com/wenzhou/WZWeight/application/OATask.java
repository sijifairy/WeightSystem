package com.wenzhou.WZWeight.application;

import org.apache.http.client.CookieStore;

import android.app.Application;
import android.graphics.drawable.Drawable;

public class OATask extends Application{
	
    private CookieStore cookies; 
    
    public CookieStore getCookie(){   
        return cookies;
    }
    public void setCookie(CookieStore cks){
        cookies = cks;
    }
	@Override
	
	public Drawable getWallpaper() {

		return super.getWallpaper();
	}
    
    

}
