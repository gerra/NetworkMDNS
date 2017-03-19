import net.posick.mDNS.Browse;
import net.posick.mDNS.DNSSDListener;
import net.posick.mDNS.ServiceInstance;
import org.xbill.DNS.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
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
//        producers.clear();
//        try {
//            registerServiceListener();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ignored) {}


        System.out.println("Trying to request " + filename);
        byte[] query = ("request " + filename).getBytes();
        for (ServiceInstance consumer : producers) {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(consumer.getAddresses()[0], consumer.getPort());
                SocketChannel serverChannel = SocketChannel.open(socketAddress);
                serverChannel.write(ByteBuffer.wrap(query));

                FileOutputStream fos = null;
                try {
                    File file = new File("./" + filename);
                    file.delete();
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    long position = 0;
                    long transferred;
                    FileChannel fileChannel = fos.getChannel();
                    while ((transferred = fileChannel.transferFrom(serverChannel, position, 256)) > 0) {
                        position += transferred;
                        System.out.println("received " + transferred + ", total = " + position);
                    }
                } catch (IOException e) {
                    System.err.println("no found file");
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
