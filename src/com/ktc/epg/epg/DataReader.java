package com.ktc.epg.epg;

import android.content.Context;
import android.util.Log;

import com.ktc.epg.DestroyApp;
import com.ktc.epg.R;
import com.ktc.epg.epgUtil.EPGUtil;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvEvent;
import com.mediatek.twoworlds.tv.MtkTvTimeFormat;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class DataReader {
    private static final String TAG = "DataReader";
    public static final int PER_PAGE_CHANNEL_NUMBER = 6;
    private static DataReader dtReader;
    private List<MtkTvChannelInfoBase> tvChannelList;
    private EPGTimeConvert tmCvt;
    private Context mContext;
    private String[][] sType;
    private String[] mType;
    private MtkTvEvent tvEvent;
    private String[] mMonthFull, mMonthSimple, mWeekFull, mWeekSimple;
    private EPGUtil mEPGUtil;

    private DataReader(Context context) {
        mContext = context;
        tmCvt = EPGTimeConvert.getInstance();
        tvEvent = MtkTvEvent.getInstance();
        mEPGUtil = EPGUtil.getInstance();
    }


    public static synchronized DataReader getInstance(Context context) {
        if (dtReader == null) {
            dtReader = new DataReader(context);
        }
        return dtReader;
    }

    public static DataReader getInstance() {
        if (dtReader == null) {
            dtReader = new DataReader(DestroyApp.appContext);
        }
        return dtReader;
    }


    /**
     * @param chList    channel
     * @param dayNum    [0-7]
     * @param startTime the duration start time
     * @param mTimeSpan the duration time
     */
    public void readChannelProgramInfoByTime(EPGChannelInfo chList, int dayNum, int startTime, int mTimeSpan) {
        long sTime = dayNum == 0 ? EPGUtil.getCurrentTime() : tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startTime);
        long duration = tmCvt.getHourtoMsec(mTimeSpan);
        MtkLog.d(TAG, "set start time : [" + EPGUtil.getCurrentTime() + "]duration>>" + duration);
        MtkLog.d(TAG, "set start time : [dayNum:" + dayNum + "]" + "[startTime:" + sTime + "]");
        List<EPGProgramInfo> mTVProgramInfo = new ArrayList<EPGProgramInfo>();
        mTVProgramInfo = readChannelProgramInfoByTime(chList.getTVChannel(), sTime, duration);
        chList.setmTVProgramInfoList(mTVProgramInfo);
    }

    /***
     * connect to TVAPI MtkTvEvent.java
     * List<MtkTvEventInfo>  MtkTvGetEventListByChannelId(int channelId, long startTime, long duration)
     * @param chinfo
     * @param startTime
     * @param duration
     * @return
     */
    public List<EPGProgramInfo> readChannelProgramInfoByTime(MtkTvChannelInfoBase chinfo,
                                                             long startTime, long duration) {
        List<EPGProgramInfo> mTVProgramInfoList = new ArrayList<EPGProgramInfo>();
//		List<MtkTvEventInfo> mTVEventList = new ArrayList<MtkTvEventInfo>();
//		evMonitor.setTime(startTime, duration);
        MtkLog.d(TAG, "--- set time : [" + startTime + "," + duration + "]");
//		evMonitor.syncRead();
        final MtkTvChannelInfoBase tvChannel = chinfo;
        List<MtkTvEventInfoBase> mTVEventList = null;
	GregorianCalendar gregorianCalendar = new GregorianCalendar();
	Long offset = 0L;
	if(EPGUtil.isIRANCountry()){
	offset = (gregorianCalendar.get(Calendar.ZONE_OFFSET) + gregorianCalendar.get(Calendar.DST_OFFSET)) / 1000L; }
        try {
            tvEvent.setMaxEventNum(32);
            mTVEventList = tvEvent.getEventListByChannelId(tvChannel.getChannelId(), startTime+offset, duration);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (mTVEventList != null) {
            MtkLog.d(TAG, "---mTVEventList get :" + mTVEventList.size());
            for (int i = 0; i < mTVEventList.size(); i++) {
                long proStartTime = mTVEventList.get(i).getStartTime();
				if (EPGUtil.isIRANCountry()) {
                    proStartTime = proStartTime - offset;
                }
                long proEndTime = proStartTime + mTVEventList.get(i).getDuration();
//                MtkTvTimeFormat mtkTvTimeFormat = MtkTvTimeFormat.getInstance();
//                mtkTvTimeFormat.set(startTime);
                gregorianCalendar.setTimeInMillis(startTime*1000L);
                Log.d(TAG, "readChannelProgramInfoByTime: "+gregorianCalendar.toString());
                int startDay = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                gregorianCalendar.setTimeInMillis(proStartTime*1000L);
                int proStartDay = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                Log.d(TAG, "readChannelProgramInfoByTime: " + startDay + "   " + proStartDay);
                gregorianCalendar.setTimeInMillis(proEndTime*1000L);
                int proEndDay = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                if (proStartDay > startDay
                        ||proEndTime<=startTime
                        ||(startDay ==proEndDay &&proStartDay<proEndDay )) {
                    continue;
                }
//				Date mStart = new Date(proStartTime);
//				Date mEnd = new Date(proEndTime);
                MtkLog.d(TAG, "+++++++ event guidance ++++++++++++" + mTVEventList.get(i).getChannelId() + "   " + mTVEventList.get(i).getEventId() + "   " + mTVEventList.get(i).getGuidanceText() + "    " + mTVEventList.get(i).getDuration());
                MtkLog.d(TAG, "+++++++ event name ++++++++++++" + mTVEventList.get(i).getEventTitle() + "   " + proStartTime + "   " + proEndTime);
                MtkLog.d(TAG, "+++++++ event detail ++++++++++++" + mTVEventList.get(i).getEventDetail() + "     >>" + mTVEventList.get(i).getEventRating()
                        + "  extend detail:" + mTVEventList.get(i).getEventDetailExtend());
                int[] cat = mTVEventList.get(i).getEventCategory();
                String title = mTVEventList.get(i).getEventTitle();
                String guidence = mTVEventList.get(i).getGuidanceText();
                String detail = mTVEventList.get(i).getEventDetail();
                String extendDetail = mTVEventList.get(i).getEventDetailExtend();
                String resultDetail = getResultDetail(guidence, detail, extendDetail);
                title = mEPGUtil.getAvailableString(title);
                resultDetail = mEPGUtil.getAvailableString(resultDetail);
                EPGProgramInfo mTVprogramInfo = new EPGProgramInfo(proStartTime,
                        proEndTime, (title == null || title.equals("")) ? mContext.getResources().getString(R.string.nav_epg_no_title) : title, resultDetail, 0,
                        1, true);
                mTVprogramInfo.setHasSubTitle(mTVEventList.get(i).isCaption());
                mTVprogramInfo.setCategoryType(cat);
                mTVprogramInfo.setChannelId(tvChannel.getChannelId());
                mTVprogramInfo.setProgramId(mTVEventList.get(i).getEventId());
                mTVprogramInfo.setProgramBlock(tvEvent.checkEventBlock(tvChannel.getChannelId(), mTVEventList.get(i).getEventId()));
                MtkLog.d(TAG, "++++programblock++" + tvEvent.checkEventBlock(tvChannel.getChannelId(), mTVEventList.get(i).getEventId())
                        + "   " + mTVEventList.get(i).isCaption());
                setMStype(mTVprogramInfo, cat);
                mTVprogramInfo.setRatingType(mapRating2CustomerStr(mTVEventList.get(i).getEventRatingType()));
                mTVprogramInfo.setRatingValue(mTVEventList.get(i).getEventRatingType());
                mTVProgramInfoList.add(mTVprogramInfo);
            }

            if (mTVEventList.size() == 0) {

            }
        }
//        } else {
//            EPGProgramInfo mTVprogramInfo = new EPGProgramInfo(startTime, (startTime + duration), null, null, 0,
//                    1, true);
//            mTVProgramInfoList.add(mTVprogramInfo);
//        }

        return mTVProgramInfoList;
    }

    private int[] contentThaAgeHint = {5, 6, 12, 13, 18, 21};

    private String mapRating2CustomerStr(int ratingValue) {
        Log.d(TAG, "ratingValue=" + ratingValue);
        if (ratingValue <= 0) {
            return null;
        }
        if (isAusCountry()) {
            int value = getAusRating(ratingValue);
            Log.d(TAG, "mapRating2CustomerStr--->rating Aus==" + getRatingArray()[value]);
            return getRatingArray()[value];
        } else if (isTHACountry()) {
            for (int i = contentThaAgeHint.length - 1; i >= 0; i--) {
                if (contentThaAgeHint[i] <= ratingValue) {
                    Log.d(TAG, "mapRating2CustomerStr--->rating THA==" + getRatingArray()[i + 1]);
                    return getRatingArray()[i + 1];
                }
            }
        } else {
            if (ratingValue <= 4) {
                ratingValue = 4;
            }
            return String.valueOf(ratingValue);
        }
        return null;
    }

    private boolean isAusCountry() {
        String country = MtkTvConfig.getInstance().getCountry();
        if (country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUS)) {
            return true;
        }
        return false;
    }


    //is Thailand
    private boolean isTHACountry() {
        String country = MtkTvConfig.getInstance().getCountry();
        MtkLog.d(TAG, "country:" + country);
        if (country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_THA)) {
            return true;
        }
        return false;
    }

    private int getAusRating(int value) {
        if (value < 1) {
            value = 0;
        } else if (value <= 1) {
            value = 7;
        } else if (value <= 9) {
            value = 6;
        } else if (value <= 11) {
            value = 5;
        } else if (value <= 13) {
            value = 4;
        } else if (value <= 15) {
            value = 3;
        } else if (value <= 17) {
            value = 2;
        } else if (value > 17) {
            value = 1;
        }
        return value;
    }

    private String[] getRatingArray() {
        String[] mRatingArray;
        if (isAusCountry()) {
            mRatingArray = mContext.getResources().getStringArray(R.array.array_level_au_lock);

        } else if (isTHACountry()) {
            mRatingArray = mContext.getResources().getStringArray(R.array.array_level_th_lock);
        } else {
            mRatingArray = mContext.getResources().getStringArray(R.array.array_level_dvb_lock);
        }
        return mRatingArray;
    }


    public String getResultDetail(String guidence, String detail, String extendDetail) {
        MtkLog.d(TAG, "guidence>>" + guidence + ">>");
        MtkLog.d(TAG, "guidence222>>" + detail + ">>" + extendDetail + ">>>");
        String resultDetail = "";
        if (guidence == null) {
            guidence = "";
        }
        if (detail == null) {
            detail = "";
        }
        if (extendDetail == null) {
            extendDetail = "";
        }
        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
            if (!guidence.equals("")) {
                resultDetail = guidence;
            }
            if (resultDetail.equals("")) {
                resultDetail = detail;
            } else if (!detail.equals("")) {
                resultDetail = resultDetail + "\n" + detail;
            }
        } else {
            if (!guidence.equals("")) {
                resultDetail = guidence;
            }
            if (resultDetail.equals("")) {
                resultDetail = detail;
            } else if (!detail.equals("")) {
                resultDetail = resultDetail + "\n" + detail;
            }
            if (resultDetail.equals("")) {
                resultDetail = extendDetail;
            } else if (!extendDetail.equals("")) {
                resultDetail = resultDetail + "\n" + extendDetail;
            }
        }
        return resultDetail;
    }


    public float getProWidth(EPGProgramInfo epgProInfo, long startTime, long duration) {
        float width = 0.0f;
        long proStartTime = epgProInfo.getmStartTime();
        long proEndTime = epgProInfo.getmEndTime();
        if (proStartTime < startTime && proEndTime > (startTime + duration)) {
            epgProInfo.setDrawLeftIcon(true);
            epgProInfo.setDrawRightIcon(true);
            width = 1.0f;
            MtkLog.d(TAG, "setAdpter-----layoutParams.1---------------->");
        } else if (proStartTime < startTime) {
            width = EPGTimeConvert.countShowWidth(proEndTime, startTime);
            epgProInfo.setDrawLeftIcon(true);
            MtkLog.d(TAG, "setAdpter-----layoutParams.2---------------->");
        } else if (proEndTime > (startTime + duration)) {
            width = EPGTimeConvert.countShowWidth(startTime + duration, proStartTime);
            epgProInfo.setDrawRightIcon(true);
            MtkLog.d(TAG, "setAdpter-----layoutParams.3---------------->");
        } else {
            width = tmCvt.countShowWidth(proEndTime - proStartTime);
            MtkLog.d(TAG, "setAdpter-----layoutParams.4---------------->");
        }
        MtkLog.d(TAG, " program width: " + "proEndTime:" + proEndTime + "_proEndTime:" + proEndTime
                + "_startTime:" + startTime + "_width:" + width);
        return width;
    }


    public float getProLeftMargin(EPGProgramInfo mTVprogramInfo,
                                  EPGProgramInfo preTvEvent, EPGProgramInfo currentTvEvent) {
        float leftMargin = 0.0f;
        long startTime = preTvEvent.getmEndTime();
        long endTime = currentTvEvent.getmStartTime();

        leftMargin = EPGTimeConvert.countShowWidth(endTime, startTime);
        return leftMargin;
    }

    public List<EPGProgramInfo> getChannelProgramList(MtkTvChannelInfoBase channel, int dayNum, int startTime, int mTimeSpan) {
        if (channel != null) {
            long sTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startTime);
            long duration = tmCvt.getHourtoMsec(mTimeSpan);
            List<EPGProgramInfo> mTVProgramInfo = new ArrayList<EPGProgramInfo>();
            mTVProgramInfo = readChannelProgramInfoByTime(channel, sTime, duration);
            return mTVProgramInfo;
        }
        return null;
    }

    /********************************************************
     *
     * Channel function
     *
     **********************************************************/

    /**
     * getallChannelList by type of EPGChannelInfo used for the UI
     *
     * @return
     */
    private List<EPGChannelInfo> mChannelList = new ArrayList<EPGChannelInfo>();

    public List<EPGChannelInfo> getAllChannelList(boolean filterAnalog) {
        EPGChannelInfo chInfo = null;
        mChannelList.clear();
        tvChannelList = mEPGUtil.getChannelList(MtkTvChannelList.getCurrentChannelId(), 0, 8, mEPGUtil.getChListFilterEPG());
        MtkLog.e(TAG, "epg get all channel list size:" + tvChannelList.size());
        if (null != tvChannelList) {
            for (int i = 0; i < tvChannelList.size(); i++) {
                MtkTvChannelInfoBase mtkTvChannelInfoBase = tvChannelList.get(i);
                if ((filterAnalog && mtkTvChannelInfoBase != null)
                        &&(mtkTvChannelInfoBase.isAnalogService() || mtkTvChannelInfoBase.isUserDelete()) ) {
                    continue;
                }
                if (mtkTvChannelInfoBase != null
                        && ((mtkTvChannelInfoBase.getChannelId() <MtkTvChannelList.getCurrentChannelId() ))
                ) {
                    continue;
                }
                chInfo = new EPGChannelInfo(tvChannelList.get(i));
                mChannelList.add(chInfo);
            }
        }
        return mChannelList;
    }


    public List<EPGChannelInfo> getAppendChannelList(int currentChannelId, boolean isNext) {
        EPGChannelInfo chInfo = null;
        int prevCount = isNext ? 0 : 1;
        int nextCount = isNext ? 2 : 0;
        int length = mEPGUtil.getAllEPGChannelLength();
        List<EPGChannelInfo> epgChannelInfos = new ArrayList<>();
        tvChannelList = mEPGUtil.getChannelList(currentChannelId, prevCount, nextCount, mEPGUtil.getChListFilterEPG());
        MtkLog.e(TAG, "epg get all channel list size:" + tvChannelList.size() + "   " + tvChannelList.get(0).toString() + " 12323  " + currentChannelId);
        if (null != tvChannelList) {
            for (int i = 0; i < tvChannelList.size(); i++) {
                MtkTvChannelInfoBase mtkTvChannelInfoBase = tvChannelList.get(i);
                if (mtkTvChannelInfoBase != null
                        && ((mtkTvChannelInfoBase.getChannelId() <= currentChannelId && isNext)
                        || (mtkTvChannelInfoBase.getChannelId() >= currentChannelId && !isNext))
                ) {
                    continue;
                }
                if (mtkTvChannelInfoBase != null
                        && (mtkTvChannelInfoBase.isAnalogService()
                        || mtkTvChannelInfoBase.isUserDelete())){
                    return getAppendChannelList(mtkTvChannelInfoBase.getChannelId(),isNext);
                }
                chInfo = new EPGChannelInfo(tvChannelList.get(i));
                epgChannelInfos.add(chInfo);
            }
        }
        return epgChannelInfos;
    }


    /**
     * set program mainType and subType
     *
     * @param program
     * @param categoryType
     */
    public void setMStype(EPGProgramInfo program, int[] categoryType) {
        int index = categoryType[0];
        MtkLog.d(TAG, "subIndex>>>" + index + "   " + program.getmTitle());
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_EU:
                if (index == 0) {
                    program.setMainType(-1);
                    program.setSubType(-1);
                } else {
                    int mainIndex = (index & 0xf0) >> 4;
                    if (mainIndex > 0) {
                        String country = MtkTvConfig.getInstance().getCountry();
                        if (country != null && country.equals("GBR")) {
                            switch (mainIndex) {
                                case 7:
                                case 8:
                                    mainIndex = 2;
                                    break;
                                case 6:
                                case 9:
                                case 10:
                                    mainIndex = mainIndex - 3;
                                    break;
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                    mainIndex = 9;
                                    break;
                                case 15:
                                    mainIndex = 8;
                                    break;
                                default:
                                    break;
                            }
                            program.setMainType(mainIndex);
                            program.setSubType(-1);
                        } else if (country != null && country.equals("AUS")) {
                            MtkLog.d(TAG, "AUS subIndex>>>" + mainIndex);
                            program.setMainType(mainIndex);
                            program.setSubType(-1);
                        } else {
                            if (mainIndex > 0 && mainIndex < mType.length) {
                                program.setMainType(mainIndex);
                                int subIndex = (index & 0x0f);
                                MtkLog.d(TAG, "subIndex>>>" + mainIndex + "   " + subIndex);
                                if (sType[mainIndex] != null && subIndex >= 0 && subIndex < sType[mainIndex].length) {
                                    program.setSubType(subIndex);
                                } else {
                                    program.setSubType(-1);
                                }
                            } else {
                                program.setMainType(-1);
                                program.setSubType(-1);
                            }
                        }
                    } else {
                        program.setMainType(-1);
                        program.setSubType(-1);
                    }
                }
                break;
            default:
                if (index == 0) {
                    program.setMainType(-1);
                    program.setSubType(-1);
                } else {
                    int mainIndex = (index & 0xf0) >> 4;
                    if (mainIndex >= 0 && mainIndex < mType.length) { //
                        program.setMainType(mainIndex);
                        int subIndex = (index & 0xf0) << 4;
                        if (subIndex >= 0 && subIndex < sType[mainIndex].length) {
                            program.setSubType(subIndex);
                        } else {
                            program.setSubType(-1);
                        }
                    } else {
                        program.setMainType(-1);
                        program.setSubType(-1);
                    }
                }
                break;
        }
    }

    public String[] getMainType() {
        return mType;
    }

    public String[][] getSubType() {
        return sType;
    }

    public void loadProgramType() {
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_EU:
                sType = new String[11][];
                String country = MtkTvConfig.getInstance().getCountry();
                if (country != null && country.equals("GBR")) {
                    mType = mContext.getResources().getStringArray(R.array.nav_epg_filter_type_UK);
                } else if (country != null && country.equals("AUS")) {
                    sType = new String[14][];
                    mType = mContext.getResources().getStringArray(R.array.nav_epg_filter_type_AUS);
                } else {
                    mType = mContext.getResources().getStringArray(
                            R.array.nav_epg_filter_type);
                    sType[0] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_movie);
                    sType[1] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_news);
                    sType[2] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_show);
                    sType[3] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_sports);
                    sType[4] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_children);
                    sType[5] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_music);
                    sType[6] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_arts);
                    sType[7] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_social);
                    sType[8] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_education);
                    sType[9] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_leisure);
                    sType[10] = mContext.getResources().getStringArray(
                            R.array.nav_epg_subtype_special);
                }
                break;
            case MarketRegionInfo.REGION_SA:
                sType = new String[16][];
                mType = mContext.getResources().getStringArray(
                        R.array.nav_epg_filter_sa_type);
                sType[0] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_news);
                sType[1] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_sports);
                sType[2] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_education);
                sType[3] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_soap_opera);
                sType[4] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_mini_series);
                sType[5] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_series);
                sType[6] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_variety);
                sType[7] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_reality_show);
                sType[8] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_information);
                sType[9] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_comical);
                sType[10] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_children);
                sType[11] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_erotic);
                sType[12] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_movie);
                sType[13] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_RA_TE_SA_PR);
                sType[14] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_DEBATE_INTERVIEW);
                sType[15] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sa_other);
                break;
            case MarketRegionInfo.REGION_CN:
            default:
                sType = new String[11][];
                mType = mContext.getResources().getStringArray(
                        R.array.nav_epg_filter_type_cn);
                sType[0] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_movie_cn);
                sType[1] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_news_cn);
                sType[2] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_show_cn);
                sType[3] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_sports_cn);
                sType[4] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_children_cn);
                sType[5] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_music_cn);
                sType[6] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_arts_cn);
                sType[7] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_social_cn);
                sType[8] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_education_cn);
                sType[9] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_leisure_cn);
                sType[10] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_special_cn);
                break;
        }
    }

    public void cleanMStypeDB() {
        if (mType == null) {
            loadProgramType();
        }
        for (int i = 0; i < mType.length; i++) {
            if (SaveValue.getInstance(mContext).readBooleanValue(mType[i], false)) {
                SaveValue.getInstance(mContext).removekey(mType[i]);
            }
            if (sType[i] != null) {
                for (int j = 0; j < sType[i].length; j++) {
                    if (SaveValue.getInstance(mContext).readBooleanValue(sType[i][j], false)) {
                        SaveValue.getInstance(mContext).removekey(sType[i][j]);
                    }
                }
            }
        }
    }

    public void loadMonthAndWeekRes() {
        mMonthFull = new String[12];
        mMonthSimple = new String[12];
        mWeekFull = new String[7];
        mWeekSimple = new String[7];
        mMonthFull[0] = mContext.getString(R.string.nav_epg_January);
        mMonthFull[1] = mContext.getString(R.string.nav_epg_February);
        mMonthFull[2] = mContext.getString(R.string.nav_epg_March);
        mMonthFull[3] = mContext.getString(R.string.nav_epg_April);
        mMonthFull[4] = mContext.getString(R.string.nav_epg_May);
        mMonthFull[5] = mContext.getString(R.string.nav_epg_June);
        mMonthFull[6] = mContext.getString(R.string.nav_epg_July);
        mMonthFull[7] = mContext.getString(R.string.nav_epg_August);
        mMonthFull[8] = mContext.getString(R.string.nav_epg_September);
        mMonthFull[9] = mContext.getString(R.string.nav_epg_October);
        mMonthFull[10] = mContext.getString(R.string.nav_epg_November);
        mMonthFull[11] = mContext.getString(R.string.nav_epg_December);

        mMonthSimple[0] = mContext.getString(R.string.nav_epg_Jan);
        mMonthSimple[1] = mContext.getString(R.string.nav_epg_Feb);
        mMonthSimple[2] = mContext.getString(R.string.nav_epg_Mar);
        mMonthSimple[3] = mContext.getString(R.string.nav_epg_Apr);
        mMonthSimple[4] = mContext.getString(R.string.nav_epg_may);
        mMonthSimple[5] = mContext.getString(R.string.nav_epg_Jun);
        mMonthSimple[6] = mContext.getString(R.string.nav_epg_Jul);
        mMonthSimple[7] = mContext.getString(R.string.nav_epg_Aug);
        mMonthSimple[8] = mContext.getString(R.string.nav_epg_Sep);
        mMonthSimple[9] = mContext.getString(R.string.nav_epg_Oct);
        mMonthSimple[10] = mContext.getString(R.string.nav_epg_Nov);
        mMonthSimple[11] = mContext.getString(R.string.nav_epg_Dec);

        mWeekFull[0] = mContext.getString(R.string.nav_epg_Sunday);
        mWeekFull[1] = mContext.getString(R.string.nav_epg_Monday);
        mWeekFull[2] = mContext.getString(R.string.nav_epg_Tuesday);
        mWeekFull[3] = mContext.getString(R.string.nav_epg_Wednesday);
        mWeekFull[4] = mContext.getString(R.string.nav_epg_Thursday);
        mWeekFull[5] = mContext.getString(R.string.nav_epg_Friday);
        mWeekFull[6] = mContext.getString(R.string.nav_epg_Saturday);

        mWeekSimple[0] = mContext.getString(R.string.nav_epg_Sun);
        mWeekSimple[1] = mContext.getString(R.string.nav_epg_Mon);
        mWeekSimple[2] = mContext.getString(R.string.nav_epg_Tue);
        mWeekSimple[3] = mContext.getString(R.string.nav_epg_Wed);
        mWeekSimple[4] = mContext.getString(R.string.nav_epg_Thur);
        mWeekSimple[5] = mContext.getString(R.string.nav_epg_Fri);
        mWeekSimple[6] = mContext.getString(R.string.nav_epg_Sat);
    }

    public String[] getWeekFullArray() {
        return mWeekFull;
    }

    public String[] getWeekSimpleArray() {
        return mWeekSimple;
    }

    public String[] getMonthFullArray() {
        return mMonthFull;
    }

    public String[] getMonthSimpleArray() {
        return mMonthSimple;
    }

}
