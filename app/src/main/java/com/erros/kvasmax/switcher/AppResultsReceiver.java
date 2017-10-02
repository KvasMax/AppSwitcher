package com.erros.kvasmax.switcher;


import android.os.Bundle;
import android.os.ResultReceiver;

import android.os.Handler;

public class AppResultsReceiver extends ResultReceiver {

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle data);
    }

    private Receiver CREATOR;

    public AppResultsReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        CREATOR = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (CREATOR != null) {
            CREATOR.onReceiveResult(resultCode, resultData);
        }
    }
}