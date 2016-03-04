package com.travelersdiary.events;

public class BaseNetworkEvent {

    public static final String UNHANDLED_MSG = "UNHANDLED_MSG";
    public static final int UNHANDLED_CODE = -1;

    protected static class OnStart<Rq> {
        private Rq mUsername;
        private Rq mAlbumId;

        public OnStart(Rq username) {
            mUsername = username;
        }

        public OnStart(Rq username, Rq albumId) {
            mUsername = username;
            mAlbumId = albumId;
        }

        public Rq getUsername() {
            return mUsername;
        }

        public Rq getAlbumId() {
            return mAlbumId;
        }
    }

    protected static class OnDone<Rs> {

        private Rs mResponse;

        public OnDone(Rs response) {
            mResponse = response;
        }

        public Rs getResponse() {
            return mResponse;
        }

    }

    protected static class OnFailed {

        private String mErrorMessage;
        private int mCode;

        public OnFailed(String errorMessage, int code) {
            mErrorMessage = errorMessage;
            mCode = code;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }

        public int getCode() {
            return mCode;
        }

    }

}
