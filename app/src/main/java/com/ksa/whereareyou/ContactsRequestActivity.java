 package com.ksa.whereareyou;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ContactsRequestActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	SimpleCursorAdapter cursorAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts_request);
		getLoaderManager().initLoader(0, null, this);
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.contacts_request_item, null,
				new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY },
				new int[] { R.id.contacts_request_item_text }, 0);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts.DISPLAY_NAME_PRIMARY },
				"", new String[0], null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		cursorAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		cursorAdapter.swapCursor(null);
	}
}
