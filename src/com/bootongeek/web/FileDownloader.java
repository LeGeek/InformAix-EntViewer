package com.bootongeek.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class FileDownloader {
	public interface onDownloadListner{
		public void onDownloadProgress(int progress);
	}

	private onDownloadListner listner = null;
	private int totalBytesRead;
	private String server;
	private String local;
	private String file;
	
	public FileDownloader(String fileURL, String localFolder, String fileName){
		server = fileURL;
		local = localFolder;
		file = fileName;
	}
	
	public FileDownloader(String fileURL, String localFolder) throws IOException{
		new FileDownloader(fileURL, localFolder, fileURL.substring(fileURL.lastIndexOf("/") + 1));
	}

	 
	public void setOnDowloadListner(onDownloadListner onDownloadListner){
		listner = onDownloadListner;
	}
	
	public void startDownload() throws IOException{
		deleteFile(local + "/" + file);
		
		URL url = new URL(server);
		InputStream is = url.openStream();
		FileOutputStream fos = new FileOutputStream(local + "/" + file);
		
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		totalBytesRead = 0;
		while((bytesRead = is.read(buffer)) != -1){
			fos.write(buffer, 0, bytesRead);
			
			totalBytesRead += bytesRead;
			if(listner != null){
				listner.onDownloadProgress(totalBytesRead);
			}
		}
		
		fos.close();
		is.close();	
	}

	private void deleteFile(String pathFile) {
		File f = new File(pathFile);
		
		if(!f.exists() || (f.isDirectory() && f.length() > 0)){
			return;
		}
		
		f.delete();
	}
	
	public String getUrl() { return server; }
	public String getLocalFolder() { return local; }
	public String getFileName() { return file; }
}
