package com.dhval.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TcpProxy {

    Integer listenPort;
    Integer targetPort;
    String targetHost;

    public TcpProxy(Integer listenPort, Integer targetPort, String targetHost) {
        this.listenPort = listenPort;
        this.targetPort = targetPort;
        this.targetHost = targetHost;
    }

    public TcpProxy(Map<String, Object> map) {
        listenPort = map.containsKey("listenPort") ? (Integer) map.get("listenPort") : 0;
        targetPort = map.containsKey("targetPort") ? (Integer) map.get("targetPort") : 0;
        targetHost =  map.containsKey("targetHost") ?  (String) map.get("targetHost") : null;
    }

    public static List<TcpProxy> buildProxies(Map jsonMap) {
        List<TcpProxy> list = new ArrayList<>();
        if (jsonMap == null || !jsonMap.containsKey("tcpProxies"))
            return list;
        List<Map<String, Object>> proxyList = (List<Map<String, Object>>) jsonMap.get("tcpProxies");
        return proxyList.stream().filter(m -> m.get("enabled").equals("yes")).map(TcpProxy::new).collect(Collectors.toList());
    }

    public Integer getListenPort() {
        return listenPort;
    }

    public Integer getTargetPort() {
        return targetPort;
    }

    public String getTargetHost() {
        return targetHost;
    }
}
