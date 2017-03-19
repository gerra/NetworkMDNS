import com.sun.net.httpserver.HttpServer;
import net.posick.mDNS.ServiceInstance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JSConsumer extends Consumer {
    private HttpServer requestListener;

    public JSConsumer(int port, String hostname) throws IOException {
        super(port, hostname);
        requestListener = HttpServer.create(new InetSocketAddress("0.0.0.0", 3000), 0);
        requestListener.createContext("/js", new JSHttpHandler(this));
        requestListener.start();
    }

    public List<URL> getProducersUrls() throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        for (ServiceInstance producer : producers) {
            URL url = new URL("http://" +
//                    producer.getAddresses()[0].getCanonicalHostName() + ":" + producer.getPort() +
                    producer.getHost() + "local" + ":" + producer.getPort() +
                    "/js");
            urls.add(url);
        }
        return urls;
    }
}
