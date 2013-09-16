/*
 * Promenade au Thabor
 * Copyright (C) 2011 40degree (Marc Haussaire & Fabien Ric)
 *
 * http://www.40degree.com
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package com.fortydegree.ra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Toast;

import com.fortydegree.ra.gui.AugmentedView;
import com.fortydegree.ra.gui.CameraSurface;
import com.fortydegree.ra.model.Marker;
import com.fortydegree.ra.model.SavedState;

public class RAView extends Activity {

	private static final String TAG = RAView.class.getName();
	private static final int REQUEST_LOCATION_SETTINGS = 10;
	private static final int MENU_ITEM_ABOUT = 1;
	protected CameraSurface camScreen;
	protected AugmentedView augScreen;
	protected RAContext model;
	protected WakeLock mWakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			model = new RAContext();
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			camScreen = new CameraSurface(this);
			augScreen = new AugmentedView(this, model);
			setContentView(camScreen);

			addContentView(augScreen, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			Object o = this.getLastNonConfigurationInstance();
			if (o != null) {
				SavedState savedState = (SavedState) o;
				model.getFinder().setInfo(savedState.findInfo);
				model.getFinder().setMarkers(savedState.markers);
				model.getSensor().setLocation(savedState.me);
			}

		} catch (Throwable ex) {
			handleError(ex);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (model != null) {
			model.pause((SensorManager) getSystemService(Context.SENSOR_SERVICE),
					(LocationManager) getSystemService(Context.LOCATION_SERVICE));
		}

		if (this.mWakeLock != null) {
			this.mWakeLock.release();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		model.close();

	}

	@Override
	protected void onStart() {
		super.onStart();

		if (this.mWakeLock == null) {
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
					TAG);
		}

		if (this.mWakeLock != null)
			this.mWakeLock.acquire();
	}

	@Override
	protected void onResume() {
		super.onResume();

		initModel();
	}

	protected void initModel() {
		if (model != null) {
			model.init((SensorManager) getSystemService(Context.SENSOR_SERVICE),
					(LocationManager) getSystemService(Context.LOCATION_SERVICE));
			augScreen.setModel(model);

			if (!model.getSensor().isGpsEnabled()) {
				if (model.getSensor().hasGps()) {
					showGpsAlert();
				} else {
					showNoGpsInfo();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		createMenuItem(menu, MENU_ITEM_ABOUT, getResources().getString(R.string.about),
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	private static MenuItem createMenuItem(Menu menu, int index, String name, int iconIndex) {
		MenuItem item = menu.add(Menu.FIRST, index, index, name);
		item.setIcon(iconIndex);
		return item;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case MENU_ITEM_ABOUT:
			showAlert(R.string.about, R.string.aboutBoxMessage, R.string.close);
			break;

		}
		return true;
	}

	private void showAlert(int titleResourceId, int msgResourceId, int buttonResourceId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(msgResourceId));

		builder.setNegativeButton(getResources().getString(buttonResourceId), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.setTitle(getResources().getString(titleResourceId));
		alert.show();
	}

	private void showNoGpsInfo() {
		showAlert(R.string.gpsMissing, R.string.gpsMissingAlert, R.string.cont);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (augScreen.isInfoPanelClicked(event.getX(), event.getY())) {
					Marker m = model.getFinder().getTheGoodPlace();
					if (m != null)
						launchInfoActivity(m);
				}
			}

			return true;
		} catch (Exception ex) {
			handleError(ex);
			return super.onTouchEvent(event);
		}
	}

	private void launchInfoActivity(Marker m) {
		Intent in = new Intent(this, InfoActivity.class);
		for (String s : m.getDataKeys())
			in.putExtra(s, m.getData(s));

		this.startActivity(in);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			Toast.makeText(this, getResources().getString(R.string.recalibrate), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		SavedState state = new SavedState();
		state.markers = model.getFinder().getMarkers();
		state.me = new Location(model.getSensor().getLocation());
		state.findInfo = model.getFinder().getInfo();
		return state;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOCATION_SETTINGS && resultCode == 0) {
			initModel();
		}
	}

	public void handleError(Throwable t) {
		String error = t.getMessage();
		Log.e(getPackageName(), error, t);

		showErrorDialog(error);

		try {
			augScreen.invalidate();
		} catch (Exception e) {
			Log.e(getPackageName(), error, e);
		}
	}

	protected void showErrorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(false);

		/* Close application */
		builder.setPositiveButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void showGpsAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.appName));
		builder.setMessage(getResources().getString(R.string.gpsUnavailableAlert)).setCancelable(false);

		/* Open settings */
		builder.setPositiveButton(getResources().getString(R.string.gpsActivate),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent locSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

						try {
							startActivityForResult(locSettings, REQUEST_LOCATION_SETTINGS);
						} catch (ActivityNotFoundException e) {
							showErrorDialog(getResources().getString(R.string.gpsUnavailable));
						}
					}
				});
		/* Close application */
		builder.setNegativeButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
