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
import android.widget.TextView;
import fi.iki.elonen.NanoHTTPD;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class MainActivity extends AppCompatActivity {
    private static final int PORT = 8765;
    private TextView hello;
    private MyHTTPD server;
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hello = (TextView) findViewById(R.id.hello);
        Intent serviceIntent = new Intent(this,WikiFilesServer.class);
        this.startService(serviceIntent);
//        RequestQueue queue = Volley.newRequestQueue(this);
//        Server
//        queue.add(new JsonObjectRequest())

    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        textIpaddr.setText("Please access! http://" + formatedIpAddress + ":" + PORT);

//        try {
//            server = new MyHTTPD();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (server != null)
            server.stop();
    }
    public class MyHTTPD extends NanoHTTPD {

        public MyHTTPD() throws IOException {
            super(PORT);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            hello.setText("\nRunning! Point your browsers to http://localhost:"+PORT+"/ \n");
        }

        @Override
        public Response serve(IHTTPSession session) {
            String msg = "<html><body><h1>Hello server</h1>\n";
//            Map<String, String> parms = session.getParms();
//            if (parms.get("username") == null) {
//                msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//            } else {
//                msg += "<p>Hello, " + parms.get("username") + "!</p>";
//            }
            return newFixedLengthResponse(msg + "</body></html>\n");
        }
    }
//    private class MyHTTPD extends NanoHTTPD {
//        public MyHTTPD() throws IOException {
//            super(PORT, null);
//        }
//
//        @Override
//        public Response serve(String uri, String method, Map<String, String> parms, Map<String, String> files) {
//            final StringBuilder buf = new StringBuilder();
//            for (Entry<Object, Object> kv : header.entrySet())
//                buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    hello.setText(buf);
//                }
//            });
//
//            final String html = "<html><head><head><body><h1>Hello, World</h1></body></html>";
//            return new NanoHTTPD.Response(OK, MIME_HTML, html);
//        }
//    }
}
