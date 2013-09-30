package com.bootongeek.web;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bootongeek.Ical.EventComponent;
import com.bootongeek.Ical.IcalFileParser;
import com.bootongeek.Ical.EventComponent.IcalDate;

public class HtmlCalendar {
	private String html = "";
	private int[] tabTime = {2, 8, 10, 13, 15, 17};
	
	public HtmlCalendar(IcalFileParser ifp, Context c){
		if(ifp.getVector().size() == 0) return;
		Vector<EventComponent> tmpVect = ifp.getVector();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		
		String tmpDate = tmpVect.get(0).getDateBegin().getDateToString();
		addLine("Version en b&ecirc;ta !");
		addLine("<head>" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
				"<style type='text/css'>");
		
		if(!prefs.getBoolean("auto_width_col", false)){
			addLine("body { width: " + (Integer.valueOf(prefs.getString("width_col", "100")) * 6) + ";}");
		}
		
		addLine("td { border: 1px solid black; padding: 5px;}" +
				".tete { font-weight: bold; text-align: center; font-size: 1.2em;}" +
				"</style>" +
				"</head>" +
				"<body>");
		
		addLine("<table style=\"border-collapse: collapse; border: 2px solid black;\">" +
				"<tr>" +
				"<td class='tete'>Jour</td>" +
				"<td class='tete'>8h - 10h</td>" +
				"<td class='tete'>10h - 12h</td>" +
				"<td class='tete'>13h - 15h</td>" +
				"<td class='tete'>15h - 17h</td>" +
				"<td class='tete'>17h - 19h</td>" +
				"</tr>");
		
		int currentTimeIndex = 0;
		
		for(int i=0; i < tmpVect.size(); ++i){
			IcalDate deb = tmpVect.get(i).getDateBegin();
			long duration = (tmpVect.get(i).getDateEnd().getTimeToLong() - deb.getTimeToLong())/10000;
			
			if(!deb.getDateToString().equals(tmpDate)){
				fillEmptyCells(currentTimeIndex);
				tmpDate = deb.getDateToString();
				addLine("</tr><tr>");
				currentTimeIndex = 0;
			}

			while(tabTime[currentTimeIndex] != deb.hour){
				createEmptyCells(1);
				++currentTimeIndex;
			}
			
			addLine("<td colspan='" + duration / 2 +"' style='text-align: center; border: 1px solid black;' " +
					"onclick=\"AndroidScript.showAlert('Titre :<br>" +
					tmpVect.get(i).getSummary() + "<br>" +
					"<br>Description :" +
					tmpVect.get(i).getDescription() +
					"<br>Location :<br>" +
					tmpVect.get(i).getLocation() + "');\">");
			addLineBr("<strong>" + tmpVect.get(i).getSummary() + "</strong>");
			addLine("<em>" + tmpVect.get(i).getLocation() + "</em>");
			addLine("</td>");
			
			currentTimeIndex += duration/2;
		}
		fillEmptyCells(currentTimeIndex);
		
		addLine("</body></table>");
	}
	
	public String toString(){
		return html;
	}
	
	private void addLineBr(String txt){
		html += txt + "<br>";
	}
	
	private void addLine(String txt){
		html += txt;
	}
	
	private void fillEmptyCells(int index){
		while(index < tabTime.length){
			addLine("<td style='border: 1px solid black;'></td>");
			++index;
		}
	}
	
	private void createEmptyCells(int number){
		for(int i = 0; i < number; ++i){
			addLine("<td style='border: 1px solid black;'></td>");
		}
	}
}
