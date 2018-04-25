package org.apache.dhval.storage;

import org.apache.dhval.client.Sender;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.QueueLong;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * https://jankotek.gitbooks.io/mapdb/content/quick-start/
 */
@Configuration
public class LocalDB {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

    public static String LAST_OPEN_DIRECTORY;

    private DB db = DBMaker.fileDB("tcpmon.db").make();
    private Set<String> fileHistory;
    private Map<String, String> history;

    @PostConstruct
    void initDB() {
        fileHistory = db.hashSet("file-history", Serializer.STRING).expireMaxSize(10).expireAfterGet().createOrOpen();
        history = db.hashMap("history", Serializer.STRING, Serializer.STRING).createOrOpen();
    }

    public void saveFileHistory(String fileName) {
        fileHistory.add(fileName);
        db.commit();
    }

    public Set<String> getFileHistory() {
        return fileHistory;
    }

    public void saveHistory(String k, String v) {
        history.put(k, v);
        db.commit();
    }

    public String getFromHistory(String k) {
        return history.get(k);
    }

    // ConcurrentMap map = db.hashMap("map").createOrOpen();
    // LOG.info(map.get("json").toString());
    //map.put("json", jsonMap);
    //db.commit();


    @PreDestroy
    public void close() {
        if (db.isClosed())
            return;
        for(String f : getFileHistory())
            LOG.info(f);
        LOG.info("Closing file store.");
        db.close();
    }
}
