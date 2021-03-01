package com.ktc.epg.epgUtil;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.ktc.epg.epg.DataReader;
import com.ktc.epg.epg.EPGProgramInfo;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvHighLevel;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EPGUtil {
    private static final String TAG = "EPGUtil";
    /**
     * svl id value, 1:T, 2:C, 3:General S, 4: Prefer S, 5:T(CI), 6:C(CI), 7:S(CI)
     */
    public static final int DB_AIR_SVLID = 1;
    public static final int DB_CAB_SVLID = 2;
    public static final int DB_SAT_SVLID = 3;
    public static final int DB_SAT_PRF_SVLID = 4;
    public static final int DB_CI_PLUS_SVLID_AIR = 5;
    public static final int DB_CI_PLUS_SVLID_CAB = 6;
    public static final int DB_CI_PLUS_SVLID_SAT = 7;
    /**
     * tuner mode value, 0:T, 1:C, 2:S, 3:used by set general S in wizard and menu
     */
    public static final int DB_AIR_OPTID = 0;
    public static final int DB_CAB_OPTID = 1;
    public static final int DB_SAT_OPTID = 2;
    public static final int DB_GENERAL_SAT_OPTID = 3;// maybe used by set tuner mode, but should not
    // be used by get tuner mode

    public static final int TV_NORMAL_MODE = 0;
    public static final int TV_PIP_MODE = 1;
    public static final int TV_POP_MODE = 2;
    private static final String TV_FOCUS_WIN = MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN;
    private static EPGUtil instance = null;
    public static List<MtkTvBookingBase> books;

    public static EPGUtil getInstance() {
        if (null == instance) {
            instance = new EPGUtil();
        }

        return instance;
    }


    /**
     * Time for top
     *
     * @return
     */
    public static String formatCurrentTimeWith24Hours(boolean is3rdTVSource) {
        String sbString = "";
        MtkTvTimeFormatBase timeFormat = is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() :
                MtkTvTime.getInstance().getBroadcastTime();
        String hour = String.valueOf(timeFormat.hour);
        String min = String.valueOf(timeFormat.minute);
        String sec = String.valueOf(timeFormat.second);
        if (timeFormat.minute < 10) {
            min = "0" + min;
        }
        if (timeFormat.second < 10) {
            sec = "0" + sec;
        }
        sbString = getWeekFull(timeFormat.weekDay) + ",  " +
                timeFormat.monthDay + "-" +
                getEngMonthFull(timeFormat.month + 1) + "-" +
                timeFormat.year + " " +
                hour + ":" + min + ":" + sec;
        return sbString;
    }

    public static String getEngMonthFull(int month) {
        if (month > DataReader.getInstance().getMonthFullArray().length || month < 1) {
            month = 1;
        }
        return DataReader.getInstance().getMonthFullArray()[month - 1];
    }

    public static String getEngMonthSimple(int month) {
        if (month > DataReader.getInstance().getMonthSimpleArray().length) {
            month = 1;
        }
        return DataReader.getInstance().getMonthSimpleArray()[month - 1];
    }

    public static long getCurrentLocalTimeMills() {
        MtkTvTimeFormatBase timeFormat = MtkTvTime.getInstance().getLocalTime();
        return timeFormat.toMillis();
    }

    public static String formatCurrentTimeWith12Hours() {
        return formatCurrentTimeWith12Hours(false);
    }

    /**
     * Time for top
     *
     * @return
     */
    public static String formatCurrentTimeWith12Hours(boolean is3rdTVSource) {
        String sbString = "";
        MtkTvTimeFormatBase timeFormat = is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() :
                MtkTvTime.getInstance().getBroadcastTime();
        String amp_ = "";
        if (timeFormat.hour <= 12) {//"AM"
            amp_ = " " + "AM";
        } else {
            timeFormat.hour = timeFormat.hour - 12;
            amp_ = " " + "PM";
        }
        String hour = String.valueOf(timeFormat.hour);
        String min = String.valueOf(timeFormat.minute);
        String sec = String.valueOf(timeFormat.second);
        if (timeFormat.hour == 0) {
            hour = "12";
//        } else if (timeFormat.hour < 10) {
//            hour="0"+hour;
        }
        if (timeFormat.minute < 10) {
            min = "0" + min;
        }
        if (timeFormat.second < 10) {
            sec = "0" + sec;
        }
        sbString = getWeekFull(timeFormat.weekDay) + ",  " + timeFormat.monthDay + "-" +
                getEngMonthFull(timeFormat.month + 1) + "-" + timeFormat.year + "  " + hour + ":" +
                min + ":" + sec + " " + amp_;
        return sbString;
    }


    /**
     * Time for epg top
     *
     * @return
     */
    public static String[] formatCurrentTimenew(Context mContext) {
        String[] str = new String[3];
        long mtkTtime = MtkTvTime.getInstance().getBroadcastTimeInUtcSeconds();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(mtkTtime * 1000L);
        Log.d(TAG, "formatCurrentTimenew: " + gregorianCalendar.toString());
        str[0] = "";
        if (!DateFormat.is24HourFormat(mContext)) {
            if (gregorianCalendar.get(GregorianCalendar.AM_PM) == Calendar.AM) {
                str[0] = "AM";
            } else {
                str[0] = "PM";
            }
        }
        int hourVale = DateFormat.is24HourFormat(mContext) ? gregorianCalendar.get(Calendar.HOUR_OF_DAY) : gregorianCalendar.get(Calendar.HOUR);
	Log.d("yangt"," str[0] ="+str[0]+"   hourVale = "+hourVale);
	if (str[0].equals("PM") && hourVale == 0){
	    hourVale = 12;
	}
        int minuteValue = gregorianCalendar.get(Calendar.MINUTE);
        String hour = String.valueOf(hourVale);
        String min = String.valueOf(minuteValue);
        if (minuteValue < 10) {
            min = "0" + min;
        }
        if (hourVale < 10) {
            hour = "0" + hour;
        }
        if (hourVale == 0 && DateFormat.is24HourFormat(mContext)) {
            hour = "12";
        }
        str[1] = hour + ":" + min;
        str[2] = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + "." + (gregorianCalendar.get(Calendar.MONTH) + 1) + "." + gregorianCalendar.get(Calendar.YEAR);
        return str;
    }


    public static String getSimpleDate(long date) {
        String sbString = "";//format.format(gCalendar.getTime());
        MtkTvTimeFormatBase timeFormat =
                MtkTvTime.getInstance().getBroadcastTime();
        timeFormat.set(date);
        sbString = timeFormat.monthDay + "/" +
                (timeFormat.month + 1) + "/" +
                timeFormat.year + " ";
        return sbString;
    }

    public static String getWeekFull(int value) {
        if (value > DataReader.getInstance().getWeekFullArray().length - 1) {
            value = 0;
        }
        return DataReader.getInstance().getWeekFullArray()[value];
    }

    public static String getWeek(int value) {
        if (value > DataReader.getInstance().getWeekSimpleArray().length - 1) {
            value = 0;
        }
        return DataReader.getInstance().getWeekSimpleArray()[value];
    }

    public static String formatTimeFor24(long time) {
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        String minString = "";
        String hourString = "";
        if (timeformat.hour < 10) {
            hourString = "0" + timeformat.hour;
        } else {
            hourString = "" + timeformat.hour;
        }
        if (timeformat.minute < 10) {
            minString = "0" + timeformat.minute;
        } else {
            minString = "" + timeformat.minute;
        }
        String startTime = hourString + ":" + minString;
        return startTime;
    }

    /***
     * ' HH:mm AM'
     * @param time
     * @return startTime
     */
    public static String formatStartTime(long time, Context mContext) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(time * 1000L);
        boolean is24Format = EPGUtil.judgeFormatTime12_24(mContext);
        int hour = is24Format ? gregorianCalendar.get(Calendar.HOUR_OF_DAY) : gregorianCalendar.get(Calendar.HOUR);
        int minute = gregorianCalendar.get(Calendar.MINUTE);
        String minString = "";
        String hourString = "";
        String amp_ = "";
        if (!is24Format) {
            if (gregorianCalendar.get(GregorianCalendar.AM_PM) == Calendar.AM) {
                amp_ = " AM";
            } else {
                amp_ = " PM";
            }
        }
        if (minute < 10) {
            minString = "0" + minute;
        } else {
            minString = "" + minute;
        }
        if (hour < 10) {
            hourString = "0" + hour;
        } else {
            hourString = "" + hour;
        }
        if (hour == 0 && !is24Format) {
            hourString = "12";
        }
        String startTime = hourString + ":" + minString + amp_;
//        if (timeformat.hour<=12) {//"AM"
//            startTime = startTime + " " + "AM";
//        }else {
//            startTime = startTime + " " + "PM";
//        }
        return startTime;
    }


    public static String formatTimeFor24(int hour, int minute) {
        String minString = "";
        String hourString = "";
        if (hour < 10) {
            hourString = "0" + hour;
        } else {
            hourString = "" + hour;
        }
        if (minute < 10) {
            minString = "0" + minute;
        } else {
            minString = "" + minute;
        }
        String time = hourString + ":" + minString;
        return time;
    }

    public static String formatTimeFor12(int hour, int minute) {
        String minString = "";
        String hourString = "";
        String amp_ = "";
        if (hour < 12) {//"AM"
            amp_ = " " + "AM";
        } else {
            hour = hour - 12;
            amp_ = " " + "PM";
        }
        if (minute < 10) {
            minString = "0" + minute;
        } else {
            minString = "" + minute;
        }
        if (hour == 0) {
            hourString = "12";
        } else {
            hourString = "" + hour;
        }
        String time = hourString + ":" + minString + amp_;
        return time;
    }


    public static String formatTime(int hour, int minute, Context context) {
        String strTime = EPGUtil.judgeFormatTime12_24(context) ? EPGUtil.formatTimeFor24(hour, minute) : EPGUtil.formatTimeFor12(hour, minute);
        MtkLog.d(TAG, "formatTime>>strTime" + strTime);
        return strTime;
    }


    public static boolean judgeFormatTime12_24(Context context) {
        return DateFormat.is24HourFormat(context);
    }


    /***
     * ' HH:mm AM E,dd-MM'
     * @param
     * @return enfTimeStr
     */
    public static String formatEndTime(long startTime, long duration) {
        long endTime = startTime + duration;
        Date date = new Date(endTime);
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm E,dd-MM", Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        String endTimeStr = format.format(gCalendar.getTime());
        if (gCalendar.get(GregorianCalendar.AM_PM) == 0) {//"AM"
            endTimeStr = endTimeStr + " " + "AM";
        } else {
            endTimeStr = endTimeStr + " " + "PM";
        }
        return endTimeStr;
    }

    /***
     * 'E,dd-MM'
     * @param time
     * @return enfTimeStr
     */
    public static String formatEndTimeDay(long startTime, long duration) {
        long endTime = startTime + duration;
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(endTime);
        MtkLog.d(TAG, "timeformat.month11>>" + timeformat.month + ">>>" + timeformat.monthDay + ">>>" + timeformat.weekDay + ">>>");
        if (timeformat.month < 0 || timeformat.monthDay < 0 || timeformat.weekDay < 0) {
            return "";
        }
        String endTimeStr = getWeek(timeformat.weekDay) + "," + " " + timeformat.monthDay + "-" + getEngMonthSimple(timeformat.month + 1);
        return endTimeStr;
    }

    public static String getWeekDayofTime(long time, long curDay) {//fix CR DTV00584353
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        MtkLog.d(TAG, "getWeekDayofTimetimeformat.toMillis()222>>" + time + "   " + timeformat.toMillis() + "  " + curDay);
        if (timeformat.month < 0 || timeformat.monthDay < 0 || timeformat.weekDay < 0) {
            return "";
        }
        time = time - timeformat.hour * 3600 - timeformat.minute * 60 - timeformat.second;
        timeformat.set(time);
        if (timeformat.toMillis() - curDay == 0) {
            return "Today";
        } else if (timeformat.toMillis() - curDay == 24 * 60 * 60) {
            return "Tomorrow";
        } else if (curDay - timeformat.toMillis() == 24 * 60 * 60) {
            return "Yesterday";
        }
        return getWeek(timeformat.weekDay);
    }

    public static int getDayOffset(long time) {
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        time = time - timeformat.hour * 3600 - timeformat.minute * 60 - timeformat.second;
        timeformat.set(time);
        long curDay = getCurrentDayStartTime();
        MtkLog.d(TAG, "getDayOffsettimeformat.toMillis()>>" + time + "   " + timeformat.toMillis() + "  " + curDay + "  " + ((timeformat.toMillis() - curDay) / (24 * 60 * 60)));
        return (int) ((timeformat.toMillis() - curDay) / (24 * 60 * 60));
    }

    public static String getTodayOrTomorrow(long startTime, long curTime) {
        MtkLog.e(TAG, "getTodayOrTomorrow:starttime:" + startTime + "==>curtime:" + curTime);
        MtkLog.e(TAG, "getTodayOrTomorrow:value:" + (startTime - curTime));
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(startTime);
        int startDay = timeformat.monthDay;
        timeformat.set(curTime);
        int curDay = timeformat.monthDay;
        int dvalue = curDay - startDay;
        MtkLog.e(TAG, "getTodayOrTomorrow:day:" + dvalue);
        String dayString = "Toady";
        if (dvalue == 0) {
            dayString = "Toady";
        } else if (dvalue < 0) {
            dayString = "Tomorrow";
        } else if (dvalue > 0) {
            dayString = "Yesterday";
        }
        return dayString;
    }

    public static long getCurrentTime() {
        return getCurrentTime(false);
    }

    public static long getCurrentTime(boolean is3rdTVSource) {
        MtkTvTimeFormatBase tvTime = is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() :
                MtkTvTime.getInstance().getBroadcastTime();
        return is3rdTVSource ? tvTime.toMillis() : tvTime.toMillis();
    }

    public static long getCurrentDayStartTime() {
        return getCurrentDayStartTime(false);
    }

    public static long getCurrentDayStartTime(boolean is3rdTVSource) {
        MtkTvTimeFormatBase tvTime = is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() :
                MtkTvTime.getInstance().getBroadcastTime();
        MtkLog.d(TAG, "time in year:" + tvTime.hour);
        MtkLog.d(TAG, "time in year:" + tvTime.minute);
        MtkLog.d(TAG, "time in year:" + tvTime.second);
        long todayStart = 0;
        todayStart = tvTime.toMillis() - tvTime.hour * 3600 - tvTime.minute * 60 - tvTime.second;
        return todayStart;
    }

    public static int getCurrentHour() {
        return getCurrentHour(false);
    }
	
    public static boolean isIRANCountry() {
        return MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_IRN);
    }

    public static int getCurrentHour(boolean is3rdTVSource) {
        long tvTime = MtkTvTime.getInstance().getBroadcastTimeInUtcSeconds();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(tvTime * 1000L);
        return gregorianCalendar.get(Calendar.HOUR_OF_DAY);
    }

    public static long getCurrentDateDayAsMills() {
        return getCurrentDateDayAsMills(false);
    }

    /*
     * get current date day as Second
     */
    public static long getCurrentDateDayAsMills(boolean is3rdTVSource) {
        long tvTime = MtkTvTime.getInstance().getBroadcastTimeInUtcSeconds();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(tvTime * 1000L);
        return tvTime - gregorianCalendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 - gregorianCalendar.get(Calendar.MINUTE) * 60 - gregorianCalendar.get(Calendar.SECOND);
    }

    /*
     * get epg last time
     */
    public static long getEpgLastTimeMills(int dayNum, int startHour, boolean withHour, boolean is3rdTVSource) {
        MtkTvTimeFormatBase mtkTvTimeFormatBase = is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() :
                MtkTvTime.getInstance().getBroadcastTime();
        long curTimeMillSeconds = mtkTvTimeFormatBase.toMillis();
        if (!withHour) {
            curTimeMillSeconds = curTimeMillSeconds - mtkTvTimeFormatBase.hour * 60 * 60 - mtkTvTimeFormatBase.minute * 60 - mtkTvTimeFormatBase.second;
        }
        return (curTimeMillSeconds + (dayNum * 24 + startHour) * 60 * 60);
    }

    public static Locale getLocaleLan() {
//        if (sv.readValue(MenuConfigManager.OSD_LANGUAGE) == 1) {
//            return Locale.CHINA;
//        } else {
        return Locale.US;
//        }
    }

    public static long convertTime(int type, long time) {
        MtkTvTimeFormatBase from = new MtkTvTimeFormatBase();
        MtkTvTimeFormatBase to = new MtkTvTimeFormatBase();
        from.setByUtc(time);
        MtkTvTimeBase newTime = new MtkTvTimeBase();
        newTime.convertTime(type,
                from, to);
        time = to.toSeconds();
        return time;
    }

    public static int[] getBookingId(EPGProgramInfo epgProgramInfo) {
        int[] result = new int[]{-1, -1};
        books = MtkTvRecord.getInstance().getBookingList();
        Log.d(TAG, "getBookingId: " + (books.size()));
        if (books != null) {
            for (MtkTvBookingBase booking : books) {
                Log.d(TAG, "convertTime11: book "+booking.getRecordStartTime());
                long startTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_SYS_UTC_TO_BRDCST_LOCAL, booking.getRecordStartTime()) + 10;
//               long eventStartTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_SYS_UTC_TO_BRDCST_LOCAL, Util.formatTime2Local(epgProgramInfo.getmStartTime())) ;
//                long eventEndTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_SYS_UTC_TO_BRDCST_LOCAL, Util.formatTime2Local(epgProgramInfo.getmEndTime()));
                long eventStartTime =  Util.formatTime2Local(epgProgramInfo.getmStartTime()) ;
                long eventEndTime = Util.formatTime2Local(epgProgramInfo.getmEndTime());
                Log.d(TAG, "getBookingId: 1111"+eventStartTime +"   "+eventEndTime);
                Log.d(TAG, "getBookingId: " + booking.toString() + " " + epgProgramInfo.getProgramId() + " " + Util.formatTime2Local(epgProgramInfo.getmStartTime()) + "  " + startTime);
                if (booking.getChannelId() == epgProgramInfo.getChannelId()
                        && startTime >= eventStartTime
                        &&startTime<eventEndTime
                ) {
                    result[0] = booking.getBookingId();
                    result[1] = booking.getRecordMode();
                    return result;
                }
            }
            return result;
        }
        return result;

    }


    public static boolean timeExist(Long startTime, Long endTime) {
        if (books != null) {
            startTime = startTime+10;
            endTime = endTime-10;
            for (MtkTvBookingBase booking : books) {
                Log.d(TAG, "timeExist: " + startTime + "  " + booking.getRecordStartTime() + "  " + booking.getRecordDuration());
                if ((startTime >= booking.getRecordStartTime()
                        && startTime < booking.getRecordStartTime() + booking.getRecordDuration())
                        || (endTime > booking.getRecordStartTime()
                        && endTime <= booking.getRecordStartTime() + booking.getRecordDuration())
                        ||(startTime<=booking.getRecordStartTime()
                        &&endTime>=booking.getRecordStartTime() + booking.getRecordDuration())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getAvailableString(String illegalString) {
        String resultString = "";
        if (null != illegalString && !("").equals(illegalString)) {
            byte[] illegalByte = illegalString.getBytes();
            int j = 0;
            byte[] availableByte = new byte[illegalByte.length];
            for (byte mByte : illegalByte) {
                if (((mByte & 0xff) >= 32 && (mByte & 0xff) != 127)
                        || ((mByte & 0xff) == 10) || ((mByte & 0xff) == 13)) {
                    availableByte[j] = mByte;
                    j++;
                }
            }
            if (((availableByte[availableByte.length - 1] & 0xff) == 10)
                    || ((availableByte[availableByte.length - 1] & 0xff) == 13)) {
                j--;
            }
            if (null != availableByte) {
                resultString = new String(availableByte, 0, j);
            }
        }
        return resultString;
    }

    public int getAllEPGChannelLength() {
        return getAllEPGChannelLength(false);
    }

    /*
     * only for epg channel to show.
     */
    public int getAllEPGChannelLength(boolean showSkip) {
        int len = 0;
        len = MtkTvChannelList.getInstance().getChannelCountByFilter(getSvl(), getChListFilterEPG());
        MtkLog.d(TAG, "getchannellength getAllEPGChannelLength len = " + len);
        return len;
    }

    /**
     * same with getSvl() function svlID 1:air, 2:cable, 3:general sat, 4:prefer sat, 5:CAM-air,
     * 6:CAM-cable, 7:CAM-sat
     *
     * @return
     */
    public int getSvlFromACFG() {
        int svlId = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_SVL_ID);
        MtkLog.d(TAG, "getSvlFromACFG>>>>" + svlId);
        return svlId;
    }

    /**
     * same with getSvlFromACFG() function svlID 1:air, 2:cable, 3:general sat, 4:prefer sat,
     * 5:CAM-air, 6:CAM-cable, 7:CAM-sat
     *
     * @return
     */
    public int getSvl() {

        if (true) {
            return getSvlFromACFG();
        }
        int svl = -1;
        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_CN) {
            return getSvlFromACFG();
        }
        int tunerMode;
        if (isdualtunermode()) {
            tunerMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_MISC_2ND_TUNER_TYPE);
            MtkLog.d(TAG, "getSvl>>>>" + tunerMode);
        } else {
            tunerMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC);
        }
        boolean flag =
                MtkTvConfig.getInstance().isConfigVisible(MtkTvConfigType.CFG_MISC_CH_LST_TYPE)
                        == MtkTvConfigType.CFGR_VISIBLE;
        boolean hasCAM = false;
        if (flag) {
            int value = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_CH_LST_TYPE);
            if (value > 0) {
                hasCAM = true;
            }
            MtkLog.d(TAG, "getSvl>>>>" + value);
        }
        switch (tunerMode) {
            case DB_AIR_OPTID:// T
                if (hasCAM) {
                    svl = DB_CI_PLUS_SVLID_AIR;
                } else {
                    svl = DB_AIR_SVLID;
                }
                break;
            case DB_CAB_OPTID:// C
                if (hasCAM) {
                    svl = DB_CI_PLUS_SVLID_CAB;
                } else {
                    svl = DB_CAB_SVLID;
                }
                break;
            case DB_SAT_OPTID:// S
            case DB_GENERAL_SAT_OPTID:// maybe has no this value when get tuner mode
                if (hasCAM) {
                    svl = DB_CI_PLUS_SVLID_SAT;
                } else {
                    int prefer;

                    if (isdualtunermode()) {
                        prefer = MtkTvConfig.getInstance().getConfigValue(
                                MtkTvConfigTypeBase.CFG_MISC_2ND_PREFER_CH_LIST);
                    } else {
                        prefer = MtkTvConfig.getInstance().getConfigValue(
                                MtkTvConfigTypeBase.CFG_TWO_SAT_CHLIST_PREFERRED_SAT);
                    }

                    MtkLog.d(TAG, "getSvl tunerMode,prefer =" + prefer);
                    if (prefer != 0) {
                        svl = DB_SAT_PRF_SVLID;
                    } else {
                        svl = DB_SAT_SVLID;
                    }
                }
                break;
            default:// default is T
                if (hasCAM) {
                    svl = DB_CI_PLUS_SVLID_AIR;
                } else {
                    svl = DB_AIR_SVLID;
                }
                break;
        }
        MtkLog.d(TAG, "getSvl tunerMode =" + tunerMode + " svl =" + svl);
        return svl;
    }


    public boolean isdualtunermode() {
        return getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT);
    }


    /**
     * get current focus output, has "main" or "sub"
     *
     * @return
     */
    public String getCurrentFocus() {
        String focusWin = "";
        /*
         * if (mCurrentTvMode != TV_NORMAL_MODE){ mCurrentTvMode =
         * instanceMtkTvHighLevel.getCurrentTvMode(); }
         */
        MtkTvHighLevel instanceMtkTvHighLevel = new MtkTvHighLevel();
        MtkLog.d(TAG, "getCurrentFocus(), mCurrentTvMode =" + instanceMtkTvHighLevel.getCurrentTvMode());
        if (instanceMtkTvHighLevel.getCurrentTvMode() == TV_NORMAL_MODE) {
            focusWin = MtkTvInputSource.INPUT_OUTPUT_MAIN;
        } else {
            int result = MtkTvConfig.getInstance().getConfigValue(TV_FOCUS_WIN);
            MtkLog.d(TAG, "getCurrentFocus(), TV_FOCUS_WIN =" + result);
            if (0 == result) {
                focusWin = MtkTvInputSource.INPUT_OUTPUT_MAIN;
            } else if (1 == result) {
                focusWin = MtkTvInputSource.INPUT_OUTPUT_SUB;
            }
        }
        Log.d(TAG, "come in getCurrentFocus focusWin =" + focusWin);
        return focusWin;
    }

    public int getChListFilterEPG() {
        int filter = 0;
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_CN:
            case MarketRegionInfo.REGION_EU:
            case MarketRegionInfo.REGION_SA:
                filter = MtkTvChCommonBase.SB_VNET_ACTIVE
                        | MtkTvChCommonBase.SB_VNET_FAKE
                        |MtkTvChCommonBase.SB_VOPT_DELETED_BY_USER
                        |MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
                // |MtkTvChCommonBase.SB_VNET_VISIBLE;
                break;
            case MarketRegionInfo.REGION_US:
                filter = MtkTvChCommonBase.SB_VNET_EPG | MtkTvChCommonBase.SB_VNET_FAKE;
                // |MtkTvChCommonBase.SB_VNET_VISIBLE;
                break;
        }
        MtkLog.d(TAG, "getChListFilter filter =" + filter);
        return filter;
    }

    /*
     * chlist filter default :MtkTvChannelList.CHLST_FLT_ACTIVE | MtkTvChannelList.CHLST_FLT_VISIBLE
     * chId: ch cursor prevCount: prev chId channel count,except chId itself. nextCount: next chId
     * channel count, if chId exist contain itself ,or not contain. note: default filter fake channel;
     */
    public List<MtkTvChannelInfoBase> getChannelList(int chId, int prevCount, int nextCount,
                                                     int filter) {
        MtkLog.d(TAG, "getChannelList chId =" + chId + "pre =" + prevCount + "nextCOunt ="
                + nextCount + "filter =" + filter);
        List<MtkTvChannelInfoBase> chList = MtkTvChannelList.getInstance().getChannelListByFilter(getSvl(),
                filter, chId, prevCount, nextCount);
        // fakefilter(chId,prevCount,nextCount);
        /*
         * new ArrayList<MtkTvChannelInfoBase>(); mtkTvChList.getChannelListByFilter(getSvl(),
         * getDefFilter(),chId,prevCount, nextCount);
         */
        if (chList != null && chList.size() > 0) {
            MtkLog.d(TAG, "getChannelList chId = " + chId + "chList.get(0).getchId"
                    + chList.get(0).getChannelId());
        } else {
            MtkLog.d(TAG, "getChannelList chId = " + chId + "chList == null,or size == 0");
        }

        return chList;
    }


}
