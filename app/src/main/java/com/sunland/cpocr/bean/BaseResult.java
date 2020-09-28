package com.sunland.cpocr.bean;

import java.io.Serializable;

public class BaseResult implements Serializable {
    public Integer code = 0;//返回代码	0-成功，1-失败，其他情况预留
    public String message = "";//接口成功或失败的具体描述
}
