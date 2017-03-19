import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.util.Set;

public class Producer extends MdnsService {

    private File directory;

    private Thread queriesListenerThread;

    private HttpServer server;

    public Producer(File directory, int port, String hostname) throws IOException {
        super("producer_" + port, "_http._tcp", "local.", port, hostname, "type=producer");
        this.directory = directory;
    }

    public void startQueriesListening() throws IOException {
        server = HttpServer.create(new InetSocketAddress(registeredService.getAddresses()[0], registeredService.getPort()), 0);
        server.createContext("/", new FileSendHandler(directory));
        server.start();

//        queriesListenerThread = new Thread(() -> {
//            try {
//
//
//
//                Selector selector = Selector.open();
//                ServerSocketChannel socketChannel = ServerSocketChannel.open();
//                InetAddress inetAddress = registeredService.getAddresses()[0];
//                InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, registeredService.getPort());
//                System.out.println("starting server on " + socketAddress.toString());
//                socketChannel.bind(socketAddress);
//                socketChannel.configureBlocking(false);
//                int ops = socketChannel.validOps();
//                socketChannel.register(selector, ops, null);
//                while (!Thread.currentThread().isInterrupted()) {
//                    selector.select();
//                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
//                    for (SelectionKey key : selectionKeySet) {
//                        if (key.isAcceptable()) {
//                            SocketChannel clientChannel = socketChannel.accept();
//                            clientChannel.configureBlocking(false);
//                            clientChannel.register(selector, SelectionKey.OP_READ);
//                            System.out.println("client: " + clientChannel.getLocalAddress());
//                        } else if (key.isReadable()) {
//                            SocketChannel clientChannel = (SocketChannel) key.channel();
//                            ByteBuffer byteBuffer = ByteBuffer.allocate(256);
//                            int read;
//                            String res = "";
//                            while ((read = clientChannel.read(byteBuffer)) > 0) {
//                                res += new String(byteBuffer.array(), 0, read);
//                                byteBuffer.clear();
//                            }
//                            System.out.println("received: " + res);
//                            if (res.startsWith("request") || res.startsWith("GET")) {
//                                String filename;
//                                if (res.startsWith("request")) {
//                                    filename = res.substring("request ".length());
//                                } else {
//                                    filename = res.substring("GET /".length(), res.indexOf(" HTTP/1.1"));
//                                }
//
//                                if (filename.endsWith("\n")) {
//                                    filename = filename.substring(0, filename.length() - 1);
//                                }
//                                System.out.println("trying to find " + filename);
//                                final String finalFilename = filename;
//                                File[] files = directory.listFiles(pathname -> {
//                                    return pathname.getName().equals(finalFilename);
//                                });
//                                if (files != null && files.length > 0) {
//                                    clientChannel.register(selector, SelectionKey.OP_WRITE, new MyFile(files[0]));
//                                } else {
//                                    System.err.println("close channel");
//                                    clientChannel.close();
//                                }
//                            } else {
//                                System.err.println("close channel");
//                                clientChannel.close();
//                            }
//                        } else if (key.isWritable()) {
//                            SocketChannel clientChannel = (SocketChannel) key.channel();
//                            MyFile file = (MyFile) key.attachment();
//                            FileChannel fileChannel = FileChannel.open(Paths.get(file.file.toURI()));
//                            long transferred = fileChannel.transferTo(file.position, 256, clientChannel);
//                            if (file.position + transferred >= fileChannel.size()) {
//                                System.out.println("sent, close");
//                                clientChannel.close();
//                            } else {
//                                System.out.println("sent " + (transferred + file.position) + " of " + fileChannel.size());
//                                file.position += transferred;
//                            }
//                        }
//                    }
//                    selectionKeySet.clear();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        queriesListenerThread.start();
    }

    public synchronized void stop() {
        super.stop();
        if (server != null) {
            server.stop(0);
        }
//        if (queriesListenerThread != null) {
//            queriesListenerThread.interrupt();
//        }
    }
}
