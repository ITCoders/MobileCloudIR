package in.ac.iiitv.wikishare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WikiFilesServer extends Service {
    Context c;
    MyHTTPD server;
    static int PORT = 8056;
    public WikiFilesServer() {
        c = this;
        try {
            server = new MyHTTPD();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    class MyHTTPD extends NanoHTTPD {
        MyHTTPD() throws IOException {
            super(PORT);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        }
        private void sendMessageToActivity(String ipAddress) {
            Intent intent = new Intent("IPofServer");
            // You can also include some extra data.
            intent.putExtra("ipaddr", ipAddress);
            LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
        }
        @Override
        public Response serve(IHTTPSession session) {
            String msg="adfsdsfsdf";
            Map params = session.getParameters();
            Log.e("afas",params.toString());
            if (params.containsKey("ip")){
                ArrayList list = (ArrayList) params.get("ip");
                sendMessageToActivity((String) list.get(0));
                return newFixedLengthResponse(msg);
            }
            if (params.containsKey("q")){
                ArrayList list = (ArrayList) params.get("q");
                String query = (String)list.get(0);
                try{
                File indexFile = new File(getApplicationContext().getExternalFilesDir(null),"index.json");
                FileInputStream is = new FileInputStream(indexFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder text = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    text.append(line);
                    line = reader.readLine();
                }
                JSONObject jsonObject = new JSONObject(text.toString());
                for (String word:query.split(" ")
                     ) {
                    word = word.toLowerCase();
                    JSONArray array = jsonObject.getJSONArray(word);
                    int sum = 0;
                    for (int i=0; i<array.length();i++){
                        sum += array.getJSONObject(i).getInt("value");
                    }
                    Log.e("sum",sum+"");
//                    msg = sum+"";
                    msg = "{\"sum\":"+sum+"}";
                }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Log.e("i listened",params.toString());
//            String msg = "<html><body><h1>Hello server</h1>\n";
//            Map<String, String> parms = session.getParms();
//            if (parms.get("username") == null) {
//                msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//            } else {
//                msg += "<p>Hello, " + parms.get("username") + "!</p>";
//            }
            return newFixedLengthResponse(msg);
        }
    }

}
