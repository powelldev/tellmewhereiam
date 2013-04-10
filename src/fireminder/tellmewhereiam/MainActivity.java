package fireminder.tellmewhereiam;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fireminder.tellmewhereiam.location.LocationHelper;

public class MainActivity extends FragmentActivity {

	Button btnMapView;
	Button btnHybridView;
	Button btnSatView;
	ImageButton btnFindMe;
	ImageButton btnCpy;
	
	TextView tvLatLong;
	TextView tvAddress;
	
	LocationHelper locHelper;

	GoogleMap map;
	SupportMapFragment mMapFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		map = mMapFragment.getMap();
	
		
		findViewsById();
		SetupInit();
		if(locHelper.checkGpsStatus()){
			// launch prompt dialog
		}
		
		
	}
	
	@Override 
	protected void onPause(){
		super.onPause();
		locHelper.stopLocationHelper();
	}
	
	protected void onResume(Bundle b){
		super.onResume();
		locHelper.startLocationHelper();
	}


	private void setMapPosition(Location loc){
		if(map != null){
			map.clear();
			LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14));
			map.addMarker(new MarkerOptions()
					.position(latlng));
			map.addCircle(new CircleOptions()
					.center(latlng)
					.strokeWidth(2)
					.radius(loc.getAccuracy())
					.strokeColor(Color.BLUE));
		}
	}
	
	public OnClickListener copyOnClickListener = new OnClickListener(){
		public void onClick(View button){
			String address = tvAddress.getText().toString().substring(8);
			Toast.makeText(getApplicationContext(), "Address copied to clipboard", Toast.LENGTH_LONG).show();
			ClipboardManager cbm = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
			cbm.setText(address);
		}
	};
	public OnClickListener findMeOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View button){
			Location loc = null;
			if(locHelper.checkGpsStatus()){
				loc = locHelper.getGpsLocation();
			} else {
				loc = locHelper.getNetworkLocation();
			}
			setMapPosition(loc);
			setLatLongTextViews(loc);
			new PerformApiQuery().execute(constructQuery(loc.getLatitude(), loc.getLongitude()));
		}
	};
	public OnClickListener mapViewOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View button) {
			switch (button.getId()){
			case R.id.btnMapView:
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case R.id.btnHybridView:
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case R.id.btnSatView:
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			}
			
		}
		
	};
	public void findViewsById(){
		btnFindMe = (ImageButton) findViewById(R.id.btnFindMe);
		btnMapView = (Button) findViewById(R.id.btnMapView);
		btnHybridView = (Button) findViewById(R.id.btnHybridView);
		btnSatView = (Button) findViewById(R.id.btnSatView);
		btnCpy = (ImageButton) findViewById(R.id.btnCpy);
		
		tvLatLong = (TextView) findViewById(R.id.tvLatLong);
		tvAddress = (TextView) findViewById(R.id.tvAddress);
	}
	
	private void setAddressTextView(String address){
		tvAddress.setText("Address: " + address);
	}
	
	private void setLatLongTextViews(Location newLocation){
		
		if(newLocation != null){
			tvLatLong.setText("Latitude: " + newLocation.getLatitude() + "\n" +
								"Longitude: " + newLocation.getLongitude() + "\n" +
								"Accuracy: " + newLocation.getAccuracy() + " meters ");
			tvAddress.setText("Loading...");
		}
		else {
			Toast.makeText(getApplicationContext(), "GPS not enabled", Toast.LENGTH_SHORT).show();
		}
	}
	private void SetupInit(){	
		locHelper = new LocationHelper(getApplicationContext());
		btnMapView.setOnClickListener(mapViewOnClickListener);
		btnSatView.setOnClickListener(mapViewOnClickListener);
		btnHybridView.setOnClickListener(mapViewOnClickListener);
		btnFindMe.setOnClickListener(findMeOnClickListener);
		btnCpy.setOnClickListener(copyOnClickListener);
	}
	
	private URL constructQuery(double lat, double lng){
		String strUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true";
		URL url = null;
		try{
			url = new URL(strUrl);
		} catch (Exception e) {}
		return url;
	}
	private class PerformApiQuery extends AsyncTask<URL, Integer, String>{

		@Override
		protected String doInBackground(URL... params) {
			String addr = null;
			try{
				HttpURLConnection httpconn = (HttpURLConnection) params[0].openConnection();
				addr = parseJsonIntoStringAddress(httpconn.getInputStream());
			} catch(Exception e) {}
			return addr;
		}
		
		protected void onPostExecute(String result){
			setAddressTextView(result);
		}
		
		private String parseJsonIntoStringAddress(InputStream is){
			Scanner s = new Scanner(is).useDelimiter("\\A");
			String strResponse = s.hasNext() ? s.next() : "";
			JSONObject response;
			String addr = null;
			try{
				response = new JSONObject(strResponse);
				JSONArray array = response.getJSONArray("results");
				JSONObject obj = array.getJSONObject(0);
				addr = obj.getString("formatted_address");
				Log.d("addr", addr);
			}catch(Exception e){}
			return addr;
		}
		
	}
}
