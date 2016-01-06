package httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MyHttpHandler implements HttpHandler {

	private String root = System.getProperty("user.dir");

	public void setRoot(String r) {
		this.root = System.getProperty("user.dir");
		if (r != null && r.length() > 1)
			this.root += File.separator + r;
	}
	
	public String getRoot() {
		return this.root;
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		URI uri = t.getRequestURI();
		if (uri.getPath().equals("/"))
			uri = uri.resolve("index.html");

		String path = uri.getPath().replace("/", File.separator);
		System.out.println("Reaching file :" + this.root + path);
		File file = new File(this.root + path).getCanonicalFile();

		if (!file.getPath().startsWith(this.root)) {
			// Suspected path traversal attack: reject with 403 error.
			String response = "403 (Forbidden)\n";
			t.sendResponseHeaders(403, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} else if (!file.isFile()) {
			// Object does not exist or is not a file: reject with 404 error.
			String response = "404 (Not Found)\n";
			t.sendResponseHeaders(404, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} else {
			// Object exists and is a file: accept with response code 200.
			Headers h = t.getResponseHeaders();
			String contenType = URLConnection.guessContentTypeFromName(file.getName());
			if (contenType == null && 
					file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase().equals("js")) {
				contenType = "application/javascript";
			}
			if (contenType != null)
				h.set("Content-Type", contenType);
			
			h.set("Server", "MyHttpd Test Server");
			t.sendResponseHeaders(200, file.length());
			System.out.println("Fetching " + uri.getPath() + " -> " + contenType);

			OutputStream os = t.getResponseBody();
			FileInputStream fs = new FileInputStream(file);
			final byte[] buffer = new byte[0x10000];
			int count = 0;
			while ((count = fs.read(buffer)) >= 0) {
				os.write(buffer, 0, count);
			}
			fs.close();
			os.close();
		}
	}

}
