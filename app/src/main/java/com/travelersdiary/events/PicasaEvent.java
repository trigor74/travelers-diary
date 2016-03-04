package com.travelersdiary.events;

import com.travelersdiary.picasa_model.PicasaFeed;

public class PicasaEvent extends BaseNetworkEvent {

    public static final OnLoadingError FAILED = new OnLoadingError(UNHANDLED_MSG, UNHANDLED_CODE);

    public static class OnLoadingStart extends OnStart<String> {
        public OnLoadingStart(String username) {
            super(username);
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

    public static class OnUploadingStart extends OnStart<String> {
        public OnUploadingStart(String username) {
            super(username);
        }
    }

    public static class OnUploadingPhotoStart extends OnStart<String> {
        public OnUploadingPhotoStart(String username, String albumId) {
            super(username, albumId);
        }
    }


}
