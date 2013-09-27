package com.bootongeek.ENTViewer;

import com.bootongeek.ENTViewer.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		((WebView) findViewById(R.id.webViewHelp)).loadUrl("file:///android_asset/help.html");
		((Button) findViewById(R.id.btnHelpFermer)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
