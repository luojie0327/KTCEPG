package com.ktc.epg;

import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktc.epg.view.EpgRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class EpgHomeHolder {

    private MainActivity mContext;

    /***for epg head***/
    TextView epg_title_name, epg_title_time, epg_title_info, epg_title_age, epg_title_genre,epg_head_time,epg_head_date,epg_head_am;
    /***for channel list***/
    ListView epg_home_channel_lv;

    /***for epgs list***/
    private TextView epgs_date_0, epgs_date_1, epgs_date_2, epgs_date_3, epgs_date_4, epgs_date_5, epgs_date_6,epgs_date_7;
    private EpgRecyclerView epgs_list_0, epgs_list_1, epgs_list_2, epgs_list_3, epgs_list_4, epgs_list_5, epgs_list_6, epgs_list_7;
    private RelativeLayout epg_home_epgs_loading_ly;
    List<TextView> dateList = new ArrayList<TextView>();
    List<EpgRecyclerView> epgsLvList = new ArrayList<EpgRecyclerView>();

    EpgHomeHolder(MainActivity mContext) {
        this.mContext = mContext;
        findViews();
    }

    private void findViews() {
        epg_title_name = mContext.findViewById(R.id.tv_content_channel_name);
        epg_title_time = mContext.findViewById(R.id.tv_content_time);
        epg_title_age = mContext.findViewById(R.id.tv_content_age);
        epg_title_genre = mContext.findViewById(R.id.tv_content_genre);
        epg_title_info = mContext.findViewById(R.id.tv_content_description);
        epg_head_time = mContext.findViewById(R.id.tv_head_time);
        epg_head_date = mContext.findViewById(R.id.tv_head_date);
        epg_head_am = mContext.findViewById(R.id.tv_head_am);

        epg_home_channel_lv = mContext.findViewById(R.id.epg_main_chanel_list);
        epg_home_epgs_loading_ly = mContext.findViewById(R.id.epg_home_epgs_loading_ly);

        epgs_date_0 = mContext.findViewById(R.id.epgs_date_0);
        epgs_date_1 = mContext.findViewById(R.id.epgs_date_1);
        epgs_date_2 = mContext.findViewById(R.id.epgs_date_2);
        epgs_date_3 = mContext.findViewById(R.id.epgs_date_3);
        epgs_date_4 = mContext.findViewById(R.id.epgs_date_4);
        epgs_date_5 = mContext.findViewById(R.id.epgs_date_5);
        epgs_date_6 = mContext.findViewById(R.id.epgs_date_6);
        epgs_date_7 = mContext.findViewById(R.id.epgs_date_7);
        //lists
        epgs_list_0 = mContext.findViewById(R.id.epgs_list_0);
        epgs_list_1 = mContext.findViewById(R.id.epgs_list_1);
        epgs_list_2 = mContext.findViewById(R.id.epgs_list_2);
        epgs_list_3 = mContext.findViewById(R.id.epgs_list_3);
        epgs_list_4 = mContext.findViewById(R.id.epgs_list_4);
        epgs_list_5 = mContext.findViewById(R.id.epgs_list_5);
        epgs_list_6 = mContext.findViewById(R.id.epgs_list_6);
        epgs_list_7 = mContext.findViewById(R.id.epgs_list_7);

        dateList.add(epgs_date_0);
        dateList.add(epgs_date_1);
        dateList.add(epgs_date_2);
        dateList.add(epgs_date_3);
        dateList.add(epgs_date_4);
        dateList.add(epgs_date_5);
        dateList.add(epgs_date_6);
        dateList.add(epgs_date_7);

        epgsLvList.add(epgs_list_0);
        epgsLvList.add(epgs_list_1);
        epgsLvList.add(epgs_list_2);
        epgsLvList.add(epgs_list_3);
        epgsLvList.add(epgs_list_4);
        epgsLvList.add(epgs_list_5);
        epgsLvList.add(epgs_list_6);
        epgsLvList.add(epgs_list_7);

    }

    /**
     * Loading data
     *
     * @param toShow
     * @return void
     */
    void startEpgsLoading(boolean toShow) {
        epg_home_epgs_loading_ly.setVisibility(toShow ? View.VISIBLE : View.GONE);
    }

}
