package com.bootongeek.Ical;

import java.io.FileInputStream;
import java.io.IOException;
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
	
	
	public IcalFileParser(String filePath) throws IOException, ParserException{
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
	}
	
	public Vector<EventComponent> getVector(){
		return vectorEvent;
	}
	
	public void sortWithBubbleSort(){
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
	
	public String toString(){
		String ret = "";
		
		for(Iterator it = vectorEvent.iterator(); it.hasNext();){
			EventComponent evt = (EventComponent) it.next();
			ret += evt.getDateBegin().toString() + "\n";
		}
		
		return ret;
	}
}
