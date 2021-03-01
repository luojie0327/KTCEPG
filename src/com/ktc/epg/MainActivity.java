package com.ktc.epg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.DefaultItemAnimator;
import com.android.internal.widget.LinearLayoutManager;
import com.android.internal.widget.RecyclerView;
import com.ktc.epg.adapter.ChannelListAdapter;
import com.ktc.epg.adapter.EpgInfoAdapter;
import com.ktc.epg.epg.DataReader;
import com.ktc.epg.epg.EPGChannelInfo;
import com.ktc.epg.epg.EPGConfig;
import com.ktc.epg.epg.EPGProgramInfo;
import com.ktc.epg.epg.EPGTimeConvert;
import com.ktc.epg.epgUtil.EPGUtil;
import com.ktc.epg.view.CustomTimePicker;
import com.ktc.epg.view.EpgRecyclerView;
import com.ktc.epg.view.FocusLinerLayoutManager;
import com.ktc.epg.view.ToastFactory;
import com.mediatek.dm.DeviceManager;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    MtkTvTime mMtkTvTime;
    private DelayTextTask mDelayTextTask;
    private final static int DAYNUM = 8;
    public Context mContext;
    private static MainActivity mainActivity = null;
    private DataReader mReader;
    private List<EpgInfoAdapter> mAdapters = new ArrayList<>();
    private LongSparseArray<List<EPGProgramInfo>> mEPGProgramInfoLongSparseArray = new LongSparseArray<>();
    private ChannelListAdapter mChannelListAdapter;
    private boolean isFirstInit = false;
    private EPGProgramInfo playingInfo;
    private EPGChannelInfo currentChannel;
    private SparseIntArray positonSparseArry = new SparseIntArray(7);
    private final ITimeChange timeTimeChange = tv -> {
        final String[] str = getCurrentTime();
        for (int i = 0; i < tv.length; i++) {
            tv[0].setVisibility("".equals(str[0]) ? View.GONE : View.VISIBLE);
            int finalI = i;
            tv[i].post(() -> tv[finalI].setText(str[finalI]));
        }

    };
    private EpgHomeHolder mHolder;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EPGConfig.EPG_INIT_CHANNEL_LIST: {
                    Log.d(TAG, "refes");
                    ArrayList<EPGChannelInfo> newList;
                    newList = (ArrayList<EPGChannelInfo>) msg.obj;
                    if (!newList.isEmpty()) {
                        Log.d(TAG, "handleMessage: 111");
                        isFirstInit = true;
                        mChannelListAdapter = new ChannelListAdapter(mContext, newList);
                        mHolder.epg_home_channel_lv.setAdapter(mChannelListAdapter);
                        Message msg1 = mHandler.obtainMessage();
                        msg1.obj = mHolder.epg_home_channel_lv.getItemAtPosition(msg.arg1);
                        msg1.arg1 = msg.arg1;
                        msg1.what = EPGConfig.EPG_INIT_EVENT_LIST;
                        mHandler.sendMessageDelayed(msg1, 500);
                    }
                }
                break;
                case EPGConfig.EPG_DATA_RETRIEVING: {
                    currentChannel = (EPGChannelInfo) msg.obj;
                    getEpgInfoByChannel(currentChannel);
                    updateHeadInfo(playingInfo);
                }
                break;
                case EPGConfig.EPG_DATA_RETRIEVAL_FININSH: {
                    mHolder.startEpgsLoading(false);
                }
                break;
                case EPGConfig.EPG_INIT_EVENT_LIST: {
                    currentChannel = (EPGChannelInfo) msg.obj;
                    initEpgInfoByChannel(currentChannel);
                    updateHeadInfo(playingInfo);
                    mHolder.epg_home_channel_lv.setSelection(msg.arg1);
                    mHolder.epg_home_channel_lv.requestFocus();
                    mChannelListAdapter.setSelection(msg.arg1);


                    mHolder.startEpgsLoading(true);
                }
                break;
                case EPGConfig.EPG_DATA_APPEND: {
                    mAdapters.get(msg.arg1).updateData((List<EPGProgramInfo>) msg.obj);
                }
                break;
                case EPGConfig.EPG_EVENT_DATA_APPEND: {
                    mAdapters.get(msg.arg1).appendData((List<EPGProgramInfo>) msg.obj);
                }
                break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window mWindow = getWindow();
        if (null != mWindow) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        ((DestroyApp) getApplication()).add(this);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        mainActivity = this;
        mHolder = new EpgHomeHolder(mainActivity);
    }

    private void initData() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mMtkTvTime = MtkTvTime.getInstance();
        mReader = DataReader.getInstance();
        mReader.loadProgramType();
        mReader.loadMonthAndWeekRes();
        mDelayTextTask = new DelayTextTask(mHolder.epg_head_am, mHolder.epg_head_time, mHolder.epg_head_date, timeTimeChange);
        mDelayTextTask.start();
        mHolder.startEpgsLoading(true);
        mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_DATA_RETRIEVAL_FININSH, 5000);
        mHolder.epg_home_channel_lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: lv" + playingInfo);
                if (isFirstInit) {
                    isFirstInit = false;
                    updateHeadInfo(playingInfo);
                    return;
                }
                if (mHandler.hasMessages(EPGConfig.EPG_DATA_RETRIEVING)) {
                    mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
                }
                EPGConfig.SELECTED_CHANNEL_POSITION = position;
                mChannelListAdapter.setSelection(position);
                mHolder.startEpgsLoading(true);
                Message msg = mHandler.obtainMessage();
                msg.obj = mHolder.epg_home_channel_lv.getItemAtPosition(position);
                msg.what = EPGConfig.EPG_DATA_RETRIEVING;
                mHandler.sendMessageDelayed(msg, 500);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mHolder.epg_home_channel_lv.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                    || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                return getAppendChannel(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
            }


            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                RecyclerView.ViewHolder viewHolder = mHolder.epgsLvList.get(0).findViewHolderForAdapterPosition(positonSparseArry.get(0));
                if (viewHolder != null) {
                    viewHolder.itemView.requestFocus();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_TV_EPG && event.getAction() == KeyEvent.ACTION_DOWN) {
                finish();
            }
            return false;
        });


    }

    private Boolean getAppendChannel(boolean isNext) {
        ListView listView = mHolder.epg_home_channel_lv;
        List<EPGChannelInfo> mChannelInfo;
        if ((listView.getSelectedItemPosition() == listView.getLastVisiblePosition() && isNext)
                || (listView.getSelectedItemPosition() == listView.getFirstVisiblePosition() && !isNext)) {
            EPGChannelInfo channelInfo = (EPGChannelInfo) listView.getItemAtPosition(listView.getSelectedItemPosition());
            mChannelInfo = mReader.getAppendChannelList(channelInfo.getTVChannel().getChannelId(), isNext);
            Log.d(TAG, "getAppendChannel: epg get all channel list size " + mChannelInfo.size() + "  " + mChannelListAdapter.getCount());
            if (mChannelInfo.size() > 0) {
                mChannelListAdapter.appendData(mChannelInfo, isNext);
                mHolder.startEpgsLoading(true);
                Message msg = mHandler.obtainMessage();
                msg.obj = mChannelInfo.get(0);
                msg.what = EPGConfig.EPG_DATA_RETRIEVING;
                mHandler.sendMessageDelayed(msg, 100);
                return false;
            } else
                return EPGConfig.SELECTED_CHANNEL_POSITION == mChannelListAdapter.getCount() - 1 && isNext;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        initData();
        getChannelListWithThread();

    }


    private void getEpgInfoByChannel(EPGChannelInfo channelInfo) {
        mEPGProgramInfoLongSparseArray.clear();
        positonSparseArry.clear();
        for (int dayNum = 0; dayNum < 2; dayNum++) {
            int span = EPGConfig.mTimeSpan;
            int time = 0;
            if (dayNum == 0) {
                time = EPGUtil.getCurrentHour();
            }
            List<EPGProgramInfo> infos = getEpgEventInfo(time, span, dayNum);
            Log.d("Ktcepg", "getEpgInfoByChannel: " + infos.size());
            mEPGProgramInfoLongSparseArray.put(dayNum, infos);
            if (!mAdapters.isEmpty()) {
                EpgInfoAdapter adapter = mAdapters.get(dayNum);
                adapter.updateData(infos);
            }
        }

        if (mEPGProgramInfoLongSparseArray.size() > 0
                && mEPGProgramInfoLongSparseArray.get(0) != null
                && mEPGProgramInfoLongSparseArray.get(0).size() > 0) {
            playingInfo = mEPGProgramInfoLongSparseArray.get(0).get(0);
        } else {
            playingInfo = null;
        }
        mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_DATA_RETRIEVAL_FININSH, 500);

    }

    /**
     * get event by channel
     *
     * @param channelInfo channel
     */
    private void initEpgInfoByChannel(EPGChannelInfo channelInfo) {
        mEPGProgramInfoLongSparseArray.clear();
        for (int dayNum = 0; dayNum < DAYNUM; dayNum++) {
            int time = 0;
            int span = EPGConfig.mTimeSpan;
            if (dayNum == 0) {
                time = EPGUtil.getCurrentHour();
            }
            List<EPGProgramInfo> infos = new ArrayList<>();
            if (dayNum < 2) {
                infos = getEpgEventInfo(time, span, dayNum);
                mEPGProgramInfoLongSparseArray.put(dayNum, infos);
            }
            Log.d(TAG, "getEpgInfoByChannel: " + infos.isEmpty() + " dayNum== " + dayNum);
            EpgInfoAdapter adapter;
            EpgRecyclerView recyclerView = mHolder.epgsLvList.get(dayNum);
            if (mAdapters.size() != DAYNUM) {
                recyclerView.setLayoutManager(new FocusLinerLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                adapter = new EpgInfoAdapter(mContext, infos);
                recyclerView.setAdapter(adapter);
                mHolder.epgsLvList.get(dayNum).setAdapter(adapter);
                mAdapters.add(adapter);
                long startTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, EPGUtil.getCurrentHour());
                MtkTvTimeFormatBase tvTimeFormatBase = new MtkTvTimeFormatBase();
                tvTimeFormatBase.setByUtcAndConvertToLocalTime(startTime);
                String date = tvTimeFormatBase.monthDay + "." + (tvTimeFormatBase.month + 1) + "  " + EPGUtil.getWeek(tvTimeFormatBase.weekDay);
                mHolder.dateList.get(dayNum).setText(date);
            }


        }
        initEpgInfoListener();
        if (mEPGProgramInfoLongSparseArray.size() > 0
                && mEPGProgramInfoLongSparseArray.get(0) != null
                && mEPGProgramInfoLongSparseArray.get(0).size() > 0) {
            playingInfo = mEPGProgramInfoLongSparseArray.get(0).get(0);
        } else {
            playingInfo = null;
        }
        mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_DATA_RETRIEVAL_FININSH, 500);
    }

    private void initEpgInfoListener() {
        for (int dayNum = 0; dayNum < DAYNUM; dayNum++) {
            final int index = dayNum;
            EpgInfoAdapter epgInfoAdapter = mAdapters.get(dayNum);
            epgInfoAdapter.setOnKeyDownListener((position, keyCode, epgProgramInfo) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (index == 0) {
                        mHolder.epg_home_channel_lv.requestFocus();
                        updateHeadInfo(playingInfo);
                        if (mHolder.epg_home_channel_lv.getChildAt(EPGConfig.SELECTED_CHANNEL_POSITION) == null) {
                            mHolder.epg_home_channel_lv.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
                        } else {
                            mHolder.epg_home_channel_lv.getChildAt(EPGConfig.SELECTED_CHANNEL_POSITION).requestFocus();
                        }
                        return true;
                    } else {
                        RecyclerView recyclerView = mHolder.epgsLvList.get(index - 1);
                        recyclerView.findViewHolderForAdapterPosition(positonSparseArry.get(index - 1)).itemView.requestFocus();
                        return true;
                    }

                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                } else if (keyCode == KeyMap.KEYCODE_MTKIR_GREEN) {
                    startSchedule(epgProgramInfo, false, epgInfoAdapter);
                    epgInfoAdapter.notifyItemChanged(position);
                    return true;
                } else if (keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                    startSchedule(epgProgramInfo, true, epgInfoAdapter);
                    epgInfoAdapter.notifyItemChanged(position);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (index < DAYNUM - 1) {
                        if (mEPGProgramInfoLongSparseArray.get(index + 2) == null && index < DAYNUM - 2) {
                            updateAppendData(index + 2);
                        }
                        RecyclerView recyclerView = mHolder.epgsLvList.get(index + 1);
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(positonSparseArry.get(index + 1));
                        if (viewHolder != null) {
                            viewHolder.itemView.requestFocus();
                        }
                        if (mAdapters.get(index + 1).getData().size() > 0) {
                            updateHeadInfo(mAdapters.get(index + 1).getData().get(positonSparseArry.get(index + 1)));
                        } else {
                            updateHeadInfo(null);
                        }

                        return true;

                    }

                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    updateAppendEventData(epgInfoAdapter, position, index);

                } else if (keyCode == KeyEvent.KEYCODE_TV_EPG) {
                    finish();
                }

                return false;
            });
            epgInfoAdapter.setOnItemSelectListener(new EpgInfoAdapter.OnItemSelectListener() {
                @Override
                public void onItemSelect(View view, boolean canRecord, EPGProgramInfo epgProgramInfo, int pos) {
                    if (canRecord) {
                        updateHeadInfo(epgProgramInfo);
                    }
                    positonSparseArry.put(index, pos);
                }

                @Override
                public void omItemUnSelect(View view, int position) {
                    setHeadInvisable();
                }
            });
        }
    }

    private void updateAppendData(int index) {
        new Thread(() -> {
            List<EPGProgramInfo> infos = getEpgEventInfo(0, 4, index);
            mEPGProgramInfoLongSparseArray.put(index, infos);
            Message msg = Message.obtain();
            msg.what = EPGConfig.EPG_DATA_APPEND;
            msg.obj = infos;
            msg.arg1 = index;
            mHandler.sendMessage(msg);
        }).start();
    }

    private void updateAppendEventData(EpgInfoAdapter epgInfoAdapter, int position, int index) {
        new Thread(() -> {
            int lastPos = epgInfoAdapter.getData().size();
            if (lastPos > 0 && lastPos - 1 == position + 1) {
                long endTime = epgInfoAdapter.getData().get(position + 1).getmEndTime();
                long startTime = epgInfoAdapter.getData().get(position + 1).getmStartTime();
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTimeInMillis(startTime * 1000L);
                int startDay = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                gregorianCalendar.setTimeInMillis(endTime * 1000L);
                int endDay = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                if (endDay > startDay) {
                    return;
                }
                List<EPGProgramInfo> infos = mReader.readChannelProgramInfoByTime(currentChannel.getTVChannel(), endTime, EPGTimeConvert.getInstance().getHourtoMsec(EPGConfig.mTimeSpan));
                if (infos.size() > 0) {
                    Message msg = Message.obtain();
                    msg.what = EPGConfig.EPG_EVENT_DATA_APPEND;
                    msg.obj = infos;
                    msg.arg1 = index;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();

    }

    private List<EPGProgramInfo> getEpgEventInfo(long startTime, int span, int dayNum) {
        startTime = dayNum == 0 ? EPGUtil.getCurrentTime() : EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startTime);
//        if (dayNum!=0){
//            startTime =  EPGUtil.convertTime(MtkTvTime.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_BRDCST_UTC, startTime);
//        }
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(startTime * 1000L);
        List<EPGProgramInfo> infos = new ArrayList<>();
        while (infos.size() < 6) {
            if (span + gregorianCalendar.get(Calendar.HOUR_OF_DAY) > 24) {
                span = 24 - gregorianCalendar.get(Calendar.HOUR_OF_DAY);
                infos.addAll(mReader.readChannelProgramInfoByTime(currentChannel.getTVChannel(), startTime, EPGTimeConvert.getInstance().getHourtoMsec(span)));
                return infos;
            } else {
                infos.addAll(mReader.readChannelProgramInfoByTime(currentChannel.getTVChannel(), startTime, EPGTimeConvert.getInstance().getHourtoMsec(span)));
            }
            span = span + EPGConfig.mTimeSpan;
            if (infos.size() > 0) {
                startTime = infos.get(infos.size() - 1).getmEndTime();
            }
        }
        return infos;
    }

    private void updateHeadInfo(EPGProgramInfo epgProgramInfo) {
        if (epgProgramInfo != null) {
            if (epgProgramInfo.getMainType() >= 1) {
                if (MtkTvConfig.getInstance().getCountry().equals("GBR") && epgProgramInfo.getMainType() > mReader.getMainType().length) {
                    epgProgramInfo.setProgramType(mContext.getString(R.string.nav_epg_unsupport));
                } else {
                    epgProgramInfo.setProgramType(mReader.getMainType()[epgProgramInfo.getMainType() - 1]);
                }
            } else {
                epgProgramInfo.setProgramType(mContext.getString(R.string.nav_epg_unclassified));
            }
            mHolder.epg_title_name.setText(epgProgramInfo.getmTitle());
            mHolder.epg_title_age.setText(epgProgramInfo.getRatingType());
            mHolder.epg_title_genre.setText(mContext.getString(R.string.nav_epg_genre_type) + epgProgramInfo.getProgramType());
            mHolder.epg_title_info.setText(epgProgramInfo.getDescribe());
            String val = EPGUtil.formatStartTime(epgProgramInfo.getmStartTime(), mContext) + "-" + EPGUtil.formatStartTime(epgProgramInfo.getmEndTime(), mContext);
            mHolder.epg_title_time.setText(val);
        } else {
            setHeadInvisable();
            mHolder.epg_title_name.setText(R.string.nav_epg_no_program_data);
        }
    }


    private void setHeadInvisable() {
        mHolder.epg_title_name.setText("");
        mHolder.epg_title_age.setText("");
        mHolder.epg_title_genre.setText("");
        mHolder.epg_title_info.setText("");
        mHolder.epg_title_time.setText("");

    }


    private String[] getCurrentTime() {
        return EPGUtil.formatCurrentTimenew(mContext);
    }


    interface ITimeChange {
        void refresh(TextView[] tv);
    }

    class DelayTextTask {
        TextView[] textViews = new TextView[3];
        ITimeChange mITimeChange;
        Timer mTimer;

        DelayTextTask(TextView am, TextView time, TextView date, ITimeChange iTimeChange) {
            this.mITimeChange = iTimeChange;
            this.mTimer = new Timer();
            textViews[0] = am;
            textViews[1] = time;
            textViews[2] = date;
        }

        void start() {
            try {
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!Thread.currentThread().isInterrupted()) {
                            mITimeChange.refresh(textViews);
                        }
                    }
                }, 0, 30000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        void cancel() {
            if (mTimer != null) {
                mTimer.cancel();
            }
        }
    }

    private void getChannelListWithThread() {
        new Thread(() -> {
            ArrayList<EPGChannelInfo> channelList;
            do {
                channelList = (ArrayList<EPGChannelInfo>) mReader
                        .getAllChannelList(true);

                if (channelList != null && channelList.size() > 0) {
                    int channelId = MtkTvChannelList.getCurrentChannelId();
                    int index = 0;
                    for (int i = 0; i < channelList.size(); i++) {
                        if (channelId == channelList.get(i).getTVChannel().getChannelId()) {
                            index = i;
                            break;
                        }
                    }
                    Log.d(TAG, "run:  " + channelList.size() + " " + index);
                    Message msg = Message.obtain();
                    msg.arg1 = index;
                    msg.obj = channelList;
                    msg.what = EPGConfig.EPG_INIT_CHANNEL_LIST;
                    mHandler.sendMessage(msg);
                    break;
                }
            } while (true);
        }).start();
    }

    /**
     * Record or Reminder
     *
     * @param epgProgramInfo event info
     * @param isRecord       true: record  false:reminder
     */
    private void startSchedule(EPGProgramInfo epgProgramInfo, boolean isRecord, EpgInfoAdapter epgInfoAdapter) {
        int[] bookingId = EPGUtil.getBookingId(epgProgramInfo);
        if (bookingId[0] != -1
                && ((isRecord && bookingId[1] == EPGConfig.EPG_RECORD)
                || (!isRecord && bookingId[1] == EPGConfig.EPG_REMINDER))) {
            MtkTvRecord.getInstance().deleteBooking(bookingId[0]);
            ToastFactory.showToast(mContext, R.string.str_pick_cancel, Toast.LENGTH_SHORT);
            return;
        }
        if (isRecord) {
            if (DeviceManager.getInstance().getDeviceList().size() > 0) {
                CustomTimePicker recordTimePicker = new CustomTimePicker(mContext, epgProgramInfo, epgInfoAdapter);
                recordTimePicker.show();
                return;
            } else {
                ToastFactory.showToast(mContext, R.string.str_pick_insert_usb, Toast.LENGTH_SHORT);
                return;
            }
        }
        MtkTvTimeFormatBase mtkTvTimeFormatBase = new MtkTvTimeFormatBase();
        mtkTvTimeFormatBase.setByUtcAndConvertToLocalTime(epgProgramInfo.getmStartTime());
        long startTime = mtkTvTimeFormatBase.toSeconds();
        long endTime = epgProgramInfo.getmEndTime();
        Log.d(TAG, "startSchedule:111 " + startTime + "  " + endTime);
        MtkTvBookingBase item = new MtkTvBookingBase();
        startTime = EPGUtil.convertTime(MtkTvTimeBase.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_SYS_UTC, startTime) + 5;
        if (startTime != -1L) {
            item.setRecordStartTime(startTime);
        }
        item.setRecordDuration(1);
        Log.d(TAG, "startSchedule: " + startTime + "  " + endTime);
        item.setRecordMode(EPGConfig.EPG_REMINDER);
        item.setTunerType(MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC));
        item.setEventTitle(epgProgramInfo.getmTitle());
        item.setChannelId(epgProgramInfo.getChannelId());
        item.setSvlId(EPGUtil.getInstance().getSvl());
        Log.d("Ktcepg", "startSchedule: " + item.toString());
        int successStatus;
        if (bookingId[0] != -1) {
            successStatus = MtkTvRecord.getInstance().replaceBooking(bookingId[0], item);
        } else {
            successStatus = MtkTvRecord.getInstance().addBooking(item);
        }
        if (EPGUtil.books.size() >= 5) {
            ToastFactory.showToast(mContext, R.string.str_pick_max, Toast.LENGTH_SHORT);
            return;
        }
        if (0 == successStatus) {
            ToastFactory.showToast(mContext, R.string.str_pick_success, Toast.LENGTH_SHORT);
        } else {
            ToastFactory.showToast(mContext, R.string.str_pick_fail, Toast.LENGTH_SHORT);
        }


    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDelayTextTask.cancel();
        ((DestroyApp) getApplication()).remove(this);
    }
}
