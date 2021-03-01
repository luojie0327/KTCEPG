package com.ktc.epg.epg;

public class EPGConfig {
    public static final int mMaxDayNum = 8;
    public static final int mTimeSpan = 6;
    public static final int EPG_SYNCHRONIZATION_MESSAGE = 0x103;
    public static final int EPG_PROGRAMINFO_SHOW = 0x104;
    public static final int EPG_PROGRAM_STTL_SHOW = 0x105;
    public static final int EPG_UPDATE_CHANNEL_LIST = 0x106;
    public static final int EPG_GET_TIF_EVENT_LIST = 0x107;
    public static final int EPG_SET_LIST_ADAPTER = 0x108;
    public static final int EPG_NOTIFY_LIST_ADAPTER = 0x109;
    public static final int EPG_SHOW_LOCK_ICON = 0x110;
    public static final int EPG_SELECT_CHANNEL_COMPLETE = 0x111;
    public static final int EPG_UPDATE_API_EVENT_LIST = 0x112;
    public static final int EPG_INIT_EVENT_LIST = 0x113;
    public static final int EPG_INIT_CHANNEL_LIST = 0x114;
    public static final int EPG_REFRESH_CHANNEL_LIST = 0x116;

    public static boolean init = true;
    public static final int FROM_KEYCODE_DPAD_LEFT = 21;
    public static final int FROM_KEYCODE_DPAD_RIGHT = 22;
    public static final int FROM_KEYCODE_MTKIR_RED = 23;
    public static final int FROM_KEYCODE_MTKIR_GREEN = 24;
    public static final int FROM_ANOTHER_STREAM = 25;
    public static final int AVOID_PROGRAM_FOCUS_CHANGE = 26;
    public static final int SET_LAST_POSITION = 27;
    public static final int FROM_KEYCODE_MTKIR_OTHER = -1;
    public static int SELECTED_CHANNEL_POSITION = 0;
    public static int FROM_WHERE = SET_LAST_POSITION;

    public static final int EPG_DATA_RETRIEVING = 4;
    public static final int EPG_DATA_RETRIEVAL_FININSH = 5;
    public static final int EPG_DATA_APPEND = 6;
    public static final int EPG_EVENT_DATA_APPEND = 7;
    public static boolean avoidFoucsChange = false;
    public static final int EPG_LIST_ITME_COUNT = 6;

    //Record mode
    public static final int EPG_RECORD = 2;
    //Reminder mode
    public static final int EPG_REMINDER = 1;

}
