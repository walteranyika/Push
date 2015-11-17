package com.walter.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    ProgressBar progress;
    private static final String EMAIL_ID ="emailID" ;
    EditText edtEmail;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String REG_ID = "regId";
    GoogleCloudMessaging gcm;
    String regId="";
       //http://programmerguru.com/android-tutorial/how-to-send-push-notifications-using-gcm-service/
    //https://gist.github.com/prime31/5675017
    //http://www.programming-techniques.com/2014/01/google-cloud-messaging-gcm-in-android.html
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtEmail= (EditText) findViewById(R.id.editTextEmail);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        SharedPreferences prefs =getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        String regId = prefs.getString(REG_ID,"");
        Log.d("DATA",regId);
        if(!regId.isEmpty())
        {
            Intent i =new Intent(this, HomeActivity.class);
            i.putExtra("regId",regId);
            startActivity(i);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void register(View view) {
        String emailAddress = edtEmail.getText().toString().trim();
        if(!TextUtils.isEmpty(emailAddress))
        {
            if(checkGooglePlayServices())
            {
                Log.d("DATA","Play Services Available");
                registerInBackground(emailAddress);
            }else
            {
                Toast.makeText(this, "Invalid Email. Check Again", Toast.LENGTH_LONG).show();
                Log.d("DATA", "No Play Services");
            }

        }else
        {
            Toast.makeText(this,"Invalid Email. Check Again",Toast.LENGTH_LONG).show();
            Log.d("DATA", "Invalid Email");
        }


    }
    public boolean checkGooglePlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // When Play services not found in device
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // Show Error dialog to install Play services
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this,"This device doesn't support Play services, App will not work normally", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            Toast.makeText(this,"This device supports Play services, App will work normally",Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public void registerInBackground(final  String emailID)
    {
        new AsyncTask<Void,Void,String>()
        {

            @Override
            protected String doInBackground(Void... params) {
                String msg="";

                try {
                    if(gcm==null)
                    {
                        gcm=GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId=gcm.register(ApplicationConstants.GGOGLE_PROJECT_ID);
                    msg="REGISTRATION ID IS :"+regId;
                    Log.d("DATA","REG ID IS "+regId);
                } catch (IOException e) {
                    msg= "ERROR DURING REGISTRATION :"+e.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                if(!s.isEmpty()) {
                    storeIdInSharedPrefs(getApplicationContext(), regId, emailID);
                    Toast.makeText(getApplicationContext(),"Successfully Registred",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getApplicationContext(),"Something awful happened during registration",Toast.LENGTH_LONG).show();
                }
                super.onPostExecute(s);
            }
        }.execute();
    }
    public void storeIdInSharedPrefs(Context context,String regId, String emailID)
    {
        SharedPreferences prefs= getSharedPreferences("UserDetails",Context.MODE_PRIVATE);
        SharedPreferences.Editor  editor = prefs.edit();
        editor.putString(REG_ID,regId);
        editor.putString(EMAIL_ID,emailID);
        editor.commit();
        Log.d("DATA", "Saved ID in Shared Preferences");
        sendRegIdToServer();
    }

    public void sendRegIdToServer(){
        RequestParams params =new RequestParams();
        params.put("regId",regId);
        progress.setVisibility(View.VISIBLE);
        AsyncHttpClient client=new AsyncHttpClient();
        client.post(ApplicationConstants.SERVER_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast.makeText(getApplicationContext(),"Successfully Send The ID To The Server",Toast.LENGTH_LONG).show();
                Log.d("DATA", "Successfully Sent The Reg ID to the server");
                Intent intent=new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("regId",regId);
                startActivity(intent);
                progress.setVisibility(View.INVISIBLE);
                finish();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                progress.setVisibility(View.INVISIBLE);
                Log.d("DATA", "Failed To Send Reg ID to The Server");
                Toast.makeText(getApplicationContext(),"Error while sending the Reg Ids To the Server",Toast.LENGTH_LONG).show();

            }
        });
    }
}
