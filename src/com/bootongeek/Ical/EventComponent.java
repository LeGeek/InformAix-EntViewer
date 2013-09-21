package com.bootongeek.Ical;

public class EventComponent {
	public class IcalDate{
		public int year;
		public int month;
		public int day;
		
		public int hour;
		public int minute;
		public int second;
		
		public IcalDate(String str){
			year = Integer.valueOf(str.substring(0, 4));
			month = Integer.valueOf(str.substring(4, 6));
			day = Integer.valueOf(str.substring(6, 8));
			hour = Integer.valueOf(str.substring(9, 11)) + 2;
			minute = Integer.valueOf(str.substring(11, 13));
			second = Integer.valueOf(str.substring(13, 15));
		}
		
		public String getDateToString(){
			return (day + "/" + month + "/" + year);
		}
		
		public long getTimeToLong(){
			return ((hour * 100 + minute) * 100 + second);
		}
		
		public String toString(){
			return (year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second);
		}
		
		public String toIcalString(){
			return (year + month + day + "T" + hour + minute + second + "Z");
		}
		
		public long toLong(){
			long l = year*100 + month;
			l = ((((l * 100 + day) * 100 + hour) * 100 + minute ) * 100 + second);
			return l;
		}
	}
	
	private String summary;
	private String location;
	private String description;
	private IcalDate dateBegin;
	private IcalDate dateEnd;
	
	public EventComponent(String sum, String loc, String descr, String dateB, String dateE){
		summary = sum.replace('\n', ' ');
		location = loc.replace('\n', ' ');
		description = descr.replace('\n', ' ');
		dateBegin = new IcalDate(dateB);
		dateEnd = new IcalDate(dateE);
	}
	
	public String getSummary(){
		return summary;
	}
	public String getLocation(){
		return location;
	}
	public String getDescription(){
		return description;
	}
	public IcalDate getDateBegin(){
		return dateBegin;
	}
	public IcalDate getDateEnd(){
		return dateEnd;
	}
	
	public String toString(){
		return ("Summary : " + summary + "\n" +
				"Location : " + location + "\n" + 
				"Description : " + description + "\n" +
				"Date Begin : " + dateBegin + "\n" + 
				"Date End : " + dateEnd);
	}
}
