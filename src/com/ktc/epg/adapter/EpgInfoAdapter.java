package com.ktc.epg.adapter;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.RecyclerView;
import com.ktc.epg.R;
import com.ktc.epg.epg.EPGConfig;
import com.ktc.epg.epg.EPGProgramInfo;
import com.ktc.epg.epgUtil.EPGUtil;
import com.ktc.epg.view.ToastFactory;

import java.util.List;

public class EpgInfoAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<EPGProgramInfo> mData;
    private OnItemClickListener mListener;
    private OnItemSelectListener mOnItemSelectListener;
    private OnKeyDownListener onKeyDownListener;


    public OnKeyDownListener getOnKeyDownListener() {
        return onKeyDownListener;
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    public OnItemClickListener getListener() {
        return mListener;
    }

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public OnItemSelectListener getOnItemSelectListener() {
        return mOnItemSelectListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        mOnItemSelectListener = onItemSelectListener;
    }


    public EpgInfoAdapter(Context mContext, List<EPGProgramInfo> mData) {
        this.mContext = mContext;
        this.mData = mData;;
        this.setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_view_info_item, viewGroup, false);
        view.setFocusableInTouchMode(true);
        view.setFocusable(true);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<EPGProgramInfo> getData() {
        return mData;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            boolean canRecord = true;
            EPGProgramInfo epgProgramInfo =mData.size()==0?null:mData.get(position);
            if (mData.size()==0||mData.get(position) == null) {
                ((ViewHolder) viewHolder).getTvInfo().setText(mContext.getResources().getString(R.string.nav_epg_no_program_data));
                ((ViewHolder) viewHolder).getImReminds().setVisibility(View.GONE);
                ((ViewHolder) viewHolder).getImPlaying().setVisibility(View.GONE);
            }else {
                String str = epgProgramInfo.getmStartTimeStr() +
                        "-" + epgProgramInfo.getmEndTimeStr()
                        + "  " + epgProgramInfo.getmTitle()
                        + "  " + epgProgramInfo.mLongDescription
                        + " " + epgProgramInfo.getDescribe()
                        + "  " + epgProgramInfo.getmStartTime()
                        + "  " + epgProgramInfo.getProgramType()
                        + "  " + epgProgramInfo.getRatingType()
                        + "  " + epgProgramInfo.getChannelId()
                        + "  " + epgProgramInfo.getProgramId()
                        + "  " + epgProgramInfo.getmStartTime()
                        + "  " + epgProgramInfo.getmEndTime()
                        + "  " + epgProgramInfo.isProgramBlock()
                        + "  " + position
                        + "  " + mData.size();
                Log.d("Ktcepg", "getView: " + str);
                int[] recordType = EPGUtil.getBookingId(epgProgramInfo);
                    String val = EPGUtil.formatStartTime(epgProgramInfo.getmStartTime(),mContext) + "-" + EPGUtil.formatStartTime(epgProgramInfo.getmEndTime(),mContext) + "  " +epgProgramInfo.getmTitle();
                    ((ViewHolder) viewHolder).getTvInfo().setText(val);
                    ((ViewHolder) viewHolder).getImReminds().setVisibility(View.VISIBLE);
                    if (recordType[1] == EPGConfig.EPG_RECORD) {
                        ((ViewHolder) viewHolder).getImReminds().setImageResource(R.mipmap.record);
                    } else if (recordType[1] == EPGConfig.EPG_REMINDER) {
                        ((ViewHolder) viewHolder).getImReminds().setImageResource(R.mipmap.reminder);
                    } else {
                        ((ViewHolder) viewHolder).getImReminds().setVisibility(View.GONE);
                    }
                    ((ViewHolder) viewHolder).getImPlaying().setVisibility(isPlayingProgram(epgProgramInfo) ? View.VISIBLE : View.INVISIBLE);

                }

                viewHolder.itemView.setOnClickListener(view -> mListener.onItemClick(position));
                boolean finalCanRecord = canRecord;
                viewHolder.itemView.setOnKeyListener((view, keyCode, keyEvent) -> {
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_PROG_GREEN
                            || keyCode == KeyEvent.KEYCODE_PROG_RED) {
                        if (!finalCanRecord||epgProgramInfo==null) {
                            ToastFactory.showToast(mContext, R.string.str_main_no_data, Toast.LENGTH_SHORT);
                            return true;
                        }
                        if (isPlayingProgram(epgProgramInfo) && keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
                            ToastFactory.showToast(mContext, R.string.str_pick_past, Toast.LENGTH_SHORT);
                            return true;
                        }
                    }
                    return onKeyDownListener.onKey(position, keyCode, epgProgramInfo);
                });

                viewHolder.itemView.setOnFocusChangeListener((view, b) -> {
                    if (mOnItemSelectListener != null) {
                        ((ViewHolder) viewHolder).getTvInfo().setSelected(b);
                        if (b) {
                            mOnItemSelectListener.onItemSelect(view, finalCanRecord, epgProgramInfo,position);
                        } else {
                            mOnItemSelectListener.omItemUnSelect(view, position);
                        }
                    }
                });


            }

    }

    @Override
    public int getItemCount() {
        return mData.size() ==0 ? 1 : mData.size();
    }


    /**
     * current playing
     *
     * @param epgProgramInfo event
     * @return playing: true  not play: false
     */
    private boolean isPlayingProgram(EPGProgramInfo epgProgramInfo) {
        Long currentTime = EPGUtil.getCurrentTime();
        return currentTime >= epgProgramInfo.getmStartTime()
                && currentTime <= epgProgramInfo.getmEndTime();

    }

    public void updateData(List<EPGProgramInfo> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }
    public void appendData(List<EPGProgramInfo> mData) {
        this.mData.addAll(mData);
        notifyDataSetChanged();
    }

    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode, EPGProgramInfo epgProgramInfo);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemSelectListener {
        void onItemSelect(View view, boolean canRecord, EPGProgramInfo epgProgramInfo,int position);

        void omItemUnSelect(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvInfo;
        private ImageView imReminds;
        private ImageView imPlaying;

        private ViewHolder(View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(R.id.epg_list_info);
            imPlaying = itemView.findViewById(R.id.epg_list_info_playing);
            imReminds = itemView.findViewById(R.id.epg_list_info_reminds);

        }

        TextView getTvInfo() {
            return tvInfo;
        }

        public void setTvInfo(TextView tvInfo) {
            this.tvInfo = tvInfo;
        }

        ImageView getImReminds() {
            return imReminds;
        }

        public void setImReminds(ImageView imReminds) {
            this.imReminds = imReminds;
        }

        ImageView getImPlaying() {
            return imPlaying;
        }

        public void setImPlaying(ImageView imPlaying) {
            this.imPlaying = imPlaying;
        }


    }
}
