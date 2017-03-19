import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSHttpHandler implements HttpHandler {

    private JSConsumer jsConsumer;

    public JSHttpHandler(JSConsumer jsConsumer) {
        this.jsConsumer = jsConsumer;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(httpExchange.getProtocol());
        try {
            Gson gson = new Gson();
            JSInfo jsInfo = gson.fromJson(new InputStreamReader(httpExchange.getRequestBody()), JSInfo.class);
            List<URL> urls = jsConsumer.getProducersUrls();
            int subLength = (jsInfo.data.length() + urls.size() - 1) / urls.size();
            List<String> args = Arrays.asList(jsInfo.data.split("(?<=\\G.{" + subLength + "})"));

            List<Observable<String>> observables = new ArrayList<>();
            Observable.range(0, 2)
                    .map(i -> Observable.defer(() -> Observable.fromCallable(() -> {
                        System.out.println(Thread.currentThread());
                        JSInfo jsInfoForProducer = new JSInfo(args.get(i), jsInfo.map);
                        String jsInfoForProducerAsJson = gson.toJson(jsInfoForProducer);
                        try {
                            URL url = urls.get(i);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setRequestMethod("POST");
                            connection.addRequestProperty("Content-Type", "application/json");
                            connection.connect();
                            connection.getOutputStream().write(jsInfoForProducerAsJson.getBytes());
                            Result response = gson.fromJson(new InputStreamReader(connection.getInputStream()), Result.class);
                            return response.result;
                        } catch (Throwable t) {
                            t.printStackTrace();
                            throw t;
                        }
                    }).subscribeOn(Schedulers.newThread())
            )).toList().toBlocking().subscribe(observables::addAll);


            Observable.zip(observables, objects -> {
                List<String> strings = new ArrayList<>();
                for (Object object : objects) {
                    if (object instanceof String) {
                        strings.add((String) object);
                    }
                }
                return strings;
            }).toBlocking()
                    .subscribe(new Subscriber<List<String>>() {
                        @Override
                        public void onCompleted() {
                            System.out.println("onCompleted()");
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }

                        @Override
                        public void onNext(List<String> strings) {
                            try {
                                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                                engine.eval(jsInfo.reduce);
                                Invocable invocable = (Invocable) engine;
                                String result = (String) invocable.invokeFunction("reduce", strings);
                                httpExchange.sendResponseHeaders(200, result.length());
                                httpExchange.getResponseBody().write(result.getBytes());
                            } catch (ScriptException | NoSuchMethodException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            httpExchange.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class JSInfo {
        @SerializedName("data")
        String data;
        @SerializedName("map")
        String map;
        @SerializedName("reduce")
        String reduce;

        public JSInfo(String data, String map) {
            this.data = data;
            this.map = map;
        }
    }

    private static class Result {
        @SerializedName("result")
        String result;
    }
}
