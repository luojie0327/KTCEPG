/**
 * @Description: TODO()
 *
 */
package com.ktc.epg.epgUtil;

import com.ktc.epg.epg.EPGProgramInfo;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class Util {
	public final static String TAG = "Util";

	public static String mapKeyCodeToStr(int keyCode){
        String _mStr = "";
        char _ch;
        switch (keyCode) {
        case KeyMap.KEYCODE_0:
        	_ch = '0';
        	_mStr = "0";
        	break;
        case KeyMap.KEYCODE_1:
        	_ch = '1';
        	_mStr = "1";
        	break;
        case KeyMap.KEYCODE_2:
        	_ch = '2';
        	_mStr = "2";
        	break;
        case KeyMap.KEYCODE_3:
        	_ch = '3';
        	_mStr = "3";
        	break;
        case KeyMap.KEYCODE_4:
        	_ch = '4';
        	_mStr = "4";
        	break;
        case KeyMap.KEYCODE_5:
        	_ch = '5';
        	_mStr = "5";
        	break;
        case KeyMap.KEYCODE_6:
        	_ch = '6';
        	_mStr = "6";
        	break;
        case KeyMap.KEYCODE_7:
        	_ch = '7';
        	_mStr = "7";
        	break;
        case KeyMap.KEYCODE_8:
        	_ch = '8';
        	_mStr = "8";
        	break;
        case KeyMap.KEYCODE_9:
        	_ch = '9';
        	_mStr = "9";
        	break;
        default:
        	break;
        }

        return _mStr;
	}

	public static byte [] stringToByte(String s){
        byte[] b = new byte[3];
        if (s != null && s.length() == 5) {
        	byte[] bytes = s.getBytes();
        	for (int i = 0; i < bytes.length; i++) {
                System.out.println(i + "  = "
                        + Integer.toBinaryString(bytes[i] - 48));
        	}
        	b[0] = (byte) (((bytes[0] - 48) * 16) | (bytes[1] - 48));
        	b[1] = (byte) (((bytes[2] - 48) * 16) | (bytes[3] - 48));
        	b[2] = (byte) (((bytes[4] - 48) * 16) | (0x0F));
        }
        return b;
	}
	public static String convertConurty(String conurty){
        MtkLog.d(TAG, "conurty="+conurty);
        String newCountry=null;
        if(MtkTvConfigTypeBase.S3166_CFG_COUNT_AUS.equalsIgnoreCase(conurty)){
        	newCountry="AU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_BEL.equalsIgnoreCase(conurty)){
        	newCountry="BE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_CHE.equalsIgnoreCase(conurty)){
        	newCountry="CH";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_CZE.equalsIgnoreCase(conurty)){
        	newCountry="CS";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_DEU.equalsIgnoreCase(conurty)){
        	newCountry="DE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_DNK.equalsIgnoreCase(conurty)){
        	newCountry="DN";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ESP.equalsIgnoreCase(conurty)){
        	newCountry="ES";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_FIN.equalsIgnoreCase(conurty)){
        	newCountry="FI";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_FRA.equalsIgnoreCase(conurty)){
        	newCountry="FR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_GBR.equalsIgnoreCase(conurty)){
        	newCountry="GB";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ITA.equalsIgnoreCase(conurty)){
        	newCountry="IT";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_LUX.equalsIgnoreCase(conurty)){
        	newCountry="LU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_NLD.equalsIgnoreCase(conurty)){
        	newCountry="NL";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_NOR.equalsIgnoreCase(conurty)){
        	newCountry="NO";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SWE.equalsIgnoreCase(conurty)){
        	newCountry="SE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_HRV.equalsIgnoreCase(conurty)){
        	newCountry="HR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_GRC.equalsIgnoreCase(conurty)){
        	newCountry="GR";
        }
        else if(MtkTvConfigTypeBase.S639_CFG_LANG_HUN.equalsIgnoreCase(conurty)){
        	newCountry="HU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_IRL.equalsIgnoreCase(conurty)){
        	newCountry="IE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_POL.equalsIgnoreCase(conurty)){
        	newCountry="PL";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_PRT.equalsIgnoreCase(conurty)){
        	newCountry="PT";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ROU.equalsIgnoreCase(conurty)){
        	newCountry="RO";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_RUS.equalsIgnoreCase(conurty)){
        	newCountry="RU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SRB.equalsIgnoreCase(conurty)){
        	newCountry="SR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SVK.equalsIgnoreCase(conurty)){
        	newCountry="SK";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SVN.equalsIgnoreCase(conurty)){
        	newCountry="SI";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_TUR.equalsIgnoreCase(conurty)){
        	newCountry="TR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_EST.equalsIgnoreCase(conurty)){
        	newCountry="EE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_UKR.equalsIgnoreCase(conurty)){
        	newCountry="UA";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_THA.equalsIgnoreCase(conurty)){
            newCountry="TH";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ZAF.equalsIgnoreCase(conurty)){
            newCountry="ZA";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SQP.equalsIgnoreCase(conurty)){
            newCountry="SG";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ARG.equalsIgnoreCase(conurty)){
            newCountry="AR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_BRA.equalsIgnoreCase(conurty)){
            newCountry="BR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_CAN.equalsIgnoreCase(conurty)){
            newCountry="CA";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_JPN.equalsIgnoreCase(conurty)){
            newCountry="JP";
        }
        else{
        	newCountry=conurty;
        }
        return newCountry;
	}
    public static String buildSelectionForIds(String idName, List<Long> ids) {
        StringBuilder sb = new StringBuilder();
        sb.append(idName).append(" in (")
                .append(ids.get(0));
        for (int i = 1; i < ids.size(); ++i) {
            sb.append(",").append(ids.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    public static String formatTime24_12(String strTime){
        MtkLog.d(TAG, "formatTime24_12-----> strTime="+strTime );
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        String formatTime="";
        try{
            Date time=sdf.parse(strTime);
            SimpleDateFormat formatSdf=new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            formatTime=formatSdf.format(time);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        MtkLog.d(TAG, "formatTime24_12-----> formatTime="+formatTime );
        return formatTime;
    }

    public static String formatTime12_24(String strTime){
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
        String formatTime="";
        try{
            Date time=sdf.parse(strTime);
            SimpleDateFormat formatSdf=new SimpleDateFormat("HH:mm");
            formatTime=formatSdf.format(time);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        MtkLog.d(TAG, "formatTime12_24-----> formatTime="+formatTime );
        return formatTime;
    }

    public static long formatTime2Local(long time){
        MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
        timeFormatBase.setByUtcAndConvertToLocalTime(time);
        return timeFormatBase.toSeconds();
    }

}
