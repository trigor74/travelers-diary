package com.travelersdiary.events;

import com.travelersdiary.picasa_model.PicasaFeed;

public class LoadPicasaAlbumsEvent extends BaseNetworkEvent {

    public static final OnLoadingError FAILED = new OnLoadingError(UNHANDLED_MSG, UNHANDLED_CODE);

    public static class OnLoadingStart extends OnStart<String> {
        public OnLoadingStart(String request) {
            super(request);
        }
    }

    public static class OnLoaded extends OnDone<PicasaFeed> {
        public OnLoaded(PicasaFeed response) {
            super(response);
        }
    }

    public static class OnLoadingError extends OnFailed {
        public OnLoadingError(String errorMessage, int code) {
            super(errorMessage, code);
        }
    }


}
