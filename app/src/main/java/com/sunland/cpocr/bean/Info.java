package com.sunland.cpocr.bean;

import java.io.Serializable;

//import cn.sunlandgroup.ydjw.def.json.result.ResultQueryVeh;


public class Info implements Serializable {
    private String hphm = "";//号牌号码
    private String hpys = "";//号牌颜色
    private String hpzl = "";//号牌种类
    private String hpzlStr = "";//号牌种类中文描述
    private String message = "";//文字状态正在查询中,正常，在逃 错误等
    private String vehInfo = "";// 车辆信息
    private String tipInfo = ""; //自定义信息
    private ResultQueryVeh mQueryResult;

    public ResultQueryVeh getQueryResult() {
        return mQueryResult;
    }

    public void setQueryResult(ResultQueryVeh mQueryResult) {
        this.mQueryResult = mQueryResult;
    }

    private int code = 0;// -1错误信息，0正常信息，1正在查询中...，2正常信息里的除了正常状态外的信息

    public String getHphm() {
        return hphm;
    }

    public void setHphm(String hphm) {
        this.hphm = hphm;
    }

    public String getHpys() {
        return hpys;
    }

    public void setHpys(String hpys) {
        this.hpys = hpys;
    }

    public String getHpzl() {
        return hpzl;
    }

    public void setHpzl(String hpzl) {
        this.hpzl = hpzl;
    }

    public String getHpzlStr() {
        return hpzlStr;
    }

    public void setHpzlStr(String hpzlStr) {
        this.hpzlStr = hpzlStr;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVehInfo() {
        return vehInfo;
    }

    public void setVehInfo(String vehInfo) {
        this.vehInfo = vehInfo;
    }

    public String getTipInfo() {
        return tipInfo;
    }

    public void setTipInfo(String tipInfo) {
        this.tipInfo = tipInfo;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
