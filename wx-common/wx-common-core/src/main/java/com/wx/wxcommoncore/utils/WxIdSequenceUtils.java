package com.wx.wxcommoncore.utils;



import java.text.ParseException;

/**
  *
  * @ClassName WxIdSequenceUtils
  * @author gh
  * @Description 雪花算法：分布式 id 生成工具类
 *
 *
 *
  *
  *
  *
  *
  *
  *
  * @Date 2021/3/17 0017 11:22
  * @Version 1.0
  **/
public final class WxIdSequenceUtils {

    /**
     * wx项目创建时间 2021-01-13 17:29:00
     */
    private final static long START_TIME = 1610530140000L;










    public static void main(String[] args) throws ParseException {

//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        Date parse = simpleDateFormat.parse("2021-01-13 17:29:00");
//
//        System.out.println(parse.getTime());
//
//        System.out.println(simpleDateFormat.format(new Date(START_TIME)));


        System.out.println("最大利润："+buySell(new int[]{8,1,6,11,5,15}));

    }

    private static int buySell(int[] ints) {
        int max = 0;
        int buy = ints[0];

        for (int i = 1; i < ints.length; i++) {
            buy = Math.min(buy,ints[i-1]);
            max = Math.max(max,ints[i]-buy);
        }


        return max;
    }
}
