package com.sunland.cpocr.bean;

import java.io.Serializable;

public class ResultQueryVeh extends BaseResult {
    public VehInfo veh;
    public DzxmInfo[] dzxm;


    public static class DzxmInfo implements Serializable {
        public String jg = "";//是否警告
        public String nr = "";//内容
        public String xm = "";//项目
        public String xmdm = "";//项目代码
    }

    /**
     * 机动车信息
     */
    public static class VehInfo implements Serializable {
        public String xszbh = "";//行驶证编号
        public String hpzl = "";//号牌种类
        public String hpzlStr = "";//号牌种类（中文）
        public String hphm = "";//号牌号码
        public String clpp1 = "";//中文品牌
        public String clpp2 = "";//英文品牌
        public String gcjk = "";//国产/进口
        public String gcjkStr = "";//国产/进口（中文）
        public String zzg = "";//制造国
        public String zzgStr = "";//制造国（中文）
        public String zzcmc = "";//制造厂名称
        public String clxh = "";//车辆型号
        public String clsbdh = "";//车辆识别代号
        public String clsbdhHide = "";//车辆识别代号屏蔽部分
        public String fdjh = "";//发动机号
        public String fdjhHide = "";//发动机号屏蔽部分
        public String cllx = "";//车辆类型
        public String cllxStr = "";//车辆类型（中文）
        public String csys = "";//车身颜色
        public String csysStr = "";//车身颜色（中文）
        public String syxz = "";//使用性质
        public String syxzStr = "";//使用性质（中文）
        public String syr = "";//机动车所有人
        public String sfzmhm = "";//身份证明号码
        public String sfzmmc = "";//身份证明名称
        public String sfzmmcStr = "";//身份证明名称（中文）
        public String zsxxdz = "";//住所详细地址
        public String zsxzqh = "";//住所行政区划
        public String lxdh = "";//联系电话
        public String sjhm = "";//手机号码
        public String ccdjrq = "";//初次登记日期（yyyy-MM-dd）
        public String yxqz = "";//检验有效期止（yyyy-MM-dd）
        public String qzbfqz = "";//强制报废期止（yyyy-MM-dd）
        public String bxzzrq = "";//保险终止日期
        public String zt = "";//机动车状态
        public String ztStr = "";//机动车状态（中文）
        public String fzjg = "";//发证机关
        public String fzjgStr = "";//发证机关（中文）
        public String djrq = "";//定检日期（yyyy-MM-dd）
        public String xh = "";//序号
        public String syq = "";//所有权（1-单位 2-个人）
        public String hdzk = "";//核定载客
        public String qpzk = "";//驾驶室前排载客人数
        public String hpzk = "";//驾驶室后排载客人数
        public String zzl = "";//总质量
        public String zbzl = "";//装备质量
        public String zqyzl = "";//准牵引总质量
        public String hdzzl = "";//核定载质量
        public String ccrq = "";//出厂日期
        public String jyhgbzbh = "";//检验合格标志
        public String hgzbh = "";//合格证编号
        public String dybj = "";//抵押标记(0-未抵押，1-已抵押)
        public String fdjxh = "";//发动机型号
        public String pl = "";//排量
        public String gl = "";//功率
        public String zxxs = "";//转向形式
        public String gbthps = "";//钢板弹簧片数
        public String zs = "";//轴数
        public String zj = "";//轴距
        public String qlj = "";//前轮距
        public String hlj = "";//后轮距
        public String ltgg = "";//轮胎规格
        public String lts = "";//轮胎数
        public String cwkg = "";//车外廓高
        public String cwkc = "";//车外廓长
        public String cwkk = "";//车外廓宽
        public String hxnbcd = "";//货箱内部长度
        public String hxnbkd = "";//货箱内部宽度
        public String hxnbgd = "";//货箱内部高度
        public String dzbsxlh = "";//电子标识序列号
        public String hbdbqk = "";//环保达标情况
        public String rlzl = "";//燃料种类（代码）
        public String rlzlStr = "";//燃料种类（描述）
        public String clyt = "";//车辆用途
        public String clytStr = "";//车辆用途（中文）
        public String ytsx = "";//用途属性

        public String jgnr = "";
        public String wf24 = ""; //前24小时后1小时内违停数量
        public String zxyj = "";//执行依据
        public String bkfy = "";//布控法院
        public String jbr = "";//承办人
        public String fylxdh = "";//法院联系电话
        public String fycfbj = "";//0否，1查封
        public String sljybj = "";//申领检验标志受理回执标记（0-不存在 1-存在）
        public String ywsqr = "";//机动车申领检验标志业务申请人
        public String sqsj = "";//机动车申领检验标志申请时间
        public String hzm = "";//机动车申领检验标志受理回执码
        public String fzrq = "";//发证日期
        public String bpcs = "";//补牌次数
        public String bdjcs = "";//补（换领）证（书）次数
        public String sfxny = "";//是否新能源车
        public String kycllx = "";//客运车辆类型（0-不是客运车；1-公路客运；2-旅游客运）【调用集成平台客运车辆信息核查接口（63Q01），只核查号牌种类为01的】
        public String sfjbc = "";//是否接驳车（0-否；1-是）【调用集成平台客运车辆信息核查接口（63Q01），只核查号牌种类为01的】
    }
}
