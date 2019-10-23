package it.islandofcode.jbiblioscan.net;

public interface ProcessNetData {
    String DISCONNECTION = "BYE";
    String PONG = "PONG";
    String RECEIVED = "RECEIVED";
    String ERROR = "ERROR";
    String UNKNOW = "UNKNOW";

    void process(String result);
}
