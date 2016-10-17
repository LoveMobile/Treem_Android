package com.treem.treem.activities.pin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.treem.treem.R;
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
public class SetPinCodeActivity extends AppCompatActivity {

	private EditText newPinCodeField;
	private EditText pinCodeConfirmationField;
	private EditText currentPinCodeField;
	private Button setButton;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_pin);

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
		ImageView imageClose = (ImageView) toolbar.findViewById(R.id.toolbar_home);
		imageClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(); //finish activity on close click
			}
		});
	}

	private void initView() {
		newPinCodeField = (EditText) findViewById(R.id.new_pin_code_field);
		pinCodeConfirmationField = (EditText) findViewById(R.id.conform_pin_code_field);
		currentPinCodeField = (EditText) findViewById(R.id.current_pin_code_field);
		setButton = (Button) findViewById(R.id.button_set);

		TextWatcherAdapter textWatcher = new TextWatcherAdapter() {

			@Override
			public void afterTextChanged(Editable s) {
				String newPin = newPinCodeField.getText().toString();
				String pinConfirmation = pinCodeConfirmationField.getText().toString();
				String currentPin = currentPinCodeField.getText().toString();

				setButton.setEnabled(newPin.length() >= 4
						&& pinConfirmation.length() >= 4
						&& newPin.equals(pinConfirmation)
						&& (currentPin.length() == 0 || currentPin.length() >= 4));
			}
		};

		newPinCodeField.addTextChangedListener(textWatcher);
		pinCodeConfirmationField.addTextChangedListener(textWatcher);
		currentPinCodeField.addTextChangedListener(textWatcher);
	}

	public void onSetPinCodeButtonClick(View view) {
		DialogHelper.showConfirmation(this, R.string.set_pin_title, R.string.set_pin_message, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setPinCode();
			}
		});
	}

	private void setPinCode() {
		String pin = newPinCodeField.getText().toString();
		String currentPin = currentPinCodeField.getText().toString();

		final ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.loading), true, false);

		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				progressDialog.dismiss();
				finish();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressDialog.dismiss();

				switch (error) {
					case GENERIC_RESPONSE_CODE_2:
						DialogHelper.showAlert(SetPinCodeActivity.this, R.string.error, R.string.error_pin_formar);
						break;
					case GENERIC_RESPONSE_CODE_3:
						DialogHelper.showAlert(SetPinCodeActivity.this, R.string.error_pin_locked_title, R.string.error_pin_locked_message);
						break;
				}
			}
		};

		TreemAuthenticationService.setPinCode(new TreeSession(CurrentTreeSettings.secretTreeID, null), request, pin, currentPin);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.slide_down_over);
	}

	public static void showSetPinCodeScreen(Activity context) {
		Intent intent = new Intent(context, SetPinCodeActivity.class);

		Bundle animationBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_over, R.anim.hold).toBundle();
		ActivityCompat.startActivity(context, intent, animationBundle);
	}
}
