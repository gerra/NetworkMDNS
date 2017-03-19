import net.posick.mDNS.MulticastDNSService;
import net.posick.mDNS.ServiceInstance;
import net.posick.mDNS.ServiceName;
import org.xbill.DNS.Name;

import java.io.IOException;
import java.net.InetAddress;

public class MdnsService {
    private ServiceInstance notRegisteredService;

    protected MulticastDNSService multicastDNSService;
    protected ServiceInstance registeredService;

    public MdnsService(String name, String type, String domain, int port, String hostname, String... txtRecords) throws IOException {
        ServiceName serviceName = new ServiceName(name + "." + type + "." + domain);
        if (!hostname.endsWith(".")) {
            hostname += ".local.";
        }
        multicastDNSService = new MulticastDNSService();
        notRegisteredService = new ServiceInstance(
                serviceName, 0, 0,
                port, new Name(hostname),
//                new InetAddress[] {InetAddress.getByName("169.254.8.247")},
                InetAddress.getAllByName(hostname),
                txtRecords);
        register();
    }

    public void register() throws IOException {
        registeredService = multicastDNSService.register(notRegisteredService);
        if (registeredService != null) {
            System.out.println("Services Successfully Registered: \n\t" + registeredService +
                    " " + registeredService.getAddresses()[0].toString());

        } else {
            System.err.println("Services Registration Failed!");
            throw new IOException("Services Registration Failed!");
        }
    }

    public void unregister() {
        if (registeredService != null) {
            try {
                multicastDNSService.unregister(registeredService);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        System.out.println("stop service " + registeredService.getName());
        unregister();
        try {
            multicastDNSService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
