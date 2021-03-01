/**
 * this class is used for calculate time and format date in EPG
 */
package com.ktc.epg.epg;

import com.ktc.epg.epgUtil.EPGUtil;
import com.ktc.epg.epgUtil.Util;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EPGTimeConvert {
    private static final String TAG = "EPGTimeConvert";
    private static EPGTimeConvert tmConvert;

    private EPGTimeConvert() {

    }

    public static EPGTimeConvert getInstance() {
        if (tmConvert == null) {
            tmConvert = new EPGTimeConvert();
        }
        return tmConvert;
    }

    /**
     * convert hour to milliseconds
     *
     * @param hour
     * @return milliseconds
     */
    public long getHourtoMsec(int hour) {
        long msec = (long) hour * 60 * 60;
        return msec;
    }

    /**
     * calculate EPG program width ,total width is 1
     *
     * @param endTime   program end time
     * @param startTime program start time
     * @return width
     */
    public static float countShowWidth(long endTime, long startTime) {
        return (float) (endTime - startTime) / (60 * EPGConfig.mTimeSpan * 60L);
    }

    /**
     * calculate EPG program width
     *
     * @param duration program playing time
     * @return width
     */
    public float countShowWidth(long duration) {
        return (float) duration / (60 * EPGConfig.mTimeSpan * 60L);
    }

    /**
     * convert date to special string
     *
     * @param date
     * @return
     */
    public String getDetailDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("E,dd-MM-yyyy HH:mm:ss", EPGUtil.getLocaleLan());
        return formatter.format(date);
    }

    /**
     * calculate the time to set
     *
     * @param curTime   current time
     * @param day       current day is 0
     * @param startHour
     * @return
     */
    public long setDate(long curTime, int day, long startHour) {
        MtkLog.e(TAG, "setDate:" + day + "==>" + startHour);   //curTime  already modify hour,no need - startHour
        return (day * 24 + startHour) * 60 * 60 + curTime;   //fix CR DTV00584308
    }

    /**
     * get special date string, format (21-03-2011)
     *
     * @param date
     * @return
     */
    public String getSimpleDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",
                EPGUtil.getLocaleLan());
        return formatter.format(date);
    }


    /**
     * getspecial date string, format (03:15 - 04:30 Mon, 03-Jan)
     *
     * @param mTVProgramInfo
     * @return
     */
    public String formatProgramTimeInfo(EPGProgramInfo mTVProgramInfo, int timeType12_24) {
        if (mTVProgramInfo == null || mTVProgramInfo.getmTitle() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
        MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>mTVProgramInfo.getmStartTime()=" + mTVProgramInfo.getmStartTime());
        timeFormatBase.set(mTVProgramInfo.getmStartTime());
        String shour = String.valueOf(timeFormatBase.hour);
        int startDay = timeFormatBase.monthDay;
        MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>timeFormatBase.hour=" + timeFormatBase.hour + ",shour=" + shour + ",startDay=" + startDay);
        String smin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.minute < 10) {
            smin = "0" + smin;
        }
        MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>timeFormatBase.minute=" + timeFormatBase.minute + ",smin=" + smin);
        String startTime = shour + ":" + smin;
        String monthDay = String.valueOf(timeFormatBase.monthDay);
        String month = String.valueOf(timeFormatBase.month + 1);
        if ((timeFormatBase.monthDay + 1) < 10) {
            monthDay = "0" + monthDay;
        }
        String dayTime = EPGUtil.getWeek(timeFormatBase.weekDay) + " , " + monthDay + " - " + EPGUtil.getEngMonthSimple(timeFormatBase.month + 1);
        timeFormatBase.set(mTVProgramInfo.getmEndTime());
        int endDay = timeFormatBase.monthDay;
        MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>endDay=" + endDay);
        String ehour = String.valueOf(timeFormatBase.hour);
        String emin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.minute < 10) {
            emin = "0" + emin;
        }
        String endTime = ehour + ":" + emin;
        if (timeType12_24 == 0) {
            startTime = Util.formatTime24_12(startTime);
            endTime = Util.formatTime24_12(endTime);
        }
        sb.append(startTime + " - " + endTime);
        if ((mTVProgramInfo.getmEndTime() - mTVProgramInfo.getmStartTime()) > 24 * 60 * 60) {
            sb.append("(24:00)");
        }
        sb.append("   " + dayTime);
        MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>sb.toString()=" + sb.toString());
        return sb.toString();
    }

    public static String converTimeByLong2Str(long time) {
        MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
        timeFormatBase.set(time);
        String shour = String.valueOf(timeFormatBase.hour);
        String smin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.hour < 10) {
            shour = "0" + shour;
        }
        if (timeFormatBase.minute < 10) {
            smin = "0" + smin;
        }
        String strTime = shour + ":" + smin;
        MtkLog.d(TAG, "converTimeByLong2Str------>strTime=" + strTime);
        return strTime;
    }


    public Long getStartTime(EPGProgramInfo mTVProgramInfo) {
        String dateStr = egpInfoToStr(mTVProgramInfo.getmStartTime());
        return egpDataToDate(dateStr).getTime();
    }

    public Long getEndTime(EPGProgramInfo mTVProgramInfo) {
        String dateStr = egpInfoToStr(mTVProgramInfo.getmEndTime());
        return egpDataToDate(dateStr).getTime();
    }

    public String egpInfoToStr(long epgTime) {
        StringBuilder sb = new StringBuilder();
        MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
        timeFormatBase.set(epgTime);
        String shour = String.valueOf(timeFormatBase.hour);
        String smin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.hour < 10) {
            shour = "0" + shour;
        }
        if (timeFormatBase.minute < 10) {
            smin = "0" + smin;
        }
        String monthDay = String.valueOf(timeFormatBase.monthDay);
        String month = String.valueOf(timeFormatBase.month + 1);
        if (timeFormatBase.monthDay < 10) {
            monthDay = "0" + monthDay;
        }
        if ((timeFormatBase.month + 1) < 10) {
            month = "0" + month;
        }

        int year = timeFormatBase.year;
        MtkLog.d(TAG, "Year:" + year);
        String epgTimeStr = String.format("%d/%s/%s,%s:%s:%d", year, month, monthDay, shour, smin, timeFormatBase.second);
        return epgTimeStr;
    }

    public Date egpDataToDate(String str) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss",
                Locale.getDefault());
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * get hour and minute of date
     *
     * @param date
     * @return
     */
    public String getHourMinite(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",
                EPGUtil.getLocaleLan());
        return formatter.format(date);
    }

    public String getHourMinite(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",
                EPGUtil.getLocaleLan());
        return formatter.format(new Date(time));
    }

}
