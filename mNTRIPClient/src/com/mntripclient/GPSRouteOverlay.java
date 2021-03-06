package com.mntripclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

@SuppressLint("ParserError")
public class GPSRouteOverlay extends Overlay {

	private ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	private MapView mapView;

	Projection projection;
	Point startPoint;
	Point stopPoint;
	Paint paint;

	public GPSRouteOverlay(MapView mapView) {
		this.mapView = mapView;
		mapView.invalidate();
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.MAGENTA);
		paint.setStrokeWidth(4);
		paint.setAlpha(200);

		startPoint = new Point();
		stopPoint = new Point();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if ((!shadow) && (geoPoints.size() > 1)) {

			projection = mapView.getProjection();

			for (int i = 0; i < (geoPoints.size() - 1); i++) {
				projection.toPixels(geoPoints.get(i), startPoint);
				projection.toPixels(geoPoints.get(i + 1), stopPoint);
				canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
						stopPoint.y, paint);
			}
		}
	}

	public void addGeoPoint(GeoPoint geoPoint) {
		geoPoints.add(geoPoint);
		mapView.invalidate();
	}

	public void readPointsFromFile(String fileName) {
		geoPoints.clear();
		try {
			FileInputStream fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName);
			StringBuffer buffer = new StringBuffer("");

			int c;
			while ((c = fileInputStream.read()) != -1) {
				buffer.append((char) c);
			}
			
			fileInputStream.close();
			
			for(String s : buffer.toString().split("\n")) {
				int lat = (int) (Float.parseFloat(s.split(" ")[0]) * 1000000.0);
				int lon = (int) (Float.parseFloat(s.split(" ")[1]) * 1000000.0);
				geoPoints.add(new GeoPoint(lat, lon));
			}

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		mapView.invalidate();
	}

	public void clear() {
		geoPoints.clear();
		mapView.invalidate();
	}

}
