package com.pengl.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class Utils {


    /**
     * dip转px
     *
     * @param dipValue dip的值
     * @return px的值
     */
    public static int getPixels(Context ctx, int dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, ctx.getResources().getDisplayMetrics());
    }

    /**
     * 转换时间格式
     *
     * @param millis 毫秒
     * @return 1'2''
     */
    static String toTime(long millis) {
        return secToTime((int) (millis / 1000));
    }

    /**
     * 转换时间格式
     *
     * @param sec 秒
     * @return 1'2''
     */
    @SuppressLint("DefaultLocale")
    public static String secToTime(int sec) {
        int var1 = sec / 60;
        if (var1 >= 60) {
            var1 %= 60;
        }
        int var3 = sec % 60;
        if (var1 > 0) {
            return String.format("%d′%d\"", var1, var3);
        }
        return String.format("%d\"", var3);
    }


    /**
     * 取时间戳
     *
     * @return yyyyMMddHHmmss
     */
    public static String yyyyMMddHHmmss() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(System.currentTimeMillis());
    }

    /**
     * 获取随机数
     *
     * @param iRdLength 随机数的长度
     * @return 随机数
     */
    public static String getRandom(int iRdLength) {
        Random rd = new Random();
        int iRd = rd.nextInt();
        if (iRd < 0) { // 负数时转换为正数
            iRd *= -1;
        }
        String sRd = String.valueOf(iRd);
        int iLgth = sRd.length();
        if (iRdLength > iLgth) { // 获取数长度超过随机数长度
            // 将整数转化为一个n位的字符串
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < iRdLength - String.valueOf(iRd).length(); i++) {
                result.append("0");
            }
            result.append(String.valueOf(iRd));
            return result.toString();
        } else {
            return sRd.substring(iLgth - iRdLength, iLgth);
        }
    }

}
