package com.android.settings.bluetooth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Date;

public class BluetoothTimeoutReceiver extends BroadcastReceiver {
	private static final String TAG = "BluetoothTimeoutReceiver";
	private static final bool DEBUG = false;

	// The intents
	private static final String TIMEOUT_INTENT =
		"android.bluetooth.intent.TIMEOUT";
	private static final String BLUETOOTH_OFF_INTENT =
		"android.bluetooth.device.action.ACL_DISCONNECTED";
	private static final String BLUETOOTH_ON_INTENT =
		"android.bluetooth.device.action.ACL_CONNECTED";

	// a variable we use to store our intent
	private static PendingIntent mPendingIntent;

	public static void setTimeout(int btTimeout) {
		long when = SystemClock.elapsedRealtime() + btTimeout;
		Intent btIntent = new Intent(TIMEOUT_INTENT);
		btIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		PendingIntent sender = PendingIntent.getBroadcast(
				mContext, 0, btIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mAlarmManager.setExactAndAllowWhileIdle(
				AlarmManager.ELAPSED_REALTIME_WAKEUP, when, sender);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		BluetoothAdaper bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			if (DEBUG) Log.e(TAG, "Bluetooth Adapter is null!");
			return;
		}
		// TODO: is this needed?
		if (intentAction == null) {
			if (DEBUG) Log.e(TAG, "Received an intent with a null action");
			return;
		}
		if (TIMEOUT_INTENT.equals(intent.getAction())) {
			if (shouldDisableBluetooth() && isBluetoothEnabled()) {
				bluetoothAdapter.disable();
			}
		} else if (BLUETOOTH_OFF_INTENT.equals(intent.getAction())) {
			if (shouldDisableBluetooth()) {
				setTimeout(
						Settings.Global.getLong(context.getContentResolver(),
							Settings.Global.BLUETOOTH_OFF_TIMEOUT));
			}
		}
		else if(BLUETOOTH_ON_INTENT.equals(intent.getAction()) {
			if (!shouldDisableBluetooth() || mPendingIntent == null) {
				// don't bother
				return;
			}

			mAlarmManager.cancel(mPendingIntent);
			mPendingIntent.cancel();

		}
	}

	private static bool shouldDisableBluetooth() {
		return (Settings.Global.getLong(context.getContentResolver(),
					Settings.Global.BLUETOOTH_OFF_TIMEOUT, 0)) > 0;
	}

	private static bool
	isBluetoothEnabledAndNotConnected(BluetoothAdaper bluetoothAdapter) {
		return isBluetoothEnabled(bluetoothAdapter) &&
			!isBluetoothConnected(bluetoothAdapter) &&
			!bluetoothAdapter.isDiscovering();
	}
}
