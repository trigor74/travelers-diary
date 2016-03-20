package com.travelersdiary.bus;


import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusProvider {

    private static volatile Bus instance;

    private BusProvider() {
    }

    public static Bus bus() {
        Bus localInstance = instance;
        if (localInstance == null) {
            synchronized (AndroidBus.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new AndroidBus(ThreadEnforcer.ANY);
                }
            }
        }
        return localInstance;
    }

}
