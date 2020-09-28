package com.sunland.cpocr;

import android.os.Build;

public class Global {

    /**
     * 是否是广州市局渠道
     */
    public static boolean mApkIsGzsj = false;
    public static String lastDzGps = "";//用来存放地址GPS，用于跳转执法传值
    public static String Url = "";
    /**
     * 当前网络类型（1：移动，2：联通，3：电信）
     */
    public static int NET_TYPE = 1;
    /**
     * 设备型号
     */
    public static String MODEL = "";
    /**
     * 设备品牌
     */
    public static String BRAND = "";
    /**
     * 设备IMEI号
     */
    public static String IMEI = "";
    /**
     * 设备IMSI号
     */
    public static String IMSI = "";
    /**
     * 当前用户所在部门的类型（1：支队及其直属大队，2：新六区、广州港、机场，3：分局）
     */
    public static int bmType = 1;

    /**
     * 弹出式窗口定义
     */
    public static final int LOGIN_ACTIVITY = 4000;//登录窗口
    public static final int ADD_HOMEMENU_ACTIVITY = 4001;//添加首页快捷菜单窗口
    public static final int CAMERA_ACTIVITY = 4002;//拍照窗口
    public static final int ZPXZ_ACTIVITY = 4003;//照片选择窗口
    public static final int HPSB_ACTIVITY = 4004;//号牌识别窗口（调用相机过程）
    public static final int HPSB2_ACTIVITY = 4005;//号牌识别窗口（调用外部APP）
    public static final int ZXSM_ACTIVITY = 4006;//证芯扫描窗口
    public static final int READ_SFZ_ACTIVITY = 4007;//二代证读取窗口
    public static final int SCAN_SFZ_ACTIVITY = 4008;//二代证影像识别窗口
    public static final int ZDYHPT_ACTIVITY = 4009;//自定义号牌头设置窗口
    public static final int WSCPZ_ACTIVITY = 4010;//未上传凭证操作窗口
    public static final int WFDD_ACTIVITY = 4011;//违法地点选择窗口
    public static final int LDDM_ACTIVITY = 4012;//违法地点快速选择窗口
    public static final int WFXW_ACTIVITY = 4013;//违法行为选择窗口
    public static final int WFXW1_ACTIVITY = 4014;//违法行为1选择窗口
    public static final int WFXW2_ACTIVITY = 4015;//违法行为2选择窗口
    public static final int WFXW3_ACTIVITY = 4016;//违法行为3选择窗口
    public static final int WFXW4_ACTIVITY = 4017;//违法行为4选择窗口
    public static final int WFXW5_ACTIVITY = 4018;//违法行为5选择窗口
    public static final int SJWP_ACTIVITY = 4019;//收缴物品窗口
    public static final int TCC_ACTIVITY = 4020;//停车场选择窗口
    public static final int SJWPCFD_ACTIVITY = 4021;//收缴物品存放地输入窗口
    public static final int KLWPCFD_ACTIVITY = 4022;//扣留物品存放地输入窗口
    public static final int FZJG_ACTIVITY = 4023;//发证机关选择窗口
    public static final int CLLX_ACTIVITY = 4024;//车辆类型选择窗口
    public static final int PJXX_ACTIVITY = 4025;//票据信息输入窗口
    public static final int DZJCCL_ACTIVITY = 4026;//电子警察处理窗口
    public static final int PRINT_PREVIEW_ACTIVITY = 4027;//打印预览窗口
    public static final int BT_PAIR_ACTIVITY = 4028;//蓝牙配对窗口
    public static final int DZQZ_ACTIVITY = 4029; //电子签章的界面
    public static final int ZXSM1_ACTIVITY = 4030;//机动车证芯扫描窗口
    public static final int JYSG_ACTIVITY = 4089; //简易事故页面
    public static final int DCCF_ACTIVITY_A = 4090; //当场处罚界面(简易A版)
    public static final int DCCF_ACTIVITY = 4091; //当场处罚界面(简易B版)
    public static final int QZCS_ACTIVITY = 4092; //强制措施界面
    public static final int WFTZS_ACTIVITY = 4093; //违法通知书界面
    public static final int WFXWSUB_ACTIVITY = 4094;//自定义违法行为
    public static final int WFXWSUB1_ACTIVITY = 4095;//自定义违法行为
    public static final int WFXWSUB2_ACTIVITY = 4096;//自定义违法行为
    public static final int WFXWSUB3_ACTIVITY = 4097;//自定义违法行为
    public static final int WFXWSUB4_ACTIVITY = 4098;//自定义违法行为
    public static final int WFXWSUB5_ACTIVITY = 4099;//自定义违法行为

    public static final int QUERY_VEH_ACTIVITY = 5001;//机动车查询窗口
    public static final int QUERY_DRV_ACTIVITY = 5002;//驾驶证查询窗口
    public static final int QUERY_CENSUS_ACTIVITY = 5003;//人口（户籍）查询窗口

    public static final int SGDD_ACTIVITY = 6001;//事故地点选择窗口
    public static final int SGXT_ACTIVITY = 6002;//车辆形态选择窗口
    public static final int DCSG_ACTIVITY = 6003;//单车事故选择窗口
    public static final int CLJSG_ACTIVITY = 6004;//车辆间事故选择窗口
    public static final int RYXX_ACTIVITY = 6005;//事故人员信息窗口
    public static final int BXGS_ACTIVITY = 6006;//保险公司选择窗口
    public static final int SGYY_ACTIVITY = 6007;//事故原因选择窗口
    public static final int SGSS_ACTIVITY = 6008;//事故事实描述模版窗口
    public static final int TJJG_ACTIVITY = 6009;//事故调解结果模版窗口
    public static final int QUERY_RLSB_ACTIVITY = 6010;//人脸识别查询窗口

    public static final int JCBK_ADD_ACTIVITY = 7001;//添加缉查布控卡口信息窗口
    public static final int JCBK_YJXX_ACTIVITY = 7002;//预警信息窗口

    public static final int JJCL_CX_ACTIVITY = 8001;//抽血信息窗口
    public static final int JJCL_JYJD_ACTIVITY = 8002;//检验鉴定信息窗口
    public static final int SHOW_QWGL_PBXXDETAIL_ACTIVITY= 8003;//勤务管理排班信息列表界面
    public static final int UPLOADSCENE_AVTIVITY = 8004;//现场回传界面
    public static final int SACW_TRAILERREQUEST_AVTIVITY = 8005;//涉案财物申请拖吊车
    public static final int SACW_TRAILERREQUESTWF_AVTIVITY = 8006;//涉案财物申请拖吊车

    /**
     * 定义广播
     */
    public static final String BROADCAST_ACTION_SET_SYSTIME = "SUNLAND_BROADCAST_SET_SYSTIME";
    public static final String BROADCAST_ACTION_LOCK = "SUNLAND_BROADCAST_LOCK";
    public static final String BROADCAST_ACTION_JQ = "SUNLAND_BROADCAST_JQ";
    public static final String BROADCAST_ACTION_JQ_SAVED = "SUNLAND_BROADCAST_JQ_SAVED";
    public static final String BROADCAST_ACTION_SMS = "SUNLAND_BROADCAST_SMS";
    public static final String BROADCAST_ACTION_SMS_SAVED = "SUNLAND_BROADCAST_SMS_SAVED";
    public static final String BROADCAST_ACTION_KKBJ = "SUNLAND_BROADCAST_KKBJ";
    public static final String BROADCAST_ACTION_KKBJ_SAVED = "SUNLAND_BROADCAST_KKBJ_SAVED";
    public static final String BROADCAST_ACTION_GPS = "UPDATE_LOCATION";
    public static final String BROADCAST_ACTION_UPDATE = "SUNLAND_BROADCAST_UPDATE";
    public static final String BROADCAST_ACTION_IDATA_SCANRESULT = "android.intent.action.SCANRESULT";//盈达聚力iData50 扫描开启及结果返回
    public static final String BROADCAST_ACTION_QZCS_PZBH = "SUNLAND_BROADCAST_QZCS_PZBH";//盈达聚力iData50 扫描开启及结果返回

    /**
     * 外部包名（用于调用第三方应用）
     */
    public static final String EXTERNAL_PACKAGENAME_IDCREADER_NFC = "cn.sunlandgroup.wzydjw.nfcidcreader";
    public static final String EXTERNAL_PACKAGENAME_IDCREADER_OCR = "com.softsz.ocrID.Activity";
    public static final String EXTERNAL_PACKAGENAME_HPBD = "com.sunland.cpocr";
    public static final String EXTERNAL_PACKAGENAME_ZFCL = "cn.sunlandgroup.gzydjwapp.zfcl";
    public static final String EXTERNAL_PACKAGENAME_IDCARD = "com.softsz.ocrid";//身份证拍照识别
    public static final String EXTERNAL_PACKAGENAME_SGCL = "cn.sunlandgroup.gzydjwapp.sgcl";//事故处理模块
    public static final String EXTERNAL_PACKAGENAME_ZHCX = "cn.sunlandgroup.gzydjwapp.zhcx"; //综合查询模块
    public static final String EXTERNAL_PACKAGENAME_SACW = "cn.sunlandgroup.gzydjwapp.sacw"; //涉案财物模块
    public static final String EXTERNAL_PACKAGENAME_RPOS = "jjzt.ry.rposition";

    /**
     * 每页返回的记录数（当分页查询时有效）
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 当场缴款手撕发票最多张数
     */
    public static final int MAX_COUNT_PJ = 10;

    /**
     * 电子签章文件宽度（像素）
     */
    public static int DZQZ_FILE_WIDTH = 192;

    /**
     * 电子签章文件高度(像素)
     */
    public static int DZQZ_FILE_HEIGHT = 100;

    /**
     * 当前设备类型
     */
    public static boolean IS_P990_SOCSI = "P990-XD".equalsIgnoreCase(Build.MODEL.toUpperCase());//索信P990
    public static boolean IS_P990_SOCSI_7 = "P990-XD(7.0)".equalsIgnoreCase(Build.MODEL.toUpperCase()); //索信P990-XD(7.0)
    public static boolean IS_HD508_HITOWN = "HD508".equalsIgnoreCase(Build.MODEL.toUpperCase());//海棠HD508
    public static boolean IS_IDATA_50 ="ANDROID".equalsIgnoreCase(Build.MODEL.toUpperCase());//盈达聚力iData50
    public static boolean IS_HW_MATE10 = "ALP-AL00".equalsIgnoreCase(Build.MODEL.toUpperCase());//华为mate10手机
    public static boolean IS_HW_MATE20 = "HMA-AL00".equalsIgnoreCase(Build.MODEL.toUpperCase());//华为mate20手机
    public static boolean IS_NOMAL_PHONE = false;//普通手机
    public static boolean IS_P990_LANDI = false;


    //modified by 冒秉文 at 20160331
    //增加了发票管理相关全局定义
    public static final int MAX_SUPPORT_PJ = 10; //最大支持发票张数
    public static String CUR_PJBH_5 = "05";//当前5元的票据编号
    public static String CUR_PJBH_10 = "10";//当前10元的票据编号
    public static String CUR_PJBH_20 = "20";//当前20元的票据编号
    public static String CUR_PJBH_50 = "50";//当前50元的票据编号
    public static String CUR_PJME = "";//当前票据面额

    /**
     * 可选的“佳博PT-260（蓝牙）”打印机
     */
    public static final int PRN_PT_260_BT = 1;
    /**
     * 可选的“爱印互联IP-M22（蓝牙）”打印机
     */
    public static final int PRN_IP_M22_BT = 2;
    /**
     * 可选的“济强JLP352（蓝牙）”打印机
     */
    public static final int PRN_JLP352_BT = 3;
    /**
     * 可选的“济强VMP02（蓝牙）”打印机
     */
    public static final int PRINT_BRAND_JQ_VMP02= 4;

    /**
     * 设备（波特率）定义
     */
    public static final int BAUDRATE_DEV_K21 = 21;

    /**
     * 加载图章是否成功（针对索信P990）
     */
    public static boolean loadPicSuc = false;

    /**
     * 辅警绑定的民警警号
     */
    public static String zqmj = "";

    /**
     * 水印字体颜色
     */
    public static final String SY_TEXTCOLOR = "#22000000";
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
