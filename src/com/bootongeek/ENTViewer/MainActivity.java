package com.bootongeek.ENTViewer;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.bootongeek.Ical.EventComponent;
import com.bootongeek.Ical.IcalFileParser;
import com.bootongeek.ENTViewer.R;
import com.bootongeek.web.FileDownloader;
import com.bootongeek.web.HtmlCalendar;
import com.bootongeek.web.JavaScriptInterface;
import com.bootongeek.web.FileDownloader.onDownloadListner;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public final String LOCAL_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bootongeek/entviewer";
	public final String LOCAL_FILE_NAME = "icalWeek";
	public final String LOCAL_FILE_SAVED_CALENDAR = LOCAL_FOLDER + "/savecal";
	public final String ENCODING = "UTF-8";
	
	public final int PREF_ACTIVITY = 100;
	
	private ProgressDialog prgDialog;
	private AlertDialog downloadAlert;
	private Thread back = null;
	private IcalFileParser parser;
	private HtmlCalendar htmlCalendar;
	private WebView webView;
	private SharedPreferences prefs;
	private ImageButton imgThisWeek;
	private TextView txtOffset;
	
	private Handler handProgressDialog = new Handler() {
		@Override
		public void handleMessage(Message msg){
			if(msg.getData().containsKey("open")){
				if(msg.getData().getBoolean("open")){
					prgDialog.show();
				}
				else{
					prgDialog.dismiss();
				}
			}
			
			if(msg.getData().containsKey("message")){
				prgDialog.setMessage(msg.getData().getString("message"));
			}
		}
	};
	
	private Handler handProgressAlert = new Handler(){
		@Override
		public void handleMessage(Message msg){
			downloadAlert.setTitle("Erreur");
			downloadAlert.setMessage("Une erreur est survenue : \n" + msg.getData().getString("error")
									+ "\nErreur local : \n" + msg.getData().getString("errorLocal"));
			downloadAlert.show();
			
			webView.loadData("Erreur : <br><pre>" + 
							msg.getData().getString("error") + 
							"</pre><br><br>Erreur local :<br><pre>" + 
							msg.getData().getString("errorLocal") + 
							"</pre><br><br>Retour du site : <div style='border: 1px solid black'>" + EventComponent.prepareHtmlString(getFileToString(LOCAL_FOLDER + "/" + LOCAL_FILE_NAME)) + "</div>"
							,"text/html", ENCODING);
		}
	};
	
	private Handler handWebView = new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.getData().containsKey("html")){
				webView.loadData(msg.getData().getString("html"), "text/html", ENCODING);
			}
		}
	};
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		prgDialog = new ProgressDialog(this);
		prgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		prgDialog.setIndeterminate(true);
		prgDialog.setTitle("Téléchargement en cours...");
		prgDialog.setMessage("Connexion...");
		prgDialog.setProgress(0);
		prgDialog.setCanceledOnTouchOutside(false);
		prgDialog.setCancelable(false);
		prgDialog.setButton("Annuler", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				prgDialog.setMessage("Annulation en cours...");
				back.interrupt();
				dialog.dismiss();
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		downloadAlert = builder.create();
		downloadAlert.setButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JavaScriptInterface(this, webView), "AndroidScript");
		webView.setInitialScale(100);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		
		ImageButton imgBtnNext = (ImageButton) findViewById(R.id.imageButtonNextWeek);
		imgBtnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToNextWeek();
			}
		});
		
		ImageButton imgBtnPrev = (ImageButton) findViewById(R.id.imageButtonPrevWeek);
		imgBtnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToPrevWeek();
			}
		});
		
		imgThisWeek = (ImageButton) findViewById(R.id.imageButtonThisWeek);
		imgThisWeek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetOffsetWeek();
			}
		});
		
		txtOffset = (TextView) findViewById(R.id.textViewOffsetWeek);
		
		checkVisibilityBtnThisWeek();
		
		File dir = new File(LOCAL_FOLDER);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}

	private void loadSavedCalendar() {
		String ret = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(LOCAL_FILE_SAVED_CALENDAR));
			
			String tmpLine;
			while ((tmpLine = br.readLine()) != null) {
				ret += tmpLine;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		webView.loadData(ret, "text/html", ENCODING);
	}

	public void onStart(){
		super.onStart();
		loadSavedCalendar();
	}
	
	public void onStop(){
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId())
		{
			case R.id.action_refresh:
				runDownloadThread();
				return true;
				
			case R.id.action_thisWeek:
				resetOffsetWeek();
				return true;
				
			case R.id.preferences:
				startActivityForResult(new Intent(this, MenuPreference.class), PREF_ACTIVITY);
				return true;
				
			case R.id.action_help:
				startActivity(new Intent(this, HelpActivity.class));
				return true;
				
			case R.id.action_nextWeek:
				goToNextWeek();
				return true;
			
			case R.id.action_prevWeek:
				goToPrevWeek();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == PREF_ACTIVITY){
			runDownloadThread();
			checkVisibilityBtnThisWeek();
		}
		
	}

	private void runDownloadThread() {
		if(back != null)
		{
			back.interrupt();
		}
		
		back = generateDownloadThread(generateUrlCalendar(Integer.valueOf(prefs.getString("offset_week", "0"))), LOCAL_FOLDER, LOCAL_FILE_NAME);
		back.start();
	}
	
	private Thread generateDownloadThread(final String url, final String folder, final String filename){
		return new Thread(new Runnable() {

			Bundle b = new Bundle();
			public void run() {
				try{
					//Bundle bdl = new Bundle();
					b.putBoolean("open", true);
					b.putString("message", "Préparation au téléchargement...");
					sendMessageToProgressDialog(b);
					
					FileDownloader fd = new FileDownloader(url, folder, filename);
					fd.setOnDowloadListner(new onDownloadListner() {
						@Override
						public void onDownloadProgress(int progress) {
							Bundle tmpBdl = new Bundle();
							tmpBdl.putString("message", "Téléchargement : " + progress + "b");
							sendMessageToProgressDialog(tmpBdl);
						}
					});
					fd.startDownload();
					
					b.putBoolean("open", true);
					b.putString("message", "Parsage du fichier en cours...");
					sendMessageToProgressDialog(b);
					parser = new IcalFileParser(folder + "/" + filename, Integer.valueOf(prefs.getString("offset_week", "0")));
					
					b.putString("message", "Affichage");
					sendMessageToProgressDialog(b);
					htmlCalendar = new HtmlCalendar(parser, MainActivity.this);
					b.putString("html", htmlCalendar.toString());
					sendMessageToWebView(b);
					
					b.putString("message", "Enregistrement...");
					sendMessageToProgressDialog(b);
					saveCalendar(htmlCalendar.toString());
					
					b.putString("message", "Traitement terminé !");
					sendMessageToProgressDialog(b);
					Thread.sleep(1000);
					b.putBoolean("open", false);
					sendMessageToProgressDialog(b);
				}
				catch(Throwable t){
					Bundle bdl = new Bundle();
					bdl.putString("error", t.getMessage());
					bdl.putString("errorLocal",  t.getLocalizedMessage());
					bdl.putBoolean("open", false);
					sendMessageToProgressAlert(bdl);
					sendMessageToProgressDialog(bdl);
					t.printStackTrace();
				}
			}
			
			private void sendMessageToProgressDialog(Bundle bdl){
				Message msg = handProgressDialog.obtainMessage();
				msg.setData(bdl);
				handProgressDialog.sendMessage(msg);
			}
			
			private void sendMessageToProgressAlert(Bundle bdl){
				Message msg = handProgressAlert.obtainMessage();
				msg.setData(bdl);
				handProgressAlert.sendMessage(msg);
			}
			
			private void sendMessageToWebView(Bundle bdl){
				Message msg = handWebView.obtainMessage();
				msg.setData(bdl);
				handWebView.sendMessage(msg);
			}
			
			private void saveCalendar(String str){
				try {
					File f = new File(LOCAL_FILE_SAVED_CALENDAR);
					
					if(!f.exists()){
						f.createNewFile();
					}
					
					FileWriter fw = new FileWriter(f, false);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(str);
					bw.close();
				}
				catch (IOException e) {
					Bundle b = new Bundle();
					b.putString("error", "Le fichier de sauvegarde n'a pu être créé\n" + e.getMessage());
					b.putString("errorLocal", e.getLocalizedMessage());
				}
			}
		});
	}
	
	private String generateUrlCalendar(int offsetWeek){
		//"http://planning.univ-amu.fr/ade/custom/modules/plannings/anonymous_cal.jsp?resources=8400&projectId=26&startDay=16&startMonth=09&startYear=2013&endDay=22&endMonth=09&endYear=2013&calType=ical"
		Calendar nowDate = Calendar.getInstance();
		int weekOfYear = nowDate.get(Calendar.WEEK_OF_YEAR) + offsetWeek;
		
		if(weekOfYear > 52){
			weekOfYear -= 52;
		}
		else if(weekOfYear < -52)
		{
			weekOfYear += 52;
		}
		
		int year = nowDate.get(Calendar.YEAR);
		nowDate.clear();
		nowDate.setFirstDayOfWeek(Calendar.SUNDAY);
		nowDate.set(Calendar.WEEK_OF_YEAR, weekOfYear);
		nowDate.set(Calendar.YEAR, year);
		
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String ret = "http://planning.univ-amu.fr/ade/custom/modules/plannings/anonymous_cal.jsp?";
		ret += "resources=" + prefs.getString("id_value", "8385");
		ret += "&projectId=26";
		ret += "&startDay=" + dayFormat.format(nowDate.getTime());
		ret += "&startMonth=" + monthFormat.format(nowDate.getTime());
		ret += "&startYear=" + yearFormat.format(nowDate.getTime());
		nowDate.add(Calendar.DATE, 7);
		ret += "&endDay=" + dayFormat.format(nowDate.getTime());
		ret += "&endMonth=" + monthFormat.format(nowDate.getTime());
		ret += "&endYear=" + yearFormat.format(nowDate.getTime());
		ret += "&calType=ical";
		
		return ret;
	}
	
	private void goToNextWeek(){
		int val = Integer.valueOf(prefs.getString("offset_week", "0"));
		if(val == 52){
			Toast.makeText(this, "Impossible d'effectuer le décalage", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Editor edit = prefs.edit();
		edit.putString("offset_week", (++val) + "");
		edit.commit();
		
		Toast.makeText(this, "Nouveau décalage : " + prefs.getString("offset_week", "ERR"), Toast.LENGTH_SHORT).show();
		runDownloadThread();
		checkVisibilityBtnThisWeek();
	}
	
	private void goToPrevWeek(){
		int val = Integer.valueOf(prefs.getString("offset_week", "0"));
		if(val == -52){
			Toast.makeText(this, "Impossible d'effectuer le décalage", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Editor edit = prefs.edit();
		edit.putString("offset_week", (--val) + "");
		edit.commit();
		
		Toast.makeText(this, "Nouveau décalage : " + prefs.getString("offset_week", "ERR"), Toast.LENGTH_SHORT).show();
		runDownloadThread();
		checkVisibilityBtnThisWeek();
	}
	
	private void resetOffsetWeek(){
		Editor edit = prefs.edit();
		edit.putString("offset_week", "0");
		edit.commit();
		runDownloadThread();
		checkVisibilityBtnThisWeek();
		
		Toast.makeText(this, "Décalage réinitialisé", Toast.LENGTH_SHORT).show();
	}
	
	private void checkVisibilityBtnThisWeek(){
		int val = Integer.valueOf(prefs.getString("offset_week", "0"));
		if(val == 0){
			imgThisWeek.setEnabled(false);
			imgThisWeek.setVisibility(ImageView.INVISIBLE);
			txtOffset.setEnabled(false);
			txtOffset.setVisibility(ImageView.INVISIBLE);
		}
		else{
			imgThisWeek.setEnabled(true);
			imgThisWeek.setVisibility(ImageView.VISIBLE);
			txtOffset.setEnabled(true);
			txtOffset.setVisibility(ImageView.VISIBLE);
			txtOffset.setText(val + "");
		}
	}

	private String getFileToString(String file){
		String ret = "";
		String tmpLine;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((tmpLine = br.readLine()) != null) {
				ret += tmpLine;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
}
