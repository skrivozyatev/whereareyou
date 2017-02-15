package com.ksa.whereareyou;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ksa.whereareyou.data.User;
import com.ksa.whereareyou.data.UserStatus;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

	private static final String TAG = "SignInActivity";
	private SignInButton signInButton;
	private GoogleApiClient googleApiClient;
	private FirebaseAuth firebaseAuth;
	private static final int RC_SIGN_IN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		firebaseAuth = FirebaseAuth.getInstance();
		signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(this);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.requestId()
				.requestProfile()
				.build();
		googleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.sign_in_button:
				signIn();
				break;
		}
	}

	private void signIn() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RC_SIGN_IN:
				GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
				if (result.isSuccess()) {
					final GoogleSignInAccount account = result.getSignInAccount();
					AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
					firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this,
							new OnCompleteListener<AuthResult>() {
								@Override
								public void onComplete(@NonNull Task<AuthResult> task) {
									if (task.isSuccessful()) {
										writeUser(task.getResult().getUser(), account);
										Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
										intent.putExtra(Constants.ACCOUNT_ID, account.getId());
										startActivity(new Intent(SignInActivity.this, MapsActivity.class));
										finish();
									} else {
										Toast.makeText(SignInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
									}
								}
							}
					);
				} else {
					Log.e(TAG, "Google Sign In failed");
				}
				break;
		}
	}

	private void writeUser(FirebaseUser firebaseUser, GoogleSignInAccount account) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
		User user = new User();
		user.setId(firebaseUser.getUid());
		user.setName(firebaseUser.getDisplayName());
		user.setFamilyName(account.getFamilyName());
		user.setGivenName(account.getGivenName());
		user.setEmail(account.getEmail());
		user.setPhone(getPhone());
		user.setStatus(UserStatus.NEW);
		reference.setValue(user);
		//reference = FirebaseDatabase.getInstance().getReference().child("userEmails").child(account.getEmail());
		//reference.setValue(firebaseUser.getUid());
		/*reference.child("id").setValue(account.getId());
		reference.child("name").setValue(account.getDisplayName());
		reference.child("email").setValue(account.getEmail());
		reference.child("phone").setValue(getPhone());*/
	}

	private String getPhone() {
		TelephonyManager tmgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		return tmgr.getLine1Number();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
}
