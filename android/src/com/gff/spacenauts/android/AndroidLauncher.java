package com.gff.spacenauts.android;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gff.spacenauts.Spacenauts;
import com.gff.spacenauts.android.net.WifiP2PNetworkAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AndroidLauncher extends AndroidApplication {
	
	private WifiP2PNetworkAdapter adapter;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Set immersive mode
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.hideStatusBar = true;
		config.useImmersiveMode = true;
		
		//Init NetworkAdapter
		adapter = new WifiP2PNetworkAdapter(this);
		
		initialize(new Spacenauts(adapter), config);
	}
	
	/**
	 * Callback for activity result from Wifi Enable request.
	 */
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == WifiP2PNetworkAdapter.ENABLE_WIFI) {
			if (resultCode == Activity.RESULT_CANCELED) adapter.wifiRefused();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (adapter != null) adapter.unregisterReceiver();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) adapter.registerReceiver();
	}
}
