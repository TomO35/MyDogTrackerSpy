package fr.mds.mydogtrackerspy.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import fr.mds.mydogtrackerspy.R;
import fr.mds.mydogtrackerspy.tools.LocationService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

    TextView tv_id;
    LocationService locationService;
    boolean firstboot;
    String id = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstboot = getSharedPreferences("APP_PREF", MODE_PRIVATE).getBoolean("firstboot", true);

        tv_id = findViewById(R.id.tv_id_id);

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null){
            createNetworkErrorDialog(this);
        }
        LocationManager lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        if (!(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER))){
            createLocationErrorDialog(this);
        }

        if(firstboot){
            getSharedPreferences("APP_PREF", this.MODE_PRIVATE).edit().putBoolean("firstboot",false).apply();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(DogTrackerService.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            DogTrackerService dogTrackerService = retrofit.create(DogTrackerService.class);
            dogTrackerService.add_spy().enqueue(new Callback<BasicAnswer>() {
                @Override
                public void onResponse(Call<BasicAnswer> call, Response<BasicAnswer> response) {
                    id = response.body().getMyAnswer();
                    tv_id.setText(id);
                }
                @Override
                public void onFailure(Call<BasicAnswer> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Something bad happened... sorry !!!", Toast.LENGTH_SHORT).show();
                }
            });

            getSharedPreferences("APP_PREF", this.MODE_PRIVATE).edit().putString("spyid", id).apply();
        } else {
            String theId = getSharedPreferences("APP_PREF", this.MODE_PRIVATE).getString("spyid", "fail");
            if (theId != "fail") {
                tv_id.setText(String.valueOf(id));
            } else {
                tv_id.setText(R.string.error_id);
                getSharedPreferences("APP_PREF", this.MODE_PRIVATE).edit().putBoolean("firstboot",true).apply();
            }
        }
        locationService = new LocationService();
    }

    protected void createNetworkErrorDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getResources().getText(R.string.error_connectivity))
                .setTitle(getResources().getText(R.string.network_needed))
                .setCancelable(false)
                .setPositiveButton(getResources().getText(R.string.settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton(getResources().getText(R.string.out),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void createLocationErrorDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getResources().getText(R.string.error_location))
                .setTitle(getResources().getText(R.string.location_needed))
                .setCancelable(false)
                .setPositiveButton(getResources().getText(R.string.settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton(getResources().getText(R.string.out),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }
}

interface DogTrackerService {

    String ENDPOINT = "http://dogtracker.epizy.com/";

    @GET("ws.php?action=add_user")
    Call<BasicAnswer> add_spy();
}

class BasicAnswer {

    @SerializedName("response")
    private String myAnswer;

    public String getMyAnswer() {
        return myAnswer;
    }
}