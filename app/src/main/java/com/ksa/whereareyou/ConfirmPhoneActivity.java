package com.ksa.whereareyou;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;

import static com.ksa.whereareyou.PhoneActivity.PHONE_EXTRA_KEY;

public class ConfirmPhoneActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_phone);
		SMSSender smsSender = new SMSSender();
		smsSender.execute(getIntent().getStringExtra(PHONE_EXTRA_KEY), "12345");
	}

	private class SMSSender extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... strings) {
			String phone = strings[0];
			String message = strings[1];
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phone, null, message, null, null);
			return null;
		}
	}
}
