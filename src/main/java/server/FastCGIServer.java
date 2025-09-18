package server;

import com.fastcgi.FCGIInterface;
import com.fastcgi.FCGIRequest;
import server.loaders.FileLoader;
import server.model.Requests;
import server.repositories.RequestsRepo;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public class FastCGIServer {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = Logger.getLogger(FastCGIServer.class.getName());

    public static void main(String[] args) throws Exception {

        RequestsRepo repo = null;

        for (int i = 0; i < 5; i++) {
            try {
                repo = new RequestsRepo();
                break;
            } catch (Exception e) {
                if (i == 4) {
                    System.exit(1);
                }
                Thread.sleep(1000);
            }
        }

        FCGIInterface fcgi = new FCGIInterface();
        FileLoader jsonLoader = new FileLoader("json");
        FileLoader responseLoader = new FileLoader("responses");

        while (fcgi.FCGIaccept() >= 0) {
            FCGIRequest request = FCGIInterface.request;

            String requestMethod = request.params.getProperty("REQUEST_METHOD", "GET");

            if (!"GET".equalsIgnoreCase(requestMethod)) {
                String serverTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER);
                String errorBody = String.format(jsonLoader.load("error_json"), serverTime, "Метод не поддерживается");
                String response = String.format(responseLoader.load("error_response"), errorBody);
                System.out.print(response);
                System.out.flush();
                continue;
            }

            try {
                String queryString = request.params.getProperty("QUERY_STRING", "");
                String decodedQuery = URLDecoder.decode(queryString, StandardCharsets.UTF_8);

                String response;
                List<Requests> history = repo.getAllRequests();

                if (decodedQuery.contains("action=history")) {
                    String responseJson = FastCGIServer.getResultsTable(history, jsonLoader, repo);
                    response = FastCGIServer.getSuccessResponse(responseLoader, responseJson);
                } else {
                    Params params = new Params(decodedQuery);
                    response = FastCGIServer.getResponse(jsonLoader, responseLoader, repo, params);
                }

                System.out.print(response);
                System.out.flush();

            } catch (Exception e) {
                String serverTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER);
                String errorBody = String.format(jsonLoader.load("error_json"), serverTime, e.getMessage());
                String response = String.format(responseLoader.load("error_response"), errorBody);

                System.out.print(response);
                System.out.flush();
            }
        }
    }

    private static boolean calculate(float x, BigDecimal y, byte r){
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal rBD = BigDecimal.valueOf(r);

        if (BigDecimal.valueOf(x).compareTo(zero) <= 0 &&
                y.compareTo(zero) >= 0 &&
                y.compareTo(BigDecimal.valueOf(x).add(rBD)) <= 0)
            return true;

        if (BigDecimal.valueOf(x).compareTo(zero) <= 0 &&
                y.compareTo(zero) <= 0 &&
                BigDecimal.valueOf(x).compareTo(rBD.negate()) >= 0 &&
                y.compareTo(rBD.negate()) >= 0)
            return true;

        BigDecimal xBD = BigDecimal.valueOf(x);
        if (xBD.compareTo(zero) >= 0 &&
                y.compareTo(zero) <= 0 &&
                xBD.pow(2).add(y.pow(2)).compareTo(rBD.pow(2)) <= 0)
            return true;

        return false;
    }


    private static String getResultsTable(List<Requests> history, FileLoader jsonLoader, RequestsRepo repo) throws Exception {
        String jsonPattern = jsonLoader.load("start_table_json");
        StringBuilder jsonArray = new StringBuilder("[");
        for (int i = 0; i < history.size(); i++) {
            Requests r = history.get(i);
            String yValue = r.getY().stripTrailingZeros().toPlainString();
            jsonArray.append(String.format(
                    jsonPattern,
                    r.getX(), "\"" + yValue + "\"",
                    r.getR(), r.isResult(), r.getExecutionTime(),
                    r.getServerTime()
            ));
            if (i < history.size() - 1) jsonArray.append(",");
        }
        jsonArray.append("]");
        return jsonArray.toString();
    }

    private static String getSuccessResponse(FileLoader responseLoader, String json){
        return String.format(responseLoader.load("success_response"), json);
    }

    private static String getResponse(FileLoader jsonLoader, FileLoader responseLoader, RequestsRepo repo, Params params) throws Exception {
        long startTime = System.nanoTime();

        if (params.hasErrors()) {
            Map<String, String> errors = params.getErrors();
            String jsonErrors = String.format(
                    jsonLoader.load("error_json"),
                    errors.get("x"),
                    errors.get("y"),
                    errors.get("r"),
                    LocalDateTime.now().format(TIME_FORMATTER)
            );
            return String.format(responseLoader.load("error_response"), jsonErrors);
        }

        boolean hit = calculate(params.getX(), params.getY(), params.getR());
        long duration = System.nanoTime() - startTime;
        String serverTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER);

        repo.addRequest(new Requests(params.getX(), params.getY(), params.getR(), hit, serverTime, duration));
        List<Requests> history = repo.getAllRequests();
        String historyJson = FastCGIServer.getResultsTable(history, jsonLoader, repo);

        return FastCGIServer.getSuccessResponse(responseLoader, historyJson);
    }
}
