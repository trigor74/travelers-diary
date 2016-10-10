package com.travelersdiary.login;

import android.content.Intent;

public interface LoginView {

    void startActivityForResult(Intent intent, int requestCode);

    void startActivityAndFinish(Intent intent);

    /**
     * Shows error dialog to users
     */
    void showErrorDialog(String message);

    void showAuthProgressDialog();

    void hideAuthProgressDialog();

}
