package com.ksa.whereareyou;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by ksa on 07.02.2017
 */

public class LocationPermissionsDialog extends DialogFragment {

	private LocationPermissionsDialog.ResultListener resultListener;
	private int selection = -1;

	public interface ResultListener {
		void onLocationPermissionsDialog(String permission);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		resultListener = (LocationPermissionsDialog.ResultListener) context;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.location_permissions_dialog_header)
				.setSingleChoiceItems(new String[]{getString(R.string.fine_location), getString(R.string.coarse_location)}, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								selection = i;
							}
						}
				)
				.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						switch (selection) {
							case 0:
								resultListener.onLocationPermissionsDialog(Manifest.permission.ACCESS_FINE_LOCATION);
								break;
							case 1:
								resultListener.onLocationPermissionsDialog(Manifest.permission.ACCESS_COARSE_LOCATION);
								break;
						}
					}
				})
				.setNegativeButton(R.string.deny, null)
				.create();
	}
}
