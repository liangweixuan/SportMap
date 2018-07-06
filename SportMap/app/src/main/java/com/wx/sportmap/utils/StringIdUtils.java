package com.wx.sportmap.utils;

import android.content.Context;

import com.wx.sportmap.base.BaseApplication;


/**
 * Created by jd on 2017/4/12.
 */

public class StringIdUtils {
    private static StringIdUtils mInstance;
    private Context mContext;

    private StringIdUtils(){
       mContext = BaseApplication.getInstance();
    }
    public static StringIdUtils getInstance(){
        if(mInstance == null){
            synchronized (StringIdUtils.class){
                if(mInstance == null){
                    mInstance = new StringIdUtils();
                }
            }
        }
        return mInstance;
    }
    public String getString(int id){
       return mContext.getResources().getString(id);
    }

	public static String getSportTypeById(int sportType){
		String returnSportType = null;
		switch (sportType){
			case 1001:
				returnSportType = "四分钟步行";
				break;
			case 1002:
				returnSportType = "递增负荷";
				break;
			case 2001:
				returnSportType = "跑步";
				break;
			case 3001:
				returnSportType = "阻抗";
				break;
		}
		return returnSportType;
	}

}
