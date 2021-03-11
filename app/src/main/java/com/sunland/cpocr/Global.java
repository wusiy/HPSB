package com.sunland.cpocr;

import android.os.Build;

public class Global {

    /**
     * 用户解锁系统广播在本应用的广播
     */
    public static final String BROADCAST_ACTION_SCREEN_ON = "SUNLAND_BROADCAST_SCREEN_ON";    /**

    /**
     * 根据号牌识别结果，确定号牌种类
     *
     * @param hphm      识别出来的车辆号码
     * @param hpys      识别出来的号牌颜色名称或者代码（
     *                  0：代表未知颜色
     *                  1：代表蓝色车牌
     *                  2：代表黑色车牌
     *                  3：代表黄色车牌
     *                  4：代表白色车牌
     *                  5：代表绿色车牌（新能源汽车））
     * @param curHpzlDm 当前选择的号牌种类代码
     * @return 根据识别车辆的号牌信息判断的号牌种类
     */
    public static String getHpzl(String hphm, String hpys, String curHpzlDm) {
        String hpzl = "";
        if (hphm == null || hpys == null || curHpzlDm == null) {
            return "";
        }
        if (hphm.endsWith("港")) {//黑色车牌
            if (!curHpzlDm.equals("26")) {
                hpzl = "26";
            }
        } else if (hphm.endsWith("澳")) {//黑色车牌
            if (!curHpzlDm.equals("27")) {
                hpzl = "27";
            }
        } else if (hphm.endsWith("使")) {//黑色车牌
            if (!(curHpzlDm.equals("03") || curHpzlDm.equals("09"))) {//使馆汽车，使馆摩托
                hpzl = "03";// 默认使馆汽车
            }
        } else if (hphm.endsWith("领")) {//黑色车牌
            if (!(curHpzlDm.equals("04") || curHpzlDm.equals("10"))) {//领馆汽车，领馆摩托
                hpzl = "04";// 默认领馆汽车
            }
        } else if (hphm.endsWith("警")) {//白色车牌
            if (!(curHpzlDm.equals("23") || curHpzlDm.equals("24"))) {// 警用汽车，警用摩托
                hpzl = "23";// 默认警用汽车
            }
        } else if (hphm.endsWith("挂")) {//黄色车牌
            if (!curHpzlDm.equals("15")) {
                hpzl = "15";
            }
        } else if (hphm.endsWith("学")) {//黄色车牌
            if (!(curHpzlDm.equals("16") || curHpzlDm.equals("17"))) {// 教练汽车，教练摩托车
                hpzl = "16";// 默认教练汽车
            }
        } else if (!(curHpzlDm.equals("18") || curHpzlDm.equals("19")
                || curHpzlDm.equals("25") || curHpzlDm.equals("31")
                || curHpzlDm.equals("32") || curHpzlDm.equals("41")
                || curHpzlDm.equals("42") || curHpzlDm.equals("43")
                || curHpzlDm.equals("99"))) {// 试验汽车，试验摩托车，原农机号牌，武警号牌，军队号牌，无号牌，假号牌，挪用号牌，其他号牌
            if (hpys.contains("蓝")) {
                hpys = "1";
            } else if (hpys.contains("黑")) {
                hpys = "2";
            } else if (hpys.contains("黄")) {
                hpys = "3";
            } else if (hpys.contains("白")) {
                hpys = "4";
            } else if (hpys.contains("绿")) {
                hpys = "5";
            }
            switch (hpys) {
                case "0"://未知颜色
                    break;
                case "1"://蓝色车牌
                    if (!(curHpzlDm.equals("02") || curHpzlDm.equals("08"))) {// 小型汽车，轻便摩托车
                        hpzl = "02";// 默认小型汽车
                    }
                    break;
                case "2"://黑色车牌
                    if (!(curHpzlDm.equals("05") || curHpzlDm.equals("06") || curHpzlDm.equals("11")
                            || curHpzlDm.equals("12"))) {// 境外汽车，外籍汽车，境外摩托车，外籍摩托车
                        hpzl = "05";// 默认境外汽车
                    }
                    break;
                case "3"://黄色车牌
                    if (!(curHpzlDm.equals("01") || curHpzlDm.equals("07")
                            || curHpzlDm.equals("13") || curHpzlDm.equals("14"))) {// 大型汽车，普通摩托车，低速车，拖拉机
                        hpzl = "01"; // 默认大型汽车
                    }
                    break;
                case "4"://白色车牌
                    if (!(curHpzlDm.equals("20") || curHpzlDm.equals("21") || curHpzlDm.equals("22"))) {// 临时入境汽车，临时入境摩托车，临时行驶车
//                            hpzl = "22";// 默认临时行驶车
                        //由于目前车牌识别，容易将泛白的黄牌（阳光照射等情况）识别成白牌，白牌车辆又非常少，这里也默认成黄牌
                        hpzl = "01"; // 默认大型汽车
                    }
                    break;
                case "5"://绿色车牌（新能源汽车）
                    if (hphm.endsWith("D") || hphm.endsWith("F")) {//大型新能源汽车
                        if (!curHpzlDm.equals("51")) {
                            hpzl = "51";// 大型新能源汽车
                        }
                    } else {
                        if (!curHpzlDm.equals("52")) {
                            hpzl = "52";// 小型新能源汽车
                        }
                    }
                    break;
            }
        }
        return hpzl;
    }
}
