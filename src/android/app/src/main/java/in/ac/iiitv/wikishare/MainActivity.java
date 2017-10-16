package in.ac.iiitv.wikishare;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class MainActivity extends AppCompatActivity {
    private static final int PORT = 8765;
    private TextView hello;
    private Handler handler = new Handler();
    EditText mServerAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hello = (TextView) findViewById(R.id.hello);
        mServerAddress = (EditText) findViewById(R.id.serveripaddr);
        Intent serviceIntent = new Intent(this,WikiFilesServer.class);
        this.startService(serviceIntent);
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        textIpaddr.setText("Please access! http://" + formatedIpAddress + ":" + WikiFilesServer.PORT);

    }

    public void requestServer(View view) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = mServerAddress.getText().toString();
        queue.add(new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hello.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

        }
        }));
    }

}
