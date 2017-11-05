package in.ac.iiitv.wikishare;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private TextView mResponse;
    EditText mServerAddress;
    RequestQueue queue;
    Context context;
    SharedPreferences sharedPref;
    Button btn_query_result;
    EditText query;
    String baseURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mResponse = (TextView) findViewById(R.id.response);
        btn_query_result=(Button)findViewById(R.id.query_result);
//        mServerAddress = (EditText) findViewById(R.id.serveripaddr);
        Intent serviceIntent = new Intent(this,WikiFilesServer.class);
        this.startService(serviceIntent);
        BroadcastReceiver mIpAddressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String ipaddr = intent.getStringExtra("ipaddr");
                baseURL = "http://"+ipaddr+":8000";
                requestServer(baseURL);
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mIpAddressReceiver, new IntentFilter("IPofServer"));
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        queue = Volley.newRequestQueue(getApplicationContext());
        query=(EditText) findViewById(R.id.query);
        findServerAndConnect(formatedIpAddress);
        btn_query_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query1=query.getText().toString();
                send_query_to_server(query1,baseURL);
            }
        });


    }

    private void findServerAndConnect(String formatedIpAddress) {
        try{
            InetAddress group = InetAddress.getByName("224.1.1.1");
            final MulticastSocket s = new MulticastSocket(9000);
            s.joinGroup(group);
            final DatagramPacket hi = new DatagramPacket(formatedIpAddress.getBytes(), formatedIpAddress.length(),
                    group, 9000);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        s.send(hi);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    public void send_query_to_server(final String query,String baseURL){
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        mResponse.setText("");
        //progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseURL+"/query", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("response", s);
                try {
                    //progressDialog.hide();
                    JSONArray responseJson = new JSONArray(s);
                    for (int i=0;i<responseJson.length();i++){
                        JSONObject jsonObject = responseJson.getJSONObject(i);
                        mResponse.append(jsonObject.getString("ip")+" send count = "+jsonObject.getInt("sum")+'\n');
                    }

                } catch (Exception e) {
                    //progressDialog.hide();
                    //Snackbar.make(layout, R.string.internet_failure, Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
//                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                Log.e("err", volleyError.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                //SharedPreferences preferences = getSharedPreferences(LoginActivity.MY_PREFERENCES, MODE_PRIVATE);
                map.put("query",query);
                return map;
            }
        };
        queue.add(stringRequest);
    }

    public void downloadFiles(String baseURL){
        long enqueue = 0;
        final DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(
                    Uri.parse(baseURL+"/data"));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle("WikiData");
//            request.setI
            request.setDestinationInExternalFilesDir( getApplicationContext(), null,"wikitext.txt");
            assert dm != null;
            enqueue = dm.enqueue(request);

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
                    assert dm != null;
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {
                            String downloadFileLocalUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            if (downloadFileLocalUri != null) {
                                File downloadedFile = new File(Uri.parse(downloadFileLocalUri).getPath());
                                if (downloadedFile.exists()) {
                                    try {
                                        ArrayList<JSONObject> documents = new ArrayList<>();
                                        FileInputStream is = new FileInputStream(downloadedFile);
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                        String line = reader.readLine();
                                        while (!line.isEmpty() ) {
                                            documents.add(new JSONObject(line));
                                            line = reader.readLine();
                                        }
                                        createIndex(documents);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putBoolean("IsDataLoaded", true);
                                        editor.apply();
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }


    public void createIndex(ArrayList<JSONObject> documents) throws JSONException {
        HashMap<String,ArrayList<AbstractMap.SimpleEntry<String,Integer>>> invertedIndex = new HashMap<>();
        for (JSONObject pair:documents
             ) {
            String documentID = pair.getString("id");
            String text = pair.getString("text");
            HashMap<String,Integer> wordCounts = new HashMap<>();
            for (String word:text.split(" ")) {
                word = word.toLowerCase();
                if (wordCounts.containsKey(word)){
                    wordCounts.put(word,wordCounts.get(word)+1);
                }else {
                    wordCounts.put(word,1);
                }
            }
            ArrayList<String> keys = new ArrayList(wordCounts.keySet());
            for (String key:keys) {
                AbstractMap.SimpleEntry<String,Integer> map = new AbstractMap.SimpleEntry<String, Integer>(documentID,wordCounts.get(key));
                if (invertedIndex.containsKey(key)){
                    ArrayList<AbstractMap.SimpleEntry<String,Integer>> list = invertedIndex.get(key);
                    list.add(map);
                    invertedIndex.put(key,list);
                }else {
                    ArrayList<AbstractMap.SimpleEntry<String,Integer>> list = new ArrayList<>();
                    list.add(map);
                    invertedIndex.put(key,list);
                }
            }
        }
        Gson gson = new Gson();
        String json = gson.toJson(invertedIndex);
        String filename = "index.json";
        Log.e("index",json);
        File file = new File(getApplicationContext().getExternalFilesDir(null), filename);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void requestServer(final String url) {
        queue.add(new StringRequest(Request.Method.GET, url+"/ping", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                sharedPref = context.getSharedPreferences(
                        "preference_file", Context.MODE_PRIVATE);
                boolean dataLoaded = sharedPref.getBoolean("IsDataLoaded", false);
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
