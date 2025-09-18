package server.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="requests")
public class Requests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private float x;

    @Column(nullable = false, precision = 130, scale = 100)
    private BigDecimal y;

    @Column(nullable = false)
    private byte r;

    @Column(nullable = false)
    private boolean result;

    @Column(nullable = false)
    private long executionTime;

    @Column(nullable = false)
    private String serverTime;


    public Requests() {}

    public Requests(float x, BigDecimal y, byte r, boolean result, String serverTime, long executionTime) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.result = result;
        this.serverTime = serverTime;
        this.executionTime = executionTime;
    }


    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public byte getR() {
        return r;
    }

    public boolean isResult() {
        return result;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public void setR(byte r) {
        this.r = r;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
}
