package com.travelersdiary.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.travelersdiary.R;
import com.travelersdiary.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements LoginView {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    private ProgressDialog authProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel = new LoginViewModel(this);

        binding.setViewModel(viewModel);

        viewModel.init(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.start(this);
    }

    @Override
    protected void onStop() {
        viewModel.stop();
        super.onStop();
    }

    @Override
    public void startActivityAndFinish(Intent intent) {
        startActivity(intent);
        finish();
    }

    @Override
    public void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.login_activity_error_dialog_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void showAuthProgressDialog() {
        authProgressDialog = new ProgressDialog(this);
        authProgressDialog.setTitle(getString(R.string.login_activity_progress_dialog_title));
        authProgressDialog.setMessage(getString(R.string.login_activity_progress_dialog_message));
        authProgressDialog.setCancelable(false);
        authProgressDialog.show();
    }

    @Override
    public void hideAuthProgressDialog() {
        authProgressDialog.dismiss();
    }

}
