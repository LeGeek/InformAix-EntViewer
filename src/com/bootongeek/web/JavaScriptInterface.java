package com.bootongeek.web;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.WebView;

public class JavaScriptInterface {
	Context context;
	WebView web;
	
	public JavaScriptInterface(Context c, WebView web) {
		context = c;
		this.web = web;
	}
	
	@SuppressWarnings("deprecation")
	public void showAlert(String str){
	      AlertDialog.Builder myDialog
	      = new AlertDialog.Builder(context);
	      myDialog.setTitle("Détail");
	      myDialog.setMessage(str.replace("<br>", "\n"));
	      myDialog.setPositiveButton("Ok", null);
	      myDialog.show();
	}
	
	public void showFile(String file){
		web.loadUrl("file://" + file);
	}
}
