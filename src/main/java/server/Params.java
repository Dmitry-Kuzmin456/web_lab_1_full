package server;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Params {
    private float x;
    private BigDecimal y;
    private byte r;

    private final ArrayList<Float> xValues = new ArrayList<>(
            Arrays.asList(-2.0f, -1.5f, -1f, -0.5f, 0f, 0.5f, 1f, 1.5f, 2f)
    );
    private final ArrayList<Byte> rValues = new ArrayList<>(
            Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5)
    );

    private final Map<String, String> errors = new HashMap<>();

    public Params(String queryParams) {
        Map<String, String> params = new HashMap<>();
        String[] params_arr = queryParams.split("&");
        for (String p : params_arr) {
            String[] kv = p.split("=");
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }


        String sx = params.get("x");
        if (sx == null) errors.put("x", "missing");
        else {
            try {
                x = Float.parseFloat(sx);
                if (!xValues.contains(x)) errors.put("x", "must be one of [-2, -1.5, ..., 2]");
            } catch (Exception e) {
                errors.put("x", "must be a number");
            }
        }


        String sy = params.get("y");
        if (sy == null) errors.put("y", "missing");
        else {
            try {
                y = new BigDecimal(sy);
                if (y.compareTo(BigDecimal.valueOf(-5)) <= 0 || y.compareTo(BigDecimal.valueOf(5)) >= 0)
                    errors.put("y", "must be between (-5; 5)");
            } catch (Exception e) {
                errors.put("y", "must be a number");
            }
        }


        String sr = params.get("r");
        if (sr == null) errors.put("r", "missing");
        else {
            try {
                r = Byte.parseByte(sr);
                if (!rValues.contains(r)) errors.put("r", "must be in [1, 2, 3, 4, 5]");
            } catch (Exception e) {
                errors.put("r", "must be a number");
            }
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        Map<String, String> result = new HashMap<>();
        result.put("x", errors.getOrDefault("x", "ok"));
        result.put("y", errors.getOrDefault("y", "ok"));
        result.put("r", errors.getOrDefault("r", "ok"));
        return result;
    }

    public float getX() { return x; }
    public BigDecimal getY() { return y; }
    public byte getR() { return r; }
}
