import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Producer extends MdnsService {

    private HttpServer server;

    public Producer(File directory, int port, String hostname) throws IOException {
        super("producer_" + port, "_http._tcp", "local.", port, hostname, "type=producer");
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", registeredService.getPort()), 0);
        server.createContext("/", new FileSendHandler(directory));
    }

    public synchronized void startQueriesListening() throws IOException {
        server.start();
    }

    public synchronized void stop() {
        super.stop();
        if (server != null) {
            server.stop(0);
        }
    }
}
