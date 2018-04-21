package com.dhval.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalServer {

    Integer listenPort;
    String listenHost;
    String fileToServer;
    String pathURI;
    Boolean isEnabled;

    public static LocalServer buildFromMap(Map jsonMap) {
        if (jsonMap == null || !jsonMap.containsKey("mockServer"))
            return new LocalServer(7832, "127.0.0.1", "sample.xml", "/echo/*", false);
        Map<String, Object> map = (Map<String, Object>) jsonMap.get("mockServer");
        Integer listenPort = map.containsKey("listenPort") ? (Integer) map.get("listenPort") : 7832;
        String fileToServer = map.containsKey("filesToServe") ? (String) map.get("filesToServe") : "sample.xml";
        String pathURI = map.containsKey("pathURI") ? (String) map.get("pathURI") : "/echo/*";
        String listenHost =  map.containsKey("listenHost") ?  (String) map.get("listenHost") : null;
        Boolean enable =  map.containsKey("enabled") && map.get("enabled").equals("yes") ?  true : false;
        return new LocalServer(listenPort, listenHost, fileToServer, pathURI, enable);
    }

    public LocalServer(Integer listenPort, String listenHost, String fileToServer,String pathURI, Boolean enabled) {
        this.listenPort = listenPort;
        this.listenHost = listenHost;
        this.fileToServer = fileToServer;
        this.isEnabled = enabled;
        this.pathURI = pathURI;
    }

    public Integer getListenPort() {
        return listenPort;
    }

    public String getListenHost() {
        return listenHost;
    }

    public String getFileToServer() {
        return fileToServer;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public String getPathURI() {
        return pathURI;
    }
}
