package ru.tonybo.Weather;

import java.util.Iterator;
import java.util.List;

import ru.tonybo.Weather.GetWeatherData;

import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
	
	private static final String DEBUG_TAG = "MainActivity";
	private static final String AREA_OF_INTEREST = "areaOfInterest";
	private static final String AREA_OF_INTEREST_SB_PROGRESS = "areaOfInterestSBProgress";
	private static final double DEFAULT_AREA_OF_INTEREST_RADIUS_POWER = 1.0;
	private static final double MIN_AREA_OF_INTEREST_RADIUS_POWER = -1.0;
	private static final double MAX_AREA_OF_INTEREST_RADIUS_POWER = 3.0;
	
	private LocationController locationController;
	
	private TextView latitudeTextView;
	private TextView longitudeTextView;
	
	private SeekBar areaOfInterestRadiusSeekBar;
	private TextView areaOfInterestRadiusTextView;
	
	private List<CityWeatherRecord> weatherData;
	
	private double areaOfInterest = Math.pow(10.0, DEFAULT_AREA_OF_INTEREST_RADIUS_POWER);
	
	private boolean locationReady = false;
	private Location currentLocation;
	
	private TableRow tableHeader;
	
	private final Handler locationChangeHandler = new Handler(){
	    @Override
	    public void handleMessage(Message m) {
	        switch(m.what){
	            case LocationController.UPDATE_LATLNG:
	            	locationReady = true;
	            	currentLocation = (Location)m.obj;
	            	latitudeTextView.setText(Double.toString(currentLocation.getLatitude()));
	            	longitudeTextView.setText(Double.toString(currentLocation.getLongitude()));
	            	requestWeatherData();
	            	break;
	            case GetWeatherData.WEATHER_DATA_ARRIVED:
	            	Log.d(DEBUG_TAG, "huraah!!!");
            		weatherData = (List<CityWeatherRecord>)m.obj;
	            	renderWeatherData();
	            	break;
	            case GetWeatherData.WEATHER_DATA_REQUEST_ERROR:
	            	Log.d(DEBUG_TAG, "ooops!");
	            	weatherData = null;
	            	renderWeatherData();
	            	UnableToGetWeatherDataDialogFragment alert = new UnableToGetWeatherDataDialogFragment();
	        		alert.show(getFragmentManager(),"alert");
	            	break;
	        }
	        super.handleMessage(m);
	    }
	};
	
	private final class UnableToGetWeatherDataDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.unable_to_load_weather_data)
                   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   // nop?
                       }
                   });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        latitudeTextView = (TextView) findViewById(R.id.location_latitude);
        longitudeTextView = (TextView) findViewById(R.id.location_longitude);
        
        areaOfInterestRadiusSeekBar = (SeekBar) findViewById(R.id.area_of_interest_radius_sb);
        areaOfInterestRadiusSeekBar.setOnSeekBarChangeListener(this);
        areaOfInterestRadiusTextView = (TextView) findViewById(R.id.area_of_interest_radius_tv);
        
        if (savedInstanceState != null) {
        	areaOfInterest = savedInstanceState.getDouble(AREA_OF_INTEREST);
        	areaOfInterestRadiusSeekBar.setProgress(savedInstanceState.getInt(AREA_OF_INTEREST_SB_PROGRESS));
        }
        
        if (locationController == null) {
	        locationController = new LocationController(locationChangeHandler, this);
        } else {
        	locationController.removeLocationListeners();
        }
    }
    
    @Override
    public void onStart() {
    	latitudeTextView.setText(R.string.pending);
    	longitudeTextView.setText("");
    	locationReady = false;
    	
    	updateAreaOfInterest();
    	
    	locationController.addLocationListeners();
    	super.onStart();
    }
    
    @Override
    public void onStop() {
    	locationController.removeLocationListeners();
    	super.onStop();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble(AREA_OF_INTEREST, areaOfInterest);
        savedInstanceState.putInt(AREA_OF_INTEREST_SB_PROGRESS, areaOfInterestRadiusSeekBar.getProgress());
        
        super.onSaveInstanceState(savedInstanceState);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if (seekBar.equals(areaOfInterestRadiusSeekBar)) {
			updateAreaOfInterest();
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar.equals(areaOfInterestRadiusSeekBar)) {
			updateAreaOfInterest();
			
			requestWeatherData();
		}
	}
	
	private void updateAreaOfInterest () {
		areaOfInterest = Math.round ( 
				Math.pow(
						10.0, 
						((double)areaOfInterestRadiusSeekBar.getProgress() / (double)areaOfInterestRadiusSeekBar.getMax() ) * (MAX_AREA_OF_INTEREST_RADIUS_POWER - MIN_AREA_OF_INTEREST_RADIUS_POWER) + MIN_AREA_OF_INTEREST_RADIUS_POWER
					)*10.0
				) / 10.0;
		areaOfInterestRadiusTextView.setText(Double.toString(areaOfInterest));
	}

	private void requestWeatherData() {
		if (locationReady) {
			showMessageInsideTheTable(getString(R.string.pending));
			new GetWeatherData(locationChangeHandler, currentLocation, areaOfInterest).execute();
		}
	}
	
	private void showMessageInsideTheTable (String message) {
		TableLayout weatherDataTable = (TableLayout) findViewById(R.id.weather_data_table);
		weatherDataTable.removeAllViewsInLayout();
		
		TableRow tr=new TableRow(this);
        tr.setLayoutParams( new TableRow.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );
        tr.setPadding(0, 16, 0, 16);
        
        TextView tw = new TextView(this);
        tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
        tw.setGravity(Gravity.CENTER);
        tw.setText(message);
        tr.addView(tw);
        weatherDataTable.addView(tr);
	}
	
	private void renderWeatherData () {
		TableLayout weatherDataTable = (TableLayout) findViewById(R.id.weather_data_table);
		weatherDataTable.removeAllViewsInLayout();
		
		if (weatherData == null)
			return;
		
		if (tableHeader == null) {
			tableHeader = new TableRow(this);
			tableHeader.setLayoutParams( new TableRow.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );
			tableHeader.setPadding(0, 16, 0, 16);
			tableHeader.setBackgroundColor(Color.rgb(200, 200, 255));
			
			TextView tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(R.string.city_name);
            tableHeader.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(R.string.distance_to_city);
            tableHeader.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(R.string.city_temperature);
            tableHeader.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(R.string.city_pressure);
            tableHeader.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(R.string.city_cloud_cover);
            tableHeader.addView(tw);
		}
		
		weatherDataTable.addView(tableHeader);
		
		
		Iterator<CityWeatherRecord> it = weatherData.iterator();
		while (it.hasNext()) {
			CityWeatherRecord cwr = it.next();
			
			TableRow tr=new TableRow(this);
            tr.setLayoutParams( new TableRow.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );
            tr.setPadding(0, 16, 0, 16);
            
            TextView tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(cwr.getName());
            tr.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(Double.toString(cwr.getDistance()));
            tr.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(cwr.getStringTemperatureInCentigrade());
            tr.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(Double.toString(cwr.getPressure()));
            tr.addView(tw);
            
            tw = new TextView(this);
            tw.setLayoutParams( new TableRow.LayoutParams( 0, LayoutParams.WRAP_CONTENT, 1f ) );
            tw.setText(Double.toString(cwr.getCloudCover()));
            tr.addView(tw);
            
            weatherDataTable.addView(tr);
            
            // add line below each row   
            View vline1 = new View(this);
            vline1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            vline1.setBackgroundColor(Color.BLACK);
            weatherDataTable.addView(vline1);  
		}
	}
}
