package com.pro.bookpro.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.pro.bookpro.R;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.prefs.DataStoreManager;
import com.pro.bookpro.utils.StringUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::goToNextActivity, 2000);
    }

    private void goToNextActivity() {
        if (DataStoreManager.getUser() != null && !StringUtil.isEmpty(DataStoreManager.getUser().getEmail())) {
            GlobalFunction.gotoMainActivity(this);
        } else {
            GlobalFunction.startActivity(this, SignInActivity.class);
        }
        finish();
    }
}