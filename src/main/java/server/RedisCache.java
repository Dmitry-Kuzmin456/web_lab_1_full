package server;

import redis.clients.jedis.Jedis;
import server.model.Requests;
import java.util.ArrayList;
import java.util.List;

public class RedisCache {
    private Jedis jedis;

    public RedisCache() {
        this.jedis = new Jedis("redis", 6379);
    }

    public void addRequest(Requests req) {
        String value = req.getX() + ";" +
                req.getY().toPlainString() + ";" +
                req.getR() + ";" +
                req.isResult() + ";" +
                req.getExecutionTime() + ";" +
                req.getServerTime();

        jedis.rpush("requests", value);
    }

    public List<String> getHistoryRaw() {
        return jedis.lrange("requests", 0, -1);
    }

    public List<Requests> getHistory() {
        List<String> raw = getHistoryRaw();
        List<Requests> result = new ArrayList<>();

        for (String entry : raw) {
            String[] parts = entry.split(";");
            Requests r = new Requests(
                    Float.parseFloat(parts[0]),
                    new java.math.BigDecimal(parts[1]),
                    Byte.parseByte(parts[2]),
                    Boolean.parseBoolean(parts[3]),
                    parts[5], // serverTime
                    Long.parseLong(parts[4]) // executionTime
            );
            result.add(r);
        }

        return result;
    }

    public void clear() {
        jedis.del("requests");
    }
}
