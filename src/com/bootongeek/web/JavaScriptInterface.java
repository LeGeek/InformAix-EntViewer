package com.bootongeek.web;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class JavaScriptInterface {
	Context context;
	
	public JavaScriptInterface(Context c) {
		context = c;
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
}
