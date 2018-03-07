package fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valentin.restfullocation.MainActivity;
import com.example.valentin.restfullocation.R;

import java.util.ArrayList;
import java.util.List;

import interfaces.UserOperations;
import models.ApiError;
import models.Site;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utilities.SessionPrefs;
import utilities.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CheckFragment extends Fragment implements View.OnClickListener{

    private ProgressBar progressBar;
    private TextView lat;
    private TextView lng;
    private TextView results;
    private Spinner spinner;
    private Button btnCheck;
    private Retrofit retrofit;
    private UserOperations userOperations;
    private SharedPreferences mPrefs;
    private double[] siteLocation = new double[2];
    private double[] OwnLocation = new double[2];
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    LocationManager locationManager;

    public CheckFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_check,container,false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Check in/out");

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        lat = (TextView)v.findViewById(R.id.tvLat);
        lng = (TextView)v.findViewById(R.id.tvLng);
        results = (TextView)v.findViewById(R.id.tvResults);
        spinner = (Spinner)v.findViewById(R.id.spinner);
        btnCheck = (Button)v.findViewById(R.id.btnCheck);

        retrofit = new Retrofit.Builder()
                .baseUrl(UserOperations.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userOperations = retrofit.create(UserOperations.class);
        mPrefs = ((MainActivity)getActivity())
                .getSharedPreferences(SessionPrefs.PREFS_NAME, Context.MODE_PRIVATE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("Coordenadas",Utils.sitios.get(position).getLat()+","+Utils.sitios.get(position).getLng());
                siteLocation[0] = Double.parseDouble(Utils.sitios.get(position).getLat());
                siteLocation[1] = Double.parseDouble(Utils.sitios.get(position).getLng());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnCheck.setOnClickListener(this);

        executeSitesViewSpinner();

        return v;
    }

    private void executeSitesViewSpinner() {

        if (!Utils.isOnline(((MainActivity)getActivity()))) {
            Toast.makeText(((MainActivity)getActivity()),"No hay conexión de red",Toast.LENGTH_LONG).show();
            return;
        }

        Call<ArrayList<Site>> siteCall = userOperations.getSites("Bearer "+mPrefs.getString(SessionPrefs.PREF_USER_TOKEN,""),"application/json");
        siteCall.enqueue(new Callback<ArrayList<Site>>() {
            @Override
            public void onResponse(Call<ArrayList<Site>> call, Response<ArrayList<Site>> response) {

                try{
                    if (!response.isSuccessful())
                    {
                        String error;
                        if (response.errorBody().contentType().subtype().equals("application/json"))
                        {
                            ApiError apiError = ApiError.fromResponseBody(response.errorBody());
                            error = apiError.getMessage();
                            Log.d("CheckFragmentA", apiError.getDeveloperMessage());
                        }else{
                            error = response.message();
                        }

                        Toast.makeText(((MainActivity)getActivity()),"Error: "+error,Toast.LENGTH_LONG).show();
                        return;
                    }else{
                        Utils.sitios = response.body();
                        String[] sites = new String[Utils.sitios.size()];
                        int position=0;
                        for (Site site : Utils.sitios){
                            sites[position] = site.getSite();
                            position++;
                        }
                        spinner.setAdapter(new ArrayAdapter<String>(((MainActivity)getActivity()), android.R.layout.simple_spinner_item, sites));
                        //Log.d("CheckFragmentR", Utils.sitios.get(0).getSite());
                    }
                }catch(Exception e){
                    Log.d("Error catch",e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Site>> call, Throwable t) {
                Toast.makeText(((MainActivity)getActivity()),"Fallo: "+t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCheck:
                getLocationUser();
                break;
            default:
                break;
        }
    }

    private void getLocationUser() {
        if(siteLocation[0] > 0){
            if (ContextCompat.checkSelfPermission(((MainActivity)getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(((MainActivity)getActivity()), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(((MainActivity)getActivity()),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            }else{
                if (!checkLocation())
                    return;
                progressBar.setVisibility(View.VISIBLE);
                results.setVisibility(View.GONE);
                if (btnCheck.getText().equals(getResources().getString(R.string.checkout))) {
                    locationManager.removeUpdates(locationListenerGPS);
                    btnCheck.setText(R.string.checkin);
                    progressBar.setVisibility(View.GONE);
                } else {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);
                    btnCheck.setText(R.string.checkout);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onClick(btnCheck);
                } else {

                }
                return;
            }
        }
    }


    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(((MainActivity)getActivity()));
        dialog.setTitle("Enable Location")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            OwnLocation[1] = location.getLongitude();
            OwnLocation[0] = location.getLatitude();
            ((MainActivity)getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lng.setText("Lng: "+String.valueOf(OwnLocation[1]));
                    lat.setText("Lat: "+String.valueOf(OwnLocation[0]));
                    progressBar.setVisibility(View.GONE);
                    distancia(OwnLocation[0],OwnLocation[1]);
                    //Toast.makeText(((MainActivity)getActivity()), "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    public void distancia(double lat, double lng){
        Location locationA = new Location("punto A");

        locationA.setLatitude(lat);
        locationA.setLongitude(lng);

        Location locationB = new Location("punto B");

        locationB.setLatitude(siteLocation[0]);
        locationB.setLongitude(siteLocation[1]);

        float distance = locationA.distanceTo(locationB);

        if(distance > 100){
            results.setTextColor(getResources().getColor(R.color.red));
            results.setVisibility(View.VISIBLE);
            results.setText("No estas dentro del área de trabajo: "+conversor(distance));
        }else {
            results.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            results.setVisibility(View.VISIBLE);
            results.setText("Distancia: "+conversor(distance));
        }
        //Toast.makeText(((MainActivity)getActivity()), "Distancia: "+distance+" mts", Toast.LENGTH_LONG).show();
    }

    private String conversor(double distance){
        if(distance > 999){
            return String.format("%.1f",(Math.round(distance)/1000.00))+ " Km";
        }else{
            return String.valueOf(Math.round(distance)) + " mts";
        }
    }
}
