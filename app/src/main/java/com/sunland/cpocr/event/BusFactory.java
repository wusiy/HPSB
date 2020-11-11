package com.sunland.cpocr.event;

/**
 * Created by wanglei on 2016/12/2.
 */

public class BusFactory {
    private static IBus bus;

    public static IBus getBus() {
        if (bus == null) {
            synchronized (com.sunland.cpocr.event.BusFactory.class) {
                if (bus == null) {
                    bus = new EventBusImpl();
                }
            }
        }
        return bus;
    }
}
