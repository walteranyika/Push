package com.walter.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class HomeActivity extends ActionBarActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    TextView tvUserTititle,tvMessage;
    private static final String EMAIL_ID ="emailID" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvUserTititle= (TextView) findViewById(R.id.textViewUserTitle);
        tvMessage= (TextView) findViewById(R.id.textViewMessage);

        SharedPreferences pref= getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String emailId= pref.getString(EMAIL_ID, "");
        tvUserTititle.setText("Hello " + emailId);

        String str = getIntent().getExtras().getString("msg");

        if(!checkPlayServices())
        {
            Toast.makeText(getApplicationContext(), "This device doesn't support Play services, App will not work normally", Toast.LENGTH_LONG).show();
        }
        if(str!=null)
        {
            tvMessage.setText(str);
            Log.d("DATA", str);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // Check if Google Playservices is installed in Device or not
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil .isGooglePlayServicesAvailable(this);
        // When Play services not found in device
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // Show Error dialog to install Play services
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText( getApplicationContext(), "This device doesn't support Play services, App will not work normally", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            Toast.makeText(getApplicationContext(),"This device supports Play services, App will work normally", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

    }
}
