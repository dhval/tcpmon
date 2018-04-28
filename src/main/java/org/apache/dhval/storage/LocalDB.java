package org.apache.dhval.storage;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import rx.Subscriber;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * https://jankotek.gitbooks.io/mapdb/content/quick-start/
 */
@Configuration
public class LocalDB {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDB.class);

    public static String LAST_OPEN_DIRECTORY = "LAST_OPEN_DIRECTORY";
    public static String LAST_WS_ENDPOINT = "LAST_WS_ENDPOINT";
    public static String LAST_WSS_PROFILE = "LAST_WSS_PROFILE";
    public static String KEY_STORE_LOCATION = "KEY_STORE_LOCATION";
    public static String KEY_STORE_ALIAS = "KEY_STORE_ALIAS";

    private int opnCounter = 0;

    private DB db = DBMaker.fileDB("tcpmon.db").closeOnJvmShutdown().checksumHeaderBypass().make();
    private Set<String> fileHistory;
    private Map<String, String> history;
    private Map<Integer, String> requesthistory;
    private Map<Integer, String> responsehistory;

    private int reqRespCounter = 0;

    private PublishSubject<Map.Entry<String, String>> publishSubject =  PublishSubject.create();

    @PostConstruct
    void initDB() {
        fileHistory = db.hashSet("file-history", Serializer.STRING).expireMaxSize(10).expireAfterGet().createOrOpen();
        history = db.hashMap("history", Serializer.STRING, Serializer.STRING).createOrOpen();
        requesthistory = db.hashMap("request-history", Serializer.INTEGER, Serializer.STRING).expireMaxSize(100).counterEnable().createOrOpen();
        responsehistory = db.hashMap("response-history", Serializer.INTEGER, Serializer.STRING).expireMaxSize(100).counterEnable().createOrOpen();
        reqRespCounter = requesthistory.size();

        publishSubject.debounce(2, TimeUnit.SECONDS).subscribe(new Subscriber<Map.Entry<String, String>>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable throwable) { }

            @Override
            public void onNext(Map.Entry<String, String> entry) {
                LOG.info(entry.getKey() + ":" + entry.getValue());
                saveHistory(entry.getKey(), entry.getValue());
            }
        });
        LOG.info(history.get(KEY_STORE_LOCATION));
    }

    public void saveFileHistory(String fileName) {
        fileHistory.add(fileName);
        commit();
    }

    public Set<String> getFileHistory() {
        return fileHistory;
    }

    public void saveHistory(String k, String v) {
        history.put(k, v);
        commit();
    }

    public String getHistory(String k) {
        return history.get(k);
    }

    public String getHistory(String k, String def) {
        String v = history.get(k);
        return v != null ?  v:def;
    }

    public void saveRequestResponse(String r1, String r2) {
        if (++reqRespCounter >= 98) reqRespCounter =0;
        requesthistory.put(reqRespCounter, r1);
        responsehistory.put(reqRespCounter, r2);
    }

    public List<Integer> getAvailableRequestResponse() {
        return requesthistory.keySet().stream().collect(Collectors.toList());
    }

    public String getRequestHistory(Integer id) {
        return requesthistory.get(id);
    }

    public String getResponseHistory(Integer id) {
        return responsehistory.get(id);
    }

    private void commit() {
        if (++opnCounter >= 10) {
            opnCounter = 0;
            db.commit();
        }
    }

    public void publish(String k, String v) {
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(k, v);
        publishSubject.onNext(entry);
    }

    @PreDestroy
    public void close() {
        if (db.isClosed())
            return;
        db.commit();
        db.close();
    }
}
