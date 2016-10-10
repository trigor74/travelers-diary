package com.travelersdiary;

import android.content.Context;
import android.databinding.BaseObservable;

public abstract class BaseViewModel extends BaseObservable {

    public abstract void start(Context context);

    public abstract void stop();

}
