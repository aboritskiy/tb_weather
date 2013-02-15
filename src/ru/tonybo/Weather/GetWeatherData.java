package ru.tonybo.Weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 *
 * @author TonyBo
 * class implement data retrieval from remote data source.
 * 
 *
 */
public final class GetWeatherData extends AsyncTask<Void, Void, List<CityWeatherRecord>> {
	private static final String DEBUG_TAG = "GetWheatherData";
	private static final String DEFAULT_SERVICE_URL = "http://api.openweathermap.org/data/2.1/find/city?";
	
	public static final int WEATHER_DATA_ARRIVED = 0x100;
	public static final int WEATHER_DATA_REQUEST_ERROR = 0x101;
	
	
	private Handler handler;
	private Location location;
	private double distance;
	
	public GetWeatherData (Handler handler, Location location, double distance) {
		this.handler= handler;  
		this.location = location;
		this.distance = distance;
	}

	@Override
	protected List<CityWeatherRecord> doInBackground(Void... params) {
		Log.d(DEBUG_TAG, "requestDataForLocation");
		
		List<NameValuePair> requestParams = new LinkedList<NameValuePair>();
	    /**
	     * from http://openweathermap.org/wiki/API/JSON_API
	     * lat	 		latitude
		 * lon	 		longitude
		 * radius 		radius in kilometers
		 * callback	 	functionName for JSONP calback. http://en.wikipedia.org/wiki/JSONP
		 * cluster	 	Use server clustering of points. Possible values ​​are [yes, no]
		 * lang	 		Language [ru, en, de, fr, es, it] if is posible
	     */
	    requestParams.add(new BasicNameValuePair("lat", String.valueOf(location.getLatitude())));
	    requestParams.add(new BasicNameValuePair("lon", String.valueOf(location.getLongitude())));
	    requestParams.add(new BasicNameValuePair("radius", String.valueOf(distance)));
	    requestParams.add(new BasicNameValuePair("lang", "en"));
		
		HttpClient client = new DefaultHttpClient();
	    HttpGet request = new HttpGet(DEFAULT_SERVICE_URL + URLEncodedUtils.format(requestParams, "utf-8"));

	    HttpResponse response=null;
	    try {
	        response = client.execute(request);
	    } catch (ClientProtocolException e) {
	        Log.d(DEBUG_TAG,"ClientProtocolException: "+e.toString());
	        return null;

	    } catch (IOException e) {
	        Log.d(DEBUG_TAG,"IOException: "+e.toString());
	        return null;

	    }
	    int status_code=response.getStatusLine().getStatusCode();
	    Log.d(DEBUG_TAG,"Response Code returned ="+status_code);

	    BufferedReader in=null;
	    try {
	        in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	    } catch (IllegalStateException e) {
	        Log.d(DEBUG_TAG,"3. "+e.toString());
	        return null;
	    } catch (IOException e) {
	        Log.d(DEBUG_TAG,"4. "+e.toString());
	        return null;
	    }

	    StringBuffer sb = new StringBuffer("");
	    String line = "";
	    String newline = System.getProperty("line.separator");
	    try {
	        while ((line = in.readLine()) !=null){
	            sb.append(line + newline);
	        }
	    } catch (IOException e) {
	        Log.d(DEBUG_TAG,"5. "+e.toString());
	        return null;
	    }
	    try {
	        in.close();
	    } catch (IOException e) {
	        Log.d(DEBUG_TAG,"6. "+e.toString());
	        return null;
	    }
	    String data = sb.toString();

	    try {
	        if(status_code==200 || status_code==401){
	            JSONObject responseJSONObject = new JSONObject(data);
	            
	            List<CityWeatherRecord> list = new ArrayList<CityWeatherRecord>(responseJSONObject.getInt("cnt"));
	            
	            JSONArray array = responseJSONObject.getJSONArray("list");
	            for(int i = 0 ; i < array.length() ; i++){
	                list.add(CityWeatherRecord.fromJSON(array.getJSONObject(i)));
	            }
	            
	            return list;
	        }
	    } catch (JSONException e) {
	        Log.d(DEBUG_TAG,"JSONException "+e.toString());
	    }
	    
	    return null;
	}
	
	@Override
	protected void onPostExecute(List<CityWeatherRecord> result) {
		if (result == null) {
			Message.obtain(
	        		handler,
	        		WEATHER_DATA_REQUEST_ERROR,
	                result
        		).sendToTarget();
		} else {
			Message.obtain(
	        		handler,
	        		WEATHER_DATA_ARRIVED,
	                result
        		).sendToTarget();
		}
	}
	
}