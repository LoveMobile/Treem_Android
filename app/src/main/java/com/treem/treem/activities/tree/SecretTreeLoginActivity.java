package com.treem.treem.activities.tree;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.treem.treem.R;
import com.treem.treem.activities.pin.SetPinCodeActivity;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.DialogHelper;
import com.treem.treem.models.session.TreeSession;
import com.treem.treem.services.Treem.TreemAuthenticationService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;
import com.treem.treem.util.TextWatcherAdapter;

/**
 * Date: 7/4/16.
 */
public class SecretTreeLoginActivity extends AppCompatActivity {

	public static final String EXTRA_TOKEN = "token";

	private EditText pinCodeField;
	private Button enterButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_secret_tree_login);

		setupToolbar();
		initView();
	}

	/**
	 * Setup toolbar
	 */
	private void setupToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		if (toolbar == null) {
			return;
		}

		//set toolbar as action bar
		setSupportActionBar(toolbar);

		//close image button
		ImageView imageClose = (ImageView) toolbar.findViewById(R.id.toolbar_close);
		imageClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(); //finish activity on close click
			}
		});
	}

	private void initView() {
		pinCodeField = (EditText) findViewById(R.id.pin_code_field);
		enterButton = (Button) findViewById(R.id.button_enter);

		pinCodeField.addTextChangedListener(new TextWatcherAdapter() {

			@Override
			public void afterTextChanged(Editable s) {
				enterButton.setEnabled(s.length() >= 4);
			}
		});
	}

	public void onSetPinCodeButtonClick(View view) {
		SetPinCodeActivity.showSetPinCodeScreen(this);
	}

	public void onEnterButtonClick(View view) {
		String pin = pinCodeField.getText().toString();

		final ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.loading), true, false);

		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				progressDialog.dismiss();

				JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
				String token = jsonObject.get("token").getAsString();

				Intent intent = new Intent();
				intent.putExtra(EXTRA_TOKEN, token);

				setResult(RESULT_OK, intent);
				finish();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressDialog.dismiss();

				switch (error) {
					case GENERIC_RESPONSE_CODE_2:
						DialogHelper.showAlert(SecretTreeLoginActivity.this, R.string.error, R.string.error_pin_formar);
						break;
					case GENERIC_RESPONSE_CODE_3:
						DialogHelper.showAlert(SecretTreeLoginActivity.this, R.string.error_pin_locked_title, R.string.error_pin_locked_message);
						break;
					case GENERIC_RESPONSE_CODE_4:
						DialogHelper.showAlert(SecretTreeLoginActivity.this, R.string.pin_incorrect_title, R.string.pin_incorrect_message);
						break;
				}
			}
		};

		TreemAuthenticationService.checkPinCode(new TreeSession(CurrentTreeSettings.secretTreeID, null), request, pin);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.slide_down_over);
	}

	public static void showLoginScreen(@NonNull Activity context, int requestCode) {
		Intent intent = new Intent(context, SecretTreeLoginActivity.class);

		Bundle animationBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_over, R.anim.hold).toBundle();
		ActivityCompat.startActivityForResult(context, intent, requestCode, animationBundle);
	}
}
