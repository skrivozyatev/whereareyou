package com.ksa.whereareyou;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ksa.whereareyou.data.User;
import com.ksa.whereareyou.data.UserStatus;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
		GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
		LocationPermissionsDialog.ResultListener
{

	private static final int PHONE_ACTIVITY = 1;
	private GoogleMap map;
	private FirebaseAuth firebaseAuth;
	private FirebaseUser firebaseUser;
	private String username;
	private GoogleApiClient googleApiClient;
	private double latitude;
	private double longitude;
	private static final int RP_ACCESS_LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser == null) {
			startActivity(new Intent(this, SignInActivity.class));
			finish();
		} else {
			username = firebaseUser.getDisplayName();
			FirebaseDatabase.getInstance().getReference()
				.child("users").child(firebaseUser.getUid())
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						User user = dataSnapshot.getValue(User.class);
						if (user.getStatus() == UserStatus.NEW) {
							Intent intent = new Intent(MapsActivity.this, PhoneActivity.class);
							intent.putExtra(PhoneActivity.PHONE_EXTRA_KEY, user.getPhone());
							startActivityForResult(intent, PHONE_ACTIVITY);
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				});
		}

		googleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(Auth.GOOGLE_SIGN_IN_API)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case PHONE_ACTIVITY:
				if (resultCode == RESULT_OK) {
					DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
							.child("users").child(firebaseUser.getUid());
					reference.child("status").setValue(UserStatus.READY);
					String phone = data.getStringExtra(PhoneActivity.PHONE_EXTRA_KEY);
					reference.child("phone").setValue(phone);
					FirebaseDatabase.getInstance().getReference()
							.child("phoneUsers").child(phone)
							.setValue(firebaseUser.getUid());
				}
				break;
		}
	}

	@Override
	protected void onStart() {
		googleApiClient.connect();
		super.onStart();
	}

	@Override
	protected void onStop() {
		googleApiClient.disconnect();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.logoff_item:
				firebaseAuth.signOut();
				Auth.GoogleSignInApi.signOut(googleApiClient);
				username = getString(R.string.logged_off);
				startActivity(new Intent(this, SignInActivity.class));
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(this, R.string.google_play_services_error, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		moveToCurrentLocation();
	}

	@Override
	public void onLocationPermissionsDialog(String permission) {
		ActivityCompat.requestPermissions(this, new String[] { permission }, RP_ACCESS_LOCATION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case RP_ACCESS_LOCATION:
				if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
					moveToCurrentLocation();
				} else {
					Toast.makeText(this, R.string.no_location_permissions_were_granted, Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

	private boolean checkLocationPermissions() {
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
				|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION))
			{
				new LocationPermissionsDialog().show(getSupportFragmentManager(), getString(R.string.location));
			} else {
				ActivityCompat.requestPermissions(this, new String[] {
						android.Manifest.permission.ACCESS_FINE_LOCATION,
						android.Manifest.permission.ACCESS_COARSE_LOCATION
				}, RP_ACCESS_LOCATION);
			}
			return false;
		}
		return true;
	}

	private void moveToCurrentLocation() {
		if (checkLocationPermissions()) {
			@SuppressWarnings("MissingPermission")
			Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
			latitude = lastLocation.getLatitude();
			longitude = lastLocation.getLongitude();
			moveCamera();
		}
	}

	private void moveCamera() {
		if (map != null) {
			LatLng loc = new LatLng(latitude, longitude);
			map.addMarker(new MarkerOptions().position(loc).title("A location"));
			map.moveCamera(CameraUpdateFactory.newLatLng(loc));
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}
}
