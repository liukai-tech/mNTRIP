package com.mntripclient;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends MapActivity {

	private GPSOPointOverlay gpsPhoneOverlay;
	private GPSOPointOverlay gpsBluetoothOverlay;
	private GPSOPointOverlay dgpsBluetoothOverlay;
	private GPSOPointOverlay dgpsBluetoothBufferedOverlay;
	
	private GPSRouteOverlay gpsRouteOverlay;

	private LocationManager locationManager;
	 
	private List<Overlay> mapOverlays;
	private MapView mapView;

	private ArrayBlockingQueue<byte []> httpBluetoothQueue;
	private ArrayBlockingQueue<byte []> bluetoothCoordinatesQueue;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mapview);

		prepareMapView();
		prepareOverlays();
		addOverlays();

		subcribeLocationManager();
		
		httpBluetoothQueue = new ArrayBlockingQueue<byte[]>(100);
		bluetoothCoordinatesQueue = new ArrayBlockingQueue<byte[]>(100);
		
		new Thread(FileLog.getInstance()).start();
		new Thread(new HTTPTask(httpBluetoothQueue)).start();
		new Thread(new BluetoothTask(httpBluetoothQueue, bluetoothCoordinatesQueue)).start();
		new Thread(new CoordinatesTask(bluetoothCoordinatesQueue, gpsBluetoothOverlay, dgpsBluetoothOverlay, dgpsBluetoothBufferedOverlay)).start();
		new UpdateMapTask().execute(null, null, null);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
	}

	private void prepareMapView() {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setSatellite(true);
		mapView.setBuiltInZoomControls(true);
	}

	private void prepareOverlays() {
		mapOverlays = mapView.getOverlays();
		gpsPhoneOverlay = new GPSOPointOverlay(this.getResources().getDrawable(R.drawable.redpoint));
		gpsBluetoothOverlay = new GPSOPointOverlay(this.getResources().getDrawable(R.drawable.greenpoint));
		dgpsBluetoothOverlay = new GPSOPointOverlay(this.getResources().getDrawable(R.drawable.bluepoint));
		dgpsBluetoothBufferedOverlay = new GPSOPointOverlay(this.getResources().getDrawable(R.drawable.yellowpoint));
		gpsRouteOverlay = new GPSRouteOverlay(mapView);
	}

	private void subcribeLocationManager() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new GPSPhoneListener(gpsPhoneOverlay));
	}

	private void addOverlays() {
		mapOverlays.add(gpsRouteOverlay);
		mapOverlays.add(gpsPhoneOverlay);
		mapOverlays.add(gpsBluetoothOverlay);
		mapOverlays.add(dgpsBluetoothOverlay);
		mapOverlays.add(dgpsBluetoothBufferedOverlay);

		gpsRouteOverlay.readPointsFromFile("mNTRIPClientRoute.txt");
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private class UpdateMapTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
						
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mapView.invalidate();
			new UpdateMapTask().execute(null, null, null);
		}
		
	}
	
}
