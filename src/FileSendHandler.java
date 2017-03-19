import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileSendHandler implements HttpHandler {
    private File directory;

    public FileSendHandler(File directory) {
        this.directory = directory;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String filename = httpExchange.getRequestURI().getPath().substring(1);
        File fileToSend = new File(directory, filename);
        httpExchange.sendResponseHeaders(200, fileToSend.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        Files.copy(fileToSend.toPath(), outputStream);
        httpExchange.close();
    }
}
