package com.sunland.cpocr.event;

/**
 * Event返回数据的基类
 * Created by Administrator on 2017/3/15.
 */

public class EventCenter<T> implements IBus.IEvent {

    /**
     * reserved data
     */
    private T data;

    /**
     * this code distinguish between different events
     */
    private int eventCode = -1;

    public EventCenter(int eventCode) {
        this(eventCode, null);
    }

    public EventCenter(int eventCode, T data) {
        this.eventCode = eventCode;
        this.data = data;
    }

    /**
     * get event code
     *
     * @return
     */
    public int getEventCode() {
        return this.eventCode;
    }

    /**
     * get event reserved data
     *
     * @return
     */
    public T getData() {
        return this.data;
    }

    @Override
    public int getTag() {
        return 10;
    }
}
