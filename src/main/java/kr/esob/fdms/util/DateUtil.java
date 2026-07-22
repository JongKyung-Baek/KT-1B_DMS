package kr.esob.fdms.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.value.Constant;

@Service
public class DateUtil {

    /**
     * 현재시간
     * @param format
     * @return
     * @throws ParseException
     */
    public Date toDate(String date) throws ParseException {

		String format = "yyyy" + Constant.DATE_DELIMITER + "MM" + Constant.DATE_DELIMITER + "dd";

        SimpleDateFormat type = new SimpleDateFormat(format);
        return type.parse(date);
    }

    public static String getDateTimeString(Date date) {
    	if(date == null) {
    		return "";
    	}
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
    }

    public static String getDateString(Date date) {
    	if(date == null) {
    		return "";
    	}
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	return format.format(date);
    }

    /**
     * 특정 개월수 더하기(YYYYMMMDD)
     * @param month
     * @return
     */
    public String getAddMonth(String month, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, Integer.parseInt(month));

		DateFormat df = new SimpleDateFormat(format);
		return df.format(cal.getTime());
	}


    /**
     * 특정 일수 더하기(YYYYMMMDD)
     * @param day
     * @return
     */
    public static String getAddDay(String day, String format) {
		return getAddDay(Integer.parseInt(day), format);
	}

    /**
     * 특정 일수 더하기(YYYYMMMDD)
     * @param day
     * @param format
     * @return
     */
    public static String getAddDay(Integer day, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, day);

		DateFormat df = new SimpleDateFormat(format);
		return df.format(cal.getTime());
	}

    /**
     * 특정 개월 수 더하기
     * @param day
     * @param format
     * @return
     */
    public static String getAddMonth(Integer day, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, day);

		DateFormat df = new SimpleDateFormat(format);
		return df.format(cal.getTime());
	}


	/**
	 * 오늘날짜 (YYYYMMDD)
	 * @param month
	 * @return
	 */
	public static String getToday(String format) {
		Date time = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(time);
	}

	/**
	 * 날짜 포멧을 yyyymmdd로 변경
	 * @param dt
	 * @return
	 */
	public static String getYmd(String dt) {
		if(dt == null) {
    		return "";
    	}
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date convertedCurrentDate = format.parse(dt);
			return format.format(convertedCurrentDate);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getDiffMonth(Date start, Date end) {
		int month1 = start.getYear() * 12 + start.getMonth();
		int month2 = end.getYear() * 12 + end.getMonth();

		return String.valueOf(month2 - month1);
	}

}
