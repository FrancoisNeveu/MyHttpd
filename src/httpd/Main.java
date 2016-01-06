package httpd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.net.httpserver.HttpServer;

public class Main {
	
	public static HttpServer server = null;

	public static void main(String[] args) {

		int port = 8000;
		String root = "";

		if (args.length == 2) {
			port = Integer.parseInt(args[0]);
			root = args[1];
			try {
				Main.server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
				MyHttpHandler handler = new MyHttpHandler();
				handler.setRoot(root);
				Main.server.createContext("/", handler);
				
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Date date = new Date();
				System.out.println("Launching MyHttpd server - " + dateFormat.format(date));
				System.out.println("Server listening on " + InetAddress.getLoopbackAddress().toString() + ":" + port);
				System.out.println("Root document : " + handler.getRoot());
				
				Runtime.getRuntime().addShutdownHook(new Thread() {
				    public void run() { 
				    	Main.server.stop(0);
				    	System.out.println("Shuting down the server...");
				    	System.exit(0);
				    }
				 });
				System.out.println("Press CTRL-C to exit...");
				Main.server.start();		
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		} else {
			System.err.println("Wrong number of Arguments\n Usage: httpd PORT ROOT_DIR");
		}
	}

}
