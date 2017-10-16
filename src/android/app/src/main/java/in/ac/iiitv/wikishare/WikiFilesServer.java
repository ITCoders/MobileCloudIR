package in.ac.iiitv.wikishare;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class WikiFilesServer extends Service {
    MyHTTPD server;
    static int PORT = 8056;
    public WikiFilesServer() {
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
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
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

}
