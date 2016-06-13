package com.wenzhou.WZWeight.application;

import android.app.Application;
import android.graphics.drawable.Drawable;

import org.apache.http.client.CookieStore;

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
