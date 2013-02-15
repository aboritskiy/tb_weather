package ru.tonybo.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class CityWeatherRecord {
	private static final String DEBUG_TAG = "CityWeatherRecord";
	
	public static CityWeatherRecord fromJSON (JSONObject o) throws JSONException {
		double latitude = 0.0f;
		try {
			latitude = o.getJSONObject("coord").getDouble("lat");
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "Error: unable to init latitude value.");
		}
		
		double longitude = 0.0f;
		try {
			longitude = o.getJSONObject("coord").getDouble("lon");
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "Error: unable to init longitude value.");
		}
		
		double cloudCover = 0.0f;
		try {
			cloudCover = o.getJSONObject("clouds").getDouble("all");
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "Error: unable to init cloudCover value.");
		}
		
		double temperature = 0.0f;
		try {
			temperature = o.getJSONObject("main").getDouble("temp");
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "Error: unable to init temperature value.");
		}
		
		double pressure = 0.0f;
		try {
			pressure = o.getJSONObject("main").getDouble("pressure");
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "Error: unable to init pressure value.");
		}
		
		return new CityWeatherRecord(
				latitude,
					longitude,
					o.getString("name"),
					o.getLong("id"),
					cloudCover,
					o.getDouble("distance"),
					temperature,
					pressure
				);
	}
	
	private final double latitude;
	private final double longitude;
	private final String name;
	private final long id;
	private final double cloudCover;
	private final double distance;
	private final double temperature;
	private final double pressure;
	
	private CityWeatherRecord() {
		this.latitude = 0.0;
		this.longitude = 0.0;
		this.name = "";
		this.id = 0l;
		this.cloudCover = 0.0;
		this.distance = 0.0;
		this.temperature = 0.0;
		this.pressure = 0.0;
	}
	
	private CityWeatherRecord(
			double latitude,
			double longitude,
			String name,
			long id,
			double cloudCover,
			double distance,
			double temperature,
			double pressure
		) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.id = id;
		this.cloudCover = cloudCover;
		this.distance = distance;
		this.temperature = temperature;
		this.pressure = pressure;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public String getName() {
		return name;
	}
	
	public long getId() {
		return id;
	}
	
	public double getCloudCover() {
		return cloudCover;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getTemperature() {
		return temperature;
	}
	
	public String getStringTemperatureInCentigrade() {
		String string = Double.toString(temperature - 273.0);
		
		return string.substring(0, string.lastIndexOf("."));
	}
	
	public double getPressure() {
		return pressure;
	}
	
	@Override
	public String toString() {
		return "[CityWeatherRecord: " + name + " " + id + " at lat:" + latitude + ", lnt: " + longitude + "]"; 
	}
	
	@Override
	public boolean equals (Object o) {
		if (!(o instanceof CityWeatherRecord )) {
			return false;
		}
		
		CityWeatherRecord cwr = (CityWeatherRecord)o;
		
		if (cwr.latitude != latitude) {
			return false;
		}
		
		if (cwr.longitude != longitude) {
			return false;
		}
		
		if (!cwr.name.equals(name)) {
			return false;
		}
		
		if (cwr.id != id) {
			return false;
		}
		
		if (cwr.cloudCover != cloudCover) {
			return false;
		}
		
		if (cwr.distance != distance) {
			return false;
		}
		
		if (cwr.temperature != temperature) {
			return false;
		}
		
		if (cwr.pressure != pressure) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode () {
		int result = 17;
		
		long tmp = Double.doubleToLongBits(latitude);
		result = 31 * result + (int)(tmp ^ (tmp >>> 32));
		
		tmp = Double.doubleToLongBits(longitude);
		result = 31 * result + (int)(tmp ^ (tmp >>> 32));
		
		result = 31 * result + name.hashCode();
		result = 31 * result + (int)(id ^ (id >>> 32));
		
		tmp = Double.doubleToLongBits(cloudCover);
		result = 31 * result + (int)(tmp ^ (tmp >>> 32));
		
		tmp = Double.doubleToLongBits(distance);
		result = 31 * result + (int)(tmp ^ (tmp >>> 32));
		
		tmp = Double.doubleToLongBits(temperature);
		result = 31 * result + (int)(tmp ^ (tmp >>> 32));
		
		tmp = Double.doubleToLongBits(pressure);
		result = 31 * result + (int)(tmp ^ (tmp >>> 32));
		
		return result;
	}
}
