package com.treem.treem.application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.treem.treem.R;
import com.treem.treem.activities.signup.SignupQuestionActivity;
import com.treem.treem.activities.signup.phone.SignupPhoneActivity;
import com.treem.treem.services.Treem.TreemOAuthConsumerToken;
import com.treem.treem.services.Treem.TreemOAuthUserToken;

public class SessionExpiredActivity extends AppCompatActivity {
    private static final String EXTRA_CLEAR_DEVICE_TOKENS = "extra.clear.tokens";

    private boolean isDeviceTokenClear = false;

    public static void showExpiredDialog(Context context, boolean isDevicesTokenClear){
        Intent intent = new Intent(context,SessionExpiredActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_CLEAR_DEVICE_TOKENS,isDevicesTokenClear);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_expired);
        setTitle(R.string.title_session_expired);
        handleIntent();
        clearTokens();
        Button buttonOk = (Button)findViewById(R.id.buttonOk);
        if (buttonOk != null) {
            buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void clearTokens() {
        TreemOAuthUserToken.SHARED_INSTANCE.clearAccessTokens();
        TreemOAuthUserToken.SHARED_INSTANCE.clearUserStatus();
        if (isDeviceTokenClear) {
            TreemOAuthConsumerToken.SHARED_INSTANCE.clearDeviceSpecificTokens();
        }
    }

    private void handleIntent() {
        if (getIntent()!=null)
            isDeviceTokenClear = getIntent().getBooleanExtra(EXTRA_CLEAR_DEVICE_TOKENS,false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent;
        if (isDeviceTokenClear) {
            intent = new Intent(this, SignupQuestionActivity.class);
        } else {
            intent = new Intent(this, SignupPhoneActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
