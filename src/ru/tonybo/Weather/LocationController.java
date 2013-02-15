package ru.tonybo.Weather;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

public class LocationController {
	private final LocationListener locationListener = new LocationListener() {

	    @Override
	    public void onLocationChanged(Location location) {
	        /*Message.obtain(
		        		locationChangeHandler,
		        		UPDATE_LATLNG,
		                location
	        		).sendToTarget();*/
	    	checkLocation(location);
	    }

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	
	private final Handler locationChangeHandler;
	private final Activity linkedActivity;
	private LocationManager locationManager;
	private List<String> providersList;
	
	public static final int UPDATE_LATLNG = 0x01;
	private static final String DEBUG_TAG = "LocationController";
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	private Location currentLocation;
	
	public LocationController (Handler locationChangeHandler, Activity linkedActivity) {
		this.locationChangeHandler = locationChangeHandler;
		this.linkedActivity = linkedActivity;
		
		locationManager = (LocationManager) linkedActivity.getSystemService(Context.LOCATION_SERVICE);
        providersList = locationManager.getAllProviders();
        
        currentLocation = null;
        Iterator<String> iterator = providersList.iterator();
    	while (iterator.hasNext()) {
    		checkLocation(locationManager.getLastKnownLocation(iterator.next()));
    	}
	}
	
	public void addLocationListeners() {
    	boolean allDisabled = true;
    	
    	Iterator<String> iterator = providersList.iterator();
    	while (iterator.hasNext()) {
    		String lpName = iterator.next();
    		if (lpName.equals(LocationManager.PASSIVE_PROVIDER)){
    			continue;
    		}
    		Log.d(DEBUG_TAG, lpName + " enabled: " + locationManager.isProviderEnabled(lpName));
    		if (locationManager.isProviderEnabled(lpName)) {
    			allDisabled = false;
    			locationManager.requestLocationUpdates(
        				lpName,
            	        10000,          // 10-second interval.
            	        10,             // 10 meters.
            	        locationListener
            	);
    		}
    	}
    	
    	if (allDisabled) {
    		Log.d(DEBUG_TAG, "all GPS providers are disabled");
    		EnableGPSDialogFragment alert = new EnableGPSDialogFragment();
    		alert.show(linkedActivity.getFragmentManager(),"alert");
    	}
    }
	
	public void removeLocationListeners() {
    	locationManager.removeUpdates(locationListener);
    }
	
	private void checkLocation(Location location) {
    	if (isBetterLocation(location, currentLocation)) {
			currentLocation = location;
			
			Log.d(DEBUG_TAG, "new good location: " + currentLocation);
			if (currentLocation.hasAccuracy() && (currentLocation.getAccuracy() < 1000)) {
				Message.obtain(
		        		locationChangeHandler,
		        		UPDATE_LATLNG,
		                location
	        		).sendToTarget();
			}
		}
    }
    
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
    	if (location == null) {
        	// a new null location is always worse than any existing location
        	return false;
        }
    	
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
    private void enableGPSFacilities() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        linkedActivity.startActivity(settingsIntent);
    }
    
    private final class EnableGPSDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.enable_gps)
                   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   enableGPSFacilities();
                       }
                   });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}