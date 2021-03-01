package com.ktc.epg.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktc.epg.R;
import com.ktc.epg.epg.EPGChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;

import java.util.List;

public class ChannelListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<EPGChannelInfo> data;
    private int focusIndex = -1;


    public ChannelListAdapter(Context context, List<EPGChannelInfo> mData) {
        this.data = mData;
        this.mInflater = LayoutInflater.from(context);

    }

    public List<EPGChannelInfo> getData(){
        return data;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data == null ? null : data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.epg_list_view_channel_item, null);
            holder = new ViewHolder();
            holder.tvChannelName = convertView.findViewById(R.id.channel_name);
            holder.tvChannelNumber = convertView.findViewById(R.id.channel_number);
            holder.imRight = convertView.findViewById(R.id.chanel_right);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        EPGChannelInfo epgChannelInfo = data.get(position);
        holder.imRight.setVisibility(position == focusIndex ? View.VISIBLE : View.INVISIBLE);
        if (epgChannelInfo != null) {
            int channelNumber = epgChannelInfo.getmChanelNum();
            int serviceType = epgChannelInfo.getTVChannel().getServiceType();
            if (serviceType==1&& epgChannelInfo.getTVChannel() instanceof MtkTvAnalogChannelInfo){
                channelNumber = channelNumber -2000;
            }
            holder.tvChannelName.setText(epgChannelInfo.getName());
            holder.tvChannelNumber.setText(String.valueOf(channelNumber));
        }
        return convertView;
    }

    public class ViewHolder {
         TextView tvChannelNumber;
         TextView tvChannelName;
         ImageView imRight;
    }

    public void setSelection(int position) {
        focusIndex = position;
        notifyDataSetChanged();
    }

    public void appendData(List<EPGChannelInfo> list,boolean isNext){
        if (isNext){
            this.data.add(list.get(0));
        }else {
            this.data.add(0,list.get(0));
        }
        notifyDataSetChanged();
    }
}
