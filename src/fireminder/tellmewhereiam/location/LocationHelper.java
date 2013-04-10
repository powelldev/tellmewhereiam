package fireminder.tellmewhereiam.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationHelper {
	
	private Location location;
	private LocationManager lm;
	private LocationListener ll;
	
	public LocationHelper(final Context context){
		
		location = null;
		ll = new LocationListener(){

			@Override
			public void onLocationChanged(Location arg0) {
				location = arg0;
				Toast.makeText(context, "Location Updated", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 10, ll);
		location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
	
	public boolean checkGpsStatus(){
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	public Location getLocation(){
		
		return location;
	}
	public Location getGpsLocation(){
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 10, ll);
		return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	public Location getNetworkLocation(){
		return lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
	public void stopLocationHelper(){
		lm.removeUpdates(ll);
	}
	
	public void startLocationHelper(){
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 10, ll);
	}

}
