package in.ac.iiitv.wikishare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.sql.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import fi.iki.elonen.NanoHTTPD;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private static final int PORT = 8765;
    private TextView mResponse;
    EditText mServerAddress;
    RequestQueue queue;
    Context context;
    SharedPreferences sharedPref;
    Button btn_query_result;
    EditText query;
    public static Handler handler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mResponse = (TextView) findViewById(R.id.response);
        handler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;

                    default:
                        break;
                }
            }
        };
        btn_query_result=(Button)findViewById(R.id.query_result);
        mServerAddress = (EditText) findViewById(R.id.serveripaddr);
        Intent serviceIntent = new Intent(this,WikiFilesServer.class);
        this.startService(serviceIntent);
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        textIpaddr.setText("Please access! http://" + formatedIpAddress + ":" + WikiFilesServer.PORT);
        queue = Volley.newRequestQueue(getApplicationContext());
        query=(EditText) findViewById(R.id.query);
        String msg = formatedIpAddress;
//        try{
//            InetAddress group = InetAddress.getByName("224.1.1.1");
//            MulticastSocket s = new MulticastSocket(9000);
//            s.joinGroup(group);
//            DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
//                    group, 9000);
//            s.send(hi);}
//        catch (IOException e){
//            e.printStackTrace();
//        }
        btn_query_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query1=query.getText().toString();
                send_query_to_server(query1);
            }
        });
//        ArrayList<JSONObject> documents = new ArrayList<>();
//        try {
//            documents.add(new JSONObject("{\"id\": \"22590756\", \"text\": \"Thermal profiling\\n\\nA thermal profile is a complex set of time-temperature data typically associated with the measurement of thermal temperatures in an oven (ex: reflow oven). The thermal profile is often measured along a variety of dimensions such as slope, soak, time above liquidus (TAL), and peak.\\n\\nA thermal profile can be ranked on how it fits in a process window (the specification or tolerance limit). Raw temperature values are normalized in terms of a percentage relative to both the process mean and the window limits. The center of the process window is defined as zero, and the extreme edges of the process window are \\u00b199%. A Process Window Index (PWI) greater than or equal to 100% indicates the profile is outside of the process limitations. A PWI of 99% indicates that the profile is within process limitations, but runs at the edge of the process window. For example, if the process mean is set at 200\\u00a0\\u00b0C with the process window calibrated at 180\\u00a0\\u00b0C and 220\\u00a0\\u00b0C respectively, then a measured value of 188\\u00a0\\u00b0C translates to a process window index of \\u221260%.\\n\\nThe method is used in a variety of industrial and laboratory processes, including electronic component assembly, optoelectronics, optics, biochemical engineering, food science, decontamination of hazardous wastes, and geochemical analysis.\\n\\nOne of the major uses of this method is soldering of electronic assemblies. There are two main types of profiles used today: The Ramp-Soak-Spike (RSS) and the Ramp to Spike (RTS). In modern systems, quality management practices in manufacturing industries have produced automatic process algorithms such as PWI, where soldering ovens come preloaded with extensive electronics and programmable inputs to define and refine process specifications. By using algorithms such as PWI, engineers can calibrate and customize parameters to achieve minimum process variance and a near zero defect rate.\\n\\nIn soldering, a thermal profile is a complex set of time-temperature values for a variety of process dimensions such as slope, soak, TAL, and peak. Solder paste contains a mix of metal, flux, and solvents that aid in the phase change of the paste from semi-solid, to liquid to vapor; and the metal from solid to liquid. For an effective soldering process, soldering must be carried out under carefully calibrated conditions in a reflow oven. Convection Reflow Oven Detailed Description\\n\\nThere are two main profile types used today in soldering: \\n\\nRamp is defined as the rate of change in temperature over time, expressed in degrees per second. The most commonly used process limit is 4\\u00a0\\u00b0C/s, though many component and solder paste manufacturers specify the value as 2\\u00a0\\u00b0C/s. Many components have a specification where the rise in temperature should not exceed a specified temperature per second, such as 2\\u00a0\\u00b0C/s. Rapid evaporation of the flux contained in the solder paste can lead to defects, such as lead lift, tombstoning, and solder balls. Additionally, rapid heat can lead to steam generation within the component if the moisture content is high, resulting in the formation of microcracks.\\n\\nIn the soak segment of the profile, the solder paste approaches a phase change. The amount of energy introduced to both the component and the PCB approaches equilibrium. In this stage, most of the flux evaporates out of the solder paste. The duration of the soak varies for different pastes. The mass of the PCB is another factor that must be considered for the soak duration. An over-rapid heat transfer can cause solder splattering and the production of solder balls, bridging and other defects. If the heat transfer is too slow, the flux concentration may remain high and result in cold solder joints, voids and incomplete reflow.\\n\\nAfter the soak segment, the profile enters the ramp-to-peak segment of the profile, which is a given temperature range and time exceeding the melting temperature of the alloy. Successful profiles range in temperature up to 30\\u00a0\\u00b0C higher than liquidus, which is approximately 183\\u00a0\\u00b0C for eutectic and approximately 217\\u00a0\\u00b0C for lead-free.\\n\\nThe final area of this profile is the cooling section. A typical specification for the cool down is usually less than \\u22126\\u00a0\\u00b0C/s (falling slope).\\n\\nThe Ramp to Spike (RTS) profile is almost a linear graph, starting at the entrance of the process and ending at the peak segment, with a greater \\u0394t (change in temperature) in the cooling segment. While the Ramp-Soak-Spike (RSS) allows for about 4\\u00a0\\u00b0C/s, the requirements of the RTS is about 1\\u20132\\u00a0\\u00b0C/s. These values depend on the solder paste specifications. The RTS soak period is part of the ramp and is not as easily distinguishable as in RSS. The soak is controlled primarily by the conveyor speed. The peak of the RTS profile is the endpoint of the linear ramp to the peak segment of the profile. The same considerations about defects in an RSS profile also apply to an RTS profile.\\n\\nWhen the PCB enters the cooling segment, the negative slope generally is steeper than the rising slope.\\n\\nThermocouples (or TCs) are two dissimilar metals joined by a welded bead. For a thermocouple to read the temperature at any given point, the welded bead must come in direct contact with the object whose temperatures need to be measured. The two dissimilar wires must remain separate, joined only at the bead; otherwise, the reading is no longer at the welded bead but at the position where the metals first make contact, rendering the reading invalid.\\n\\nA zigzagging thermocouple reading on a profile graph indicates loosely attached thermocouples. For accurate readings, thermocouples are attached to areas that are dissimilar in terms of mass, location and known trouble spots. Additionally, they should be isolated from air currents. Finally, the placement of several thermocouples should range from populated to less populated areas of the PCB for the best sampling conditions.\\n\\nSeveral methods of attachment are used, including epoxy, high-temperature solder, Kapton and aluminum tape, each with various levels of success for each method.\\n\\nEpoxies are good at securing TC conductors to the profile board to keep them from becoming entangled in the oven during profiling. Epoxies come in both insulator and conductor formulations The specs need to be checked otherwise an insulator can play a negative role in the collection of profile data. The ability to apply this adhesive in similar quantities and thicknesses is difficult to measure in quantitative terms. This decreases reproducibility. If epoxy is used, properties and specifications of that epoxy must be checked. Epoxy functions within a wide range of temperature tolerances.\\n\\nThe properties of solder used for TC attachment differ from that of electrically connective solder. High temperature solder is not the best choice to use for TC attachment for several reasons. First, it has the same drawbacks as epoxy \\u2013 the quantity of solder needed to adhere the TC to a substrate varies from location to location. Second, solder is conductive and may short-circuit TCs. Generally, there is a short length of conductor that is exposed to the temperature gradient. Together, this exposed area, along with the physical weld produce an Electromotive Force (EMF). Conductors and the weld are placed in a homogeneous environment within the temperature gradient to minimize the effects of EMF.\\n\\nKapton tape is one of the most widely used tapes and methods for TC and TC conductor attachment. When several layers are applied, each layer has an additive effect on the insulation and may negatively impact a profile. A disadvantage of this tape is that the PCB has to be very clean and smooth to achieve an airtight cover over the thermocouple weld and conductors. Another disadvantage to Kapton tape is that at temperatures above 200\\u00a0\\u00b0C the tape becomes elastic and, hence, the TCs have a tendency to lift off the substrate surface. The result is erroneous readings characterized by jagged lines in the profile.\\n\\nAluminum tape comes in various thicknesses and density. Heavier aluminum tape can defuse the heat transfer through the tape and act as an insulator. Low density aluminum tape allows for heat transfer to the EMF-producing area of the TC. The thermal conductivity of the aluminum tape allows for even conduction when the thickness of the tape is fairly consistent in the EMF-producing area of the thermocouple.\\n\\nVirtual profiling is a method of creating profiles without attaching the thermocouples (TCs) or having to physically instrument a PCB each and every time a profile is run for the same production board. All the typical profile data such as slope, soak, TAL, etc., that are measured by instrumented profiles are gathered by using virtual profiles. The benefits of not having attached TCs surpass the convenience of not having to instrument a PCB every time a new profile is needed.\\n\\nVirtual profiles are created automatically, for both reflow or wave solder machines. An initial recipe setup is required for modeling purposes, but once completed, profiling can be made virtual. As the system is automatic, profiles can be generated periodically or continuously for each and every assembly. SPC charts along with CpK can be used as an aid when collecting a mountain of process-related data. Automated profiling systems continuously monitor the process and create profiles for each assembly. As barcoding becomes more common with both reflow and wave processes, the two technologies can be combined for profiling traceability, allowing each generated profile to be searchable by barcode. This is useful when an assembly is questioned at some time in the future. As a profile is created for each assembly, a quick search using the PCB\\u2019s barcode can pull up the profile in question and provide evidence that the component was processed in spec. Additionally, tighter process control can be achieved when combining automated profiling with barcoding, such as confirming that the correct process has been input by the operator before launching a production run.\\n\\n\"}\n"));
//            documents.add(new JSONObject("{\"id\": \"22590770\", \"text\": \"Kaveri Kaul\\n\\nKavery (Dutta) Kaul is an American independent film director and teacher, born in India and brought up in the United States. She started her directing career with \\\"First Look\\\", the winner of the Best Cultural Film Award at the Havana Film Festival (Festival of New Latin American Cinema). Kavery's films have been shown at festivals in Berlin, London, and Sydney, and other countries including Japan, India, Burkina Faso and Martinique. She is a recipient of the New York Foundation for the Arts Fellowship in Film, and a two-time National Endowment for the Arts awardee.\\n\\nHer documentary films include \\\"Long Way from Home\\\", \\\"One Hand Don\\u2019t Clap\\\", \\\"Wild at Art\\\", \\\"Soul Gone Home\\\" and \\\"First Look\\\".\\n\\n\"}"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            createIndex(documents);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        downloadFiles(mServerAddress.getText().toString());

    }
    public void send_query_to_server(final String query){
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://"+mServerAddress.getText().toString();

        //progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/query", new Response.Listener<String>() {
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
                    /*Intent intent = new Intent(getApplicationContext(), DayEnd.class);
                    intent.putExtra(EXTRA_ISSUECOUNT, responseJson.getInt("issueCount"));
                    intent.putExtra(EXTRA_RECEIVECOUNT, responseJson.getInt("returnCount"));
                    intent.putExtra(EXTRA_AVAILCOUNT, responseJson.getInt("availableCount"));
                    intent.putExtra(EXTRA_MONEYCOUNT, responseJson.getInt("collection"));
                    progressDialog.hide();
                    startActivity(intent);*/
//                    String res=responseJson.getString("message");
//                    Log.v("response",res);


                } catch (Exception e) {
                    //progressDialog.hide();
                    //Snackbar.make(layout, R.string.internet_failure, Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                Log.e("err", volleyError.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                //SharedPreferences preferences = getSharedPreferences(LoginActivity.MY_PREFERENCES, MODE_PRIVATE);
                map.put("query",query);
                Log.e("sent", map.toString());
                return map;
            }
        };
        queue.add(stringRequest);
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
            enqueue = dm.enqueue(request);;

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
                            String downloadFilePath = null;
                            String downloadFileLocalUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            if (downloadFileLocalUri != null) {
                                File downloadedFile = new File(Uri.parse(downloadFileLocalUri).getPath());
//                                String filePath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
//                                File downloadedFile = new File(filePath);
                                if (downloadedFile.exists()) {
                                    try {
                                        ArrayList<JSONObject> documents = new ArrayList<>();
                                        FileInputStream is = new FileInputStream(downloadedFile);
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                        String line = reader.readLine();
                                        while (line != null) {
                                            Log.d("StackOverflow", line);
                                            documents.add(new JSONObject(line));
                                            line = reader.readLine();
                                        }
                                        createIndex(documents);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("IsDataLoaded", true);
                            editor.apply();
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
    public void requestServer(View view) {
        final String url = "http://"+mServerAddress.getText().toString();
        queue.add(new StringRequest(Request.Method.GET, url+"/ping", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                hello.setText(response);
                Log.e("asfas",response);
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
