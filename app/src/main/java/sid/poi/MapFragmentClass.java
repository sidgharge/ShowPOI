package sid.poi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by $URAJ on 19-Jul-17.
 */

public class MapFragmentClass extends Fragment implements OnMapReadyCallback {

    LatLngBounds bounds;
    GoogleMap googleMap;
    final static String URL = "http://dev.citrans.net:8888/skymeet/poi/list";
    JSONArray json;
    HttpClient client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_fragment_layout, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapid);
        mapFragment.getMapAsync(this);

        client = new DefaultHttpClient();

        new HttpTask().execute("location");

        return v;
    }

    public JSONArray lastTweet() throws ClientProtocolException, IOException, JSONException {
        StringBuilder url = new StringBuilder(URL);
        //url.append(username);

        HttpGet get = new HttpGet(url.toString());
        HttpResponse r = client.execute(get);
        int status = r.getStatusLine().getStatusCode();

        //if (status == 200){
        org.apache.http.HttpEntity e = r.getEntity();
        String data = EntityUtils.toString(e);
        JSONArray timeline = new JSONArray(data);
        return timeline;

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        new HttpTask().execute();

    }

    private class HttpTask extends AsyncTask<String , Integer , JSONArray> {


        @Override
        protected void onPostExecute(JSONArray ja) {

            JSONObject jo = new JSONObject();
            double f1 = 0.0;
            double f2 = 0.0;
            String d = "";

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < ja.length() - 2; i++){
                try {
                    jo = ja.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String s =jo.getString("location");
                    d = jo.getString("title");
                    f1 = Double.parseDouble((s.substring(1 , (s.indexOf(",")))));
                    f2 = Double.parseDouble((s.substring((s.indexOf(","))+1 , s.indexOf("]"))));;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LatLng loc = new LatLng(f1,f2);
                Log.d("location" , String.valueOf(f1) + "\t" + String.valueOf(f1));
                googleMap.addMarker(new MarkerOptions().position(loc).title(d)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_android_black_36dp)));

                builder.include(loc);
            }

            bounds = builder.build();

            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
                }
            });
        }


        @Override
        protected JSONArray doInBackground(String... strings) {
            try {
                json = lastTweet();
                return json;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
