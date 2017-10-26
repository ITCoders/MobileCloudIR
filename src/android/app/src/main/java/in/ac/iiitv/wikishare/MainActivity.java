package in.ac.iiitv.wikishare;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import fi.iki.elonen.NanoHTTPD;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class MainActivity extends AppCompatActivity {
    private static final int PORT = 8765;
    private TextView hello;
    private Handler handler = new Handler();
    EditText mServerAddress;
    RequestQueue queue;
    Context context;
    SharedPreferences sharedPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        hello = (TextView) findViewById(R.id.hello);
        mServerAddress = (EditText) findViewById(R.id.serveripaddr);
        Intent serviceIntent = new Intent(this,WikiFilesServer.class);
        this.startService(serviceIntent);
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        textIpaddr.setText("Please access! http://" + formatedIpAddress + ":" + WikiFilesServer.PORT);
        queue = Volley.newRequestQueue(getApplicationContext());
//        downloadFiles();

    }

    public void downloadFiles(String serverIP){
        long enqueue = 0;
        final DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = null;
        try {
            request = new DownloadManager.Request(
                    Uri.parse(serverIP+"/data"));
//                    Uri.parse("http://10.42.0.1:8000/data"));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle("Trial");
//            request.setI
            request.setDestinationInExternalFilesDir( getApplicationContext(), null,"plain.txt");
            enqueue = dm.enqueue(request);
            Log.e("asdfasd","sdfasdf");

        } catch (Exception e) {
            Log.e("error",e.toString());
        }
        final long finalEnqueue = enqueue;
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(finalEnqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("IsDataLoaded", true);
                            editor.apply();
                            Log.e("af",""+ sharedPref.getBoolean("IsDataLoaded", false));
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }


    public void requestServer(View view) {
        final String url = "http://"+mServerAddress.getText().toString();
        queue.add(new StringRequest(Request.Method.GET, url+"/ping", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hello.setText(response);
                sharedPref = context.getSharedPreferences(
                        "preference_file", Context.MODE_PRIVATE);
                boolean dataLoaded = sharedPref.getBoolean("IsDataLoaded", false);
                Log.e("dafr",dataLoaded+"");

                if (!dataLoaded){
                    downloadFiles(url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

        }
        }));


    }

}
