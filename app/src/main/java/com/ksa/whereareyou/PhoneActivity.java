package com.ksa.whereareyou;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Locale;

public class PhoneActivity extends AppCompatActivity {

	public static String PHONE_EXTRA_KEY = "PHONE_NUMBER";
	private EditText phoneEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone);
		Intent intent = getIntent();
		phoneEditText = ((EditText)findViewById(R.id.phoneEditText));
		phoneEditText.setText(intent.getStringExtra(PHONE_EXTRA_KEY));
	}

	public void onConfirmClick(View view) {
		String phone = phoneEditText.getText().toString();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			phone = PhoneNumberUtils.formatNumberToE164(phone, Locale.getDefault().getCountry());
		}
		if (phone == null) {
			Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent();
			intent.putExtra(PHONE_EXTRA_KEY, phone);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
