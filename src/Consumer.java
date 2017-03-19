import net.posick.mDNS.Browse;
import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.ServiceInstance;
import org.xbill.DNS.Message;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Consumer extends MdnsService {
    public Consumer(int port, String hostname) throws IOException {
        super("consumer_" + port, "_http._tcp", "local.", port, hostname, "type=consumer");
    }

    private List<Object> browseIds = new ArrayList<>();
    private List<ServiceInstance> producers = new ArrayList<>();

    public void registerServiceListener() throws IOException {
        Object id = multicastDNSService.startServiceDiscovery(new Browse("_http._tcp.local."), new DNSSDListener() {
            @Override
            public void serviceDiscovered(Object o, ServiceInstance serviceInstance) {
                System.out.println("found: " + serviceInstance.getName());
                if (serviceInstance.getName().toString().startsWith("producer")) {
                    producers.add(serviceInstance);
                }
            }

            @Override
            public void serviceRemoved(Object o, ServiceInstance serviceInstance) {
                System.out.println("removed: " + serviceInstance.getName());
                producers.remove(serviceInstance);
            }

            @Override
            public void receiveMessage(Object o, Message message) {

            }

            @Override
            public void handleException(Object o, Exception e) {

            }
        });
        browseIds.add(id);
    }

    public void requestFile(String filename) {
        System.out.println("Trying to request " + filename);
        File file = new File("./" + filename);
        for (ServiceInstance producer : producers) {
            try {
                URL url = new URL("http://" +
                        producer.getAddresses()[0].getCanonicalHostName() + ":" + producer.getPort() +
                        "/" + filename);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("GET");
                file.delete();
                httpConnection.connect();
                InputStream inputStream = httpConnection.getInputStream();
                Files.copy(inputStream, file.toPath());
                inputStream.close();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                file.delete();
            }
        }
    }

    public synchronized void stop() {
        for (Object id : browseIds) {
            try {
                multicastDNSService.stopServiceDiscovery(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        browseIds.clear();
        super.stop();
    }
}
