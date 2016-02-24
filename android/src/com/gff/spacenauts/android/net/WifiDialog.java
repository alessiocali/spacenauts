package com.gff.spacenauts.android.net;

import com.gff.spacenauts.android.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * An incredibly simple pop-up dialog that asks the user to turn WiFi on.
 * 
 * @author Alessio Cali'
 *
 */
public class WifiDialog extends Activity {

	private WifiManager wifiManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wi_fi);
		setImmersive();
		wifiManager = (WifiManager) getSystemService(Activity.WIFI_SERVICE);
	}
	
	/**
	 * Enables immersive mode if the Android version supports it.
	 */
	@SuppressLint("InlinedApi")
	private void setImmersive() {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			findViewById(R.id.MainLayout)
			.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);	
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wi_fi, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Yes button callback. Creates a dialog while waiting to turn WiFi on.
	 * 
	 * @param arg
	 */
	public void yes (View arg) {
		//Turn on WiFi
		wifiManager.setWifiEnabled(true);
		
		//Create dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Turning on WiFi...").setNegativeButton("Cancel", new OnClickListener () {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				wifiManager.setWifiEnabled(false);
				fail();
			}
			
		});
		final AlertDialog dialog = builder.create();
		
		//Create receiver for WiFi state change
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				
				if (state == WifiManager.WIFI_STATE_ENABLED) { 
					if (dialog.isShowing()) dialog.dismiss();
					success();
				} else if (state == WifiManager.WIFI_STATE_ENABLING) {
					dialog.setMessage("Turning on WiFi...");
				} else {	
					/*	
					 * Note : should specify other cases. If the system is not quick
					 * enough it might stay on DISABLED for a while, giving an error.
					 */
					dialog.setMessage("Error enabling WiFi.");
				}
			}
			
		}, filter);
		
		//Show "Turing WiFi on" dialog
		dialog.show();
	}
	
	private void success() {
		setResult(RESULT_OK);
		finish();
	}
	
	private void fail() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	/**
	 * No button callback.
	 * 
	 * @param arg
	 */
	public void no (View arg) {
		fail();
	}

}
