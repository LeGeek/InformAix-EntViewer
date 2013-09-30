package com.bootongeek.Ical;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Vector;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

public class IcalFileParser {
	private Calendar icalCalendar = null;
	private Vector<EventComponent> vectorEvent;
	private String daysLabel[] = { "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" };
	
	public IcalFileParser(String filePath, int offsetWeek) throws IOException, ParserException{
		FileInputStream fis = new FileInputStream(filePath);
		CalendarBuilder builder = new CalendarBuilder();
		icalCalendar = builder.build(fis);

		vectorEvent = new Vector<EventComponent>();
		for(Iterator it = icalCalendar.getComponents().iterator(); it.hasNext();){
			Component c = (Component) it.next();
			vectorEvent.add(new EventComponent( c.getProperty(Property.SUMMARY).getValue().replace("\n", "<br>"),
												c.getProperty(Property.LOCATION).getValue(),
												c.getProperty(Property.DESCRIPTION).getValue()
													.substring(0, c.getProperty(Property.DESCRIPTION)
																	.getValue()
																	.indexOf("(Exported"))
													.replace("\n", "<br>"),
												c.getProperty(Property.DTSTART).getValue(),
												c.getProperty(Property.DTEND).getValue()));
		}
		
		java.util.Calendar nowDate = java.util.Calendar.getInstance();
		int weekOfYear = nowDate.get(java.util.Calendar.WEEK_OF_YEAR) + offsetWeek;
		
		if(weekOfYear > 52){
			weekOfYear -= 52;
		}
		else if(weekOfYear < -52)
		{
			weekOfYear += 52;
		}
		
		int year = nowDate.get(java.util.Calendar.YEAR);
		nowDate.clear();
		nowDate.setFirstDayOfWeek(java.util.Calendar.MONDAY);
		nowDate.set(java.util.Calendar.WEEK_OF_YEAR, weekOfYear);
		nowDate.set(java.util.Calendar.YEAR, year);
		
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		
		for(int i = 0; i < 6; ++i){
			vectorEvent.add(new EventComponent(daysLabel[i],
												dayFormat.format(nowDate.getTime()) + "/"
												+ monthFormat.format(nowDate.getTime()) + "/"
												+ yearFormat.format(nowDate.getTime()),
												"",
												yearFormat.format(nowDate.getTime())
												+ monthFormat.format(nowDate.getTime())
												+ dayFormat.format(nowDate.getTime()) + "T000000Z",
												yearFormat.format(nowDate.getTime())
												+ monthFormat.format(nowDate.getTime())
												+ dayFormat.format(nowDate.getTime()) + "T020000Z"));
			nowDate.add(java.util.Calendar.DATE, 1);
		}
		
		sortWithBubbleSort();
	}
	
	public Vector<EventComponent> getVector(){
		return vectorEvent;
	}
	
	private void sortWithBubbleSort(){
		boolean switched;
		EventComponent tmpEvent;
		
		do{
			switched = false;
			for(int i = 1; i < vectorEvent.size(); ++i){
				if(vectorEvent.get(i-1).getDateBegin().toLong() > vectorEvent.get(i).getDateBegin().toLong()){
					tmpEvent = vectorEvent.get(i);
					vectorEvent.set(i, vectorEvent.get(i-1));
					vectorEvent.set(i-1, tmpEvent);
					switched = true;
				}
			}
		}while(switched);
	}
	
	private String generatingDateZero(EventComponent event){
		return ("" + event.getDateBegin().year + addZero(event.getDateBegin().month) + addZero(event.getDateBegin().day) + "T000000Z");
	}
	
	private String generatingDateZeroEnd(EventComponent event){
		return ("" + event.getDateBegin().year + addZero(event.getDateBegin().month) + addZero(event.getDateBegin().day) + "T020000Z");
	}
	
	private String addZero(int nbr){
		if(nbr < 10){
			return "0" + nbr;
		}
		else
		{
			return "" + nbr;
		}
	}
	
	public String toString(){
		String ret = "";
		
		for(Iterator it = vectorEvent.iterator(); it.hasNext();){
			EventComponent evt = (EventComponent) it.next();
			ret += evt.getDateBegin().toString() + "\n";
		}
		
		return ret;
	}
}
