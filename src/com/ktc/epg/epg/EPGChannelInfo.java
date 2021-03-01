package com.ktc.epg.epg;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

import java.util.List;

public class EPGChannelInfo {
    private static final String TAG = "EPGChannelInfo";

    public long mId;

    private String mChannelName;
    private int mChannelNum;
    private int mSubChannelNum = 0;
    private String mDisplayNumber;
    private MtkTvChannelInfoBase mTVChannel;
    private List<EPGProgramInfo> mTVProgramInfoList;
    private Drawable mIsdbIcon;
    private boolean mIsCiVirturalCh = false;

    public MtkTvChannelInfoBase getTVChannel() {
        return mTVChannel;
    }

    public void setTVChannel(MtkTvChannelInfo mTVChannel) {
        this.mTVChannel = mTVChannel;
    }

    public EPGChannelInfo(Context context) {
    }

    public EPGChannelInfo(MtkTvChannelInfoBase mCurrentChannel) {
        MtkLog.e("MtkTvChannelInfoBase", "----EPGChannelInfo:" + mCurrentChannel);
        if (mCurrentChannel == null) {
            return;
        }
        String value = "";
        if (mCurrentChannel instanceof MtkTvATSCChannelInfo) {
            MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) mCurrentChannel;
            value = tmpAtsc.getMajorNum() + "-" + tmpAtsc.getMinorNum();
            MtkLog.e("saepg", "getChannelNumCur1===>" + value);
            mChannelNum = tmpAtsc.getMajorNum();
            mSubChannelNum = tmpAtsc.getMinorNum();

        } else if (mCurrentChannel instanceof MtkTvISDBChannelInfo) {
            MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) mCurrentChannel;
            value = tmpIsdb.getMajorNum() + "-" + tmpIsdb.getMinorNum();
            MtkLog.e("sapg", "getChannelNumCur2===>" + value);
            mChannelNum = tmpIsdb.getMajorNum();
            mSubChannelNum = tmpIsdb.getMinorNum();
        } else {
            value = " " + mCurrentChannel.getChannelNumber();
            mChannelNum = mCurrentChannel.getChannelNumber();
            MtkLog.e("sapg", "getChannelNumCur3===>" + value);
        }

        mChannelName = mCurrentChannel.getServiceName();
        if (mChannelName == null) {
            mChannelName = "";
        }
        mTVChannel = mCurrentChannel;
    }


    public boolean isCiVirturalCh() {
        return mIsCiVirturalCh;
    }

    public Drawable getIsdbIcon() {
        return mIsdbIcon;
    }

    public String getName() {
        return mChannelName;
    }

    public void setName(String name) {
        this.mChannelName = name;
    }

    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    public int getmChanelNum() {
        return mChannelNum;
    }

    public String getmChanelNumString() {
        if (mChannelNum < 10) {
            return "0" + mChannelNum;
        }
        return mChannelNum + "";
    }

    public String getmSubNum() {
        if (mSubChannelNum == 0) {
            return "";
        }
        if (mSubChannelNum < 10) {
            return "0" + mSubChannelNum;
        }
        return mSubChannelNum + "";
    }

    public void setmChanelNum(short mChanelNum) {
        this.mChannelNum = mChanelNum;
    }

    public List<EPGProgramInfo> getmTVProgramInfoList() {
        return mTVProgramInfoList;
    }

    public void setmTVProgramInfoList(List<EPGProgramInfo> mTVProgramInfoList) {
        this.mTVProgramInfoList = mTVProgramInfoList;
    }

    public List<EPGProgramInfo> getmGroup() {
        return mTVProgramInfoList;
    }


}
