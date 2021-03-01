package com.ktc.epg.view;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.ktc.epg.R;
import com.ktc.epg.adapter.EpgInfoAdapter;
import com.ktc.epg.epg.EPGConfig;
import com.ktc.epg.epg.EPGProgramInfo;
import com.ktc.epg.epgUtil.EPGUtil;
import com.ktc.epg.epgUtil.Util;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CustomTimePicker {
    private static final String TAG = "CustomTimePicker";
    private Context mContext;
    private EPGProgramInfo mProgramInfo;
    private Dialog datePickDialog;
    private com.ktc.epg.view.PickerView mPvStartMonth, mPvStartDay, mPvStartHour, mPvStartMinute, mPvEndMonth, mPvEndDay, mPvEndHour, mPvEndMinute;
    private ArrayList<String> startMonth, startDay, startHour, startMinute, endMonth, endHour, endDay, endMinute;
    private final static int MAXMONTH = 12;
    private final static int MAXDAY = 31;
    private final static int MAXHOUR = 24;
    private final static int MAXMINUTE = 60;

    private MtkTvTimeFormatBase currentTimeFormat;
    private MtkTvTimeFormatBase endTimeFormat;
    private Calendar mCalendar = Calendar.getInstance();
    private Calendar mEndCalendar = Calendar.getInstance();
    private EpgInfoAdapter mEpgInfoAdapter;
    private GregorianCalendar startCalendar = new GregorianCalendar();
    private GregorianCalendar endCalendar = new GregorianCalendar();

    public CustomTimePicker(Context context, EPGProgramInfo epgProgramInfo, EpgInfoAdapter epgInfoAdapter) {
        this.mContext = context;
        this.mProgramInfo = epgProgramInfo;
        mEpgInfoAdapter = epgInfoAdapter;
        initDialog();
        initView();
        initData();
    }

    private void initView() {
        mPvStartMonth = datePickDialog.findViewById(R.id.start_pv_month);
        mPvStartDay = datePickDialog.findViewById(R.id.start_pv_day);
        mPvStartHour = datePickDialog.findViewById(R.id.start_pv_hour);
        mPvStartMinute = datePickDialog.findViewById(R.id.start_pv_minute);
        mPvEndMonth = datePickDialog.findViewById(R.id.end_pv_month);
        mPvEndDay = datePickDialog.findViewById(R.id.end_pv_day);
        mPvEndHour = datePickDialog.findViewById(R.id.end_pv_hour);
        mPvEndMinute = datePickDialog.findViewById(R.id.end_pv_minute);
        addListener();
        mPvStartDay.requestFocus();

    }


    private void addListener() {
        mPvStartMonth.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                if ((Integer.parseInt(text) == 2 && startCalendar.get(Calendar.DAY_OF_MONTH) > 29)
                        ||startCalendar.get(Calendar.DAY_OF_MONTH)>=mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                }
                startCalendar.set(Calendar.MONTH, Integer.parseInt(text) - 1);
                doStartDayChange();
            }

            @Override
            public void onClick() {
                startPVR();
            }
        });
        mPvStartDay.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                startCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
            }

            @Override
            public void onClick() {
                startPVR();
            }
        });
        mPvStartHour.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                startCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
            }

            @Override
            public void onClick() {
                startPVR();

            }
        });
        mPvStartMinute.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                startCalendar.set(Calendar.MINUTE, Integer.parseInt(text));
            }

            @Override
            public void onClick() {
                startPVR();
            }
        });
        mPvEndMonth.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                if (Integer.parseInt(text) == 2 && endCalendar.get(Calendar.DAY_OF_MONTH) > 29
                        ||endCalendar.get(Calendar.DAY_OF_MONTH)>=mEndCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    endCalendar.set(Calendar.DAY_OF_MONTH, 1);
                }
                endCalendar.set(Calendar.MONTH, Integer.parseInt(text) - 1);
                doEndDayChange();

            }

            @Override
            public void onClick() {
                startPVR();
            }
        });
        mPvEndDay.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                endCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
            }

            @Override
            public void onClick() {
                startPVR();
            }
        });
        mPvEndHour.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                Log.d(TAG, "onSelect: hour endTime " + text + "   " + endCalendar.toString());
                endCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
            }

            @Override
            public void onClick() {
                startPVR();

            }
        });
        mPvEndMinute.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                endCalendar.set(Calendar.MINUTE, Integer.parseInt(text));
            }

            @Override
            public void onClick() {
                startPVR();

            }
        });

    }

    private void initDialog() {
        if (datePickDialog == null) {
            datePickDialog = new Dialog(mContext, R.style.CustomDialog);
            datePickDialog.setCancelable(true);
            datePickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            datePickDialog.setContentView(R.layout.epg_record_time_picker);
//            Window window = datePickDialog.getWindow();
//            window.setGravity(Gravity.CENTER);
//            WindowManager manager = (WindowManager) mContex.getSystemService(Context.WINDOW_SERVICE);
//            DisplayMetrics dm = new DisplayMetrics();
//            manager.getDefaultDisplay().getMetrics(dm);
//            WindowManager.LayoutParams lp = window.getAttributes();
//            lp.width = dm.widthPixels;
//            window.setAttributes(lp);
        }
    }

    private void initData() {
        initArray();
        for (int i = 1; i <= MAXMONTH; i++) {
            startMonth.add(formatTimeUnit(i));
            endMonth.add(formatTimeUnit(i));
        }
        for (int i = 1; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            startDay.add(formatTimeUnit(i));
            endDay.add(formatTimeUnit(i));
        }

        for (int i = 0; i < MAXHOUR; i++) {
            startHour.add(formatTimeUnit(i));
            endHour.add(formatTimeUnit(i));
        }
        for (int i = 0; i < MAXMINUTE; i++) {
            startMinute.add(formatTimeUnit(i));
            endMinute.add(formatTimeUnit(i));
        }
        mPvStartMonth.setData(startMonth);
        mPvStartDay.setData(startDay);
        mPvStartHour.setData(startHour);
        mPvStartMinute.setData(startMinute);
        mPvEndMonth.setData(endMonth);
        mPvEndDay.setData(endDay);
        mPvEndHour.setData(endHour);
        mPvEndMinute.setData(endMinute);
        getCurrentTime();
        formatEndTime();
        changeDisplayTime();
        mPvStartMonth.setSelected(startCalendar.get(Calendar.MONTH));
        mPvStartDay.setSelected(startCalendar.get(Calendar.DAY_OF_MONTH) - 1);
        mPvStartHour.setSelected(startCalendar.get(Calendar.HOUR_OF_DAY));
        mPvStartMinute.setSelected(startCalendar.get(Calendar.MINUTE));
        mPvEndMonth.setSelected(endCalendar.get(Calendar.MONTH));
        mPvEndDay.setSelected(endCalendar.get(Calendar.DAY_OF_MONTH) - 1);
        mPvEndHour.setSelected(endCalendar.get(Calendar.HOUR_OF_DAY));
        mPvEndMinute.setSelected(endCalendar.get(Calendar.MINUTE));
    }

    private void initArray() {
        if (startMonth == null) startMonth = new ArrayList<>();
        if (startDay == null) startDay = new ArrayList<>();
        if (startHour == null) startHour = new ArrayList<>();
        if (startMinute == null) startMinute = new ArrayList<>();
        if (endMonth == null) endMonth = new ArrayList<>();
        if (endDay == null) endDay = new ArrayList<>();
        if (endHour == null) endHour = new ArrayList<>();
        if (endMinute == null) endMinute = new ArrayList<>();
        startMonth.clear();
        startDay.clear();
        startHour.clear();
        startMinute.clear();
        endMonth.clear();
        endDay.clear();
        endHour.clear();
        endMinute.clear();
    }

    /**
     * 将“0-9”转换为“00-09”
     */
    private String formatTimeUnit(int unit) {
        return unit < 10 ? "0" + unit : String.valueOf(unit);
    }

    private void getCurrentTime() {
        currentTimeFormat = MtkTvTime.getInstance().getBroadcastLocalTime();
        Log.d(TAG, "getCurrentTime: " + "  " + Util.formatTime2Local(mProgramInfo.getmStartTime()));
        if (currentTimeFormat.toSeconds() < Util.formatTime2Local(mProgramInfo.getmStartTime())) {
            currentTimeFormat.setByUtcAndConvertToLocalTime(mProgramInfo.getmStartTime());
        }
        mCalendar.set(Calendar.YEAR, currentTimeFormat.year);
    }

    private void formatEndTime() {
        endTimeFormat = new MtkTvTimeFormatBase();
        endTimeFormat.setByUtcAndConvertToLocalTime(mProgramInfo.getmEndTime());
        endTimeFormat.second = startCalendar.get(Calendar.MINUTE);
        Log.d(TAG, "formatEndTime endTime: " + endTimeFormat.toSeconds());
        mEndCalendar.set(Calendar.YEAR, endTimeFormat.year);
    }

    private void changeDisplayTime() {
        long startTime = currentTimeFormat.toSeconds();
        long endTime = endTimeFormat.toSeconds();
        startTime = EPGUtil.convertTime(MtkTvTime.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_BRDCST_UTC, startTime);
        endTime = EPGUtil.convertTime(MtkTvTime.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_BRDCST_UTC, endTime);
        startCalendar.setTimeInMillis(startTime * 1000L);
        endCalendar.setTimeInMillis(endTime * 1000L);

    }

    private void doStartDayChange() {
        mCalendar.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH));
        startDay.clear();
        for (int i = 1; i <= mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            startDay.add(formatTimeUnit(i));
        }
        mPvStartDay.setData(startDay);
        mPvStartDay.setSelected(startCalendar.get(Calendar.DAY_OF_MONTH)-1);

    }

    private void doEndDayChange() {
        mEndCalendar.set(Calendar.MONTH, endCalendar.get(Calendar.MONTH));
        endDay.clear();
        for (int i = 1; i <= mEndCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            endDay.add(formatTimeUnit(i));
        }
        mPvEndDay.setData(endDay);
        mPvEndDay.setSelected(endCalendar.get(Calendar.DAY_OF_MONTH)-1);
    }

    public void show() {
        datePickDialog.show();
    }

    private void dismiss() {
        if (datePickDialog != null) {
            datePickDialog.dismiss();
        }
    }

    private void startPVR() {
        int[] bookingId = EPGUtil.getBookingId(mProgramInfo);
        Log.d(TAG, "startPVR: start  " + startCalendar.toString() + "  end " + endCalendar.toString());
        endCalendar.set(Calendar.SECOND, startCalendar.get(Calendar.SECOND));
        Long startTime = startCalendar.getTimeInMillis() / 1000;
        Long endTime = endCalendar.getTimeInMillis() / 1000;
        Log.d(TAG, "startPVR: endTime " + endTime);
        currentTimeFormat.setByUtcAndConvertToLocalTime(mProgramInfo.getmStartTime());
        long currentTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_BRDCST_UTC, currentTimeFormat.toSeconds());
        Log.d(TAG, "startPVR: curr " + currentTime);
        Log.d(TAG, "startPVR: " + "  " + MtkTvTime.getInstance().getBroadcastUtcTime().toSeconds() + "   " + startTime + "  " + endTime + "  " + currentTimeFormat.toSeconds() + " " + endTimeFormat.toSeconds());
        if (startTime <= MtkTvTime.getInstance().getBroadcastUtcTime().toSeconds() || startTime < currentTime) {
            ToastFactory.showToast(mContext, R.string.str_pick_start_time_error, Toast.LENGTH_SHORT);
            return;
        }


        MtkTvBookingBase item = new MtkTvBookingBase();
        startTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_SYS_UTC, startTime) + 5;
        endTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_SYS_UTC, endTime) + 5;
        long duration = endTime - startTime;
        if (duration <= 0) {
            ToastFactory.showToast(mContext, R.string.str_pick_fail, Toast.LENGTH_SHORT);
            return;
        }
        if (startTime != -1L) {
            item.setRecordStartTime(startTime);
        }
        if (endTime != -1L) {
            item.setRecordDuration(duration);
        }
        item.setRecordMode(EPGConfig.EPG_RECORD);
        item.setTunerType(MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC));
        item.setEventTitle(mProgramInfo.getmTitle());
        item.setChannelId(mProgramInfo.getChannelId());
        item.setSvlId(EPGUtil.getInstance().getSvl());
        Log.d(TAG, "startPVR: " + item.toString());
        if (EPGUtil.timeExist(startTime, endTime)) {
            ToastFactory.showToast(mContext, R.string.str_pick_start_time_conflict, Toast.LENGTH_SHORT);
            return;
        }
        int successStatus = bookingId[0] == -1 ? MtkTvRecord.getInstance().addBooking(item) : MtkTvRecord.getInstance().replaceBooking(bookingId[0], item);
        if (EPGUtil.books.size() >= 5) {
            ToastFactory.showToast(mContext, R.string.str_pick_max, Toast.LENGTH_SHORT);
            return;
        }
        if (0 == successStatus) {
            ToastFactory.showToast(mContext, R.string.str_pick_success, Toast.LENGTH_SHORT);
            mEpgInfoAdapter.notifyDataSetChanged();
            dismiss();
        } else {
            ToastFactory.showToast(mContext, R.string.str_pick_fail, Toast.LENGTH_SHORT);
        }

    }


}
