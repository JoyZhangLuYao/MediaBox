package com.zly.zly.mediabox.Utils;

import android.content.Context;

/**
 * Created ZhangLuyao Tim on 2017/3/31.
 */

public class Util {
    public static  String exChange(String str){
        if(test(str)){
            StringBuffer sb = new StringBuffer();
            if(str!=null){
                for(int i=0;i<str.length();i++){
                    char c = str.charAt(i);
                    if(Character.isUpperCase(c)){
                        sb.append(Character.toLowerCase(c));
                    }else if(Character.isLowerCase(c)){
                        sb.append(Character.toUpperCase(c));
                    }
                }
            }
            return sb.toString();
        }
        return str;}
    public static  boolean test(String   s)
    {
        char   c   =   s.charAt(0);
        int   i   =(int)c;
        if((i>=65&&i<=90)||(i>=97&&i<=122)){
            return   true;
        } else   {
            return   false;
        }
    }

    public static String getString(Context context,int id){
        return context.getResources().getString(id);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale+0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
