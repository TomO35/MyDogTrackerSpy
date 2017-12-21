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

import fr.mds.mydogtrackerspy.R;
import fr.mds.mydogtrackerspy.tools.LocationService;

public class MainActivity extends AppCompatActivity {

    TextView tv_id;
    LocationService locationService;
    boolean firstboot;

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
                //TODO webserv call to result
            tv_id.setText("");//<- put result
            getSharedPreferences("APP_PREF", this.MODE_PRIVATE).edit().putInt("spyid", 0).apply();
        } else {
            int id = getSharedPreferences("APP_PREF", this.MODE_PRIVATE).getInt("spyid", -1);
            if (id != -1) {
                tv_id.setText(String.valueOf(id));
            } else {
                tv_id.setText(R.string.error_id);
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
