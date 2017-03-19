import org.xbill.DNS.MulticastDNSUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by german on 06.03.17.
 */
public class Main {
// CVEng.odt lol.cpp
    public static void main(String[] args) throws IOException {
//        HttpServer server = HttpServer.create();
//        server.bind(new InetSocketAddress(8765), 0);
//        HttpContext context = server.createContext("/lower", new JSHttpHandler());
//        server.start();
//        System.out.println("!!!");
//        server.setExecutor();

        if (args == null || args.length < 1) {
            System.err.println("Invalid options");
            System.exit(1);
        }
        String host;
        String machineName = MulticastDNSUtils.getMachineName();
//        host = MulticastDNSUtils.getHostName();
        if (machineName == null) {
            host = MulticastDNSUtils.getHostName();
        } else {
            host = machineName.endsWith(".") ? machineName : machineName + ".";
        }
        switch (args[0]) {
            case "producer": {
                int port = Integer.parseInt(args[1]);
                Producer producer;
                producer = new Producer(new File(args[2]), port, host);
                producer.startQueriesListening();
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                    int read = System.in.read();
                    if (read == 'q') {
                        producer.stop();
                        break;
                    }
                }
                break;
            }
            case "consumer": {
                int port = Integer.parseInt(args[1]);
                Consumer consumer = new JSConsumer(port, host);
                consumer.registerServiceListener();
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                    int read = System.in.read();
                    if (read == 'q') {
                        consumer.stop();
                        break;
                    } else if (read == 'r') {
                        System.in.read();
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String filename = br.readLine();
                        consumer.requestFile(filename);
                    }
                }
                break;
            }
            default:
                System.err.println("Unknown option: " + args[0]);
                break;
        }
    }
}
