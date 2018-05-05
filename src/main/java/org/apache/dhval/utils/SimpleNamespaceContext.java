package org.apache.dhval.utils;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {

    private static final Map<String, String> namespaceMap = Stream.of(
            new AbstractMap.SimpleEntry<>("ns1", "http://jnet.state.pa.us/message/jnet/OffenderInquiry/1"),
            new AbstractMap.SimpleEntry<>("ns2", "http://www.jnet.state.pa.us/niem/jnet/metadata/1"),
            new AbstractMap.SimpleEntry<>("ns3", "http://www.jnet.state.pa.us/niem/JNET/jnet-core/2"),
            new AbstractMap.SimpleEntry<>("ns4", "http://release.niem.gov/niem/niem-core/3.0/")
    ).collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));

    private final Map<String, String> PREF_MAP = new HashMap<String, String>();

    public SimpleNamespaceContext(final Map<String, String> prefMap) {
        PREF_MAP.putAll(prefMap);
    }

    public SimpleNamespaceContext() {
        PREF_MAP.putAll(namespaceMap);
    }

    public String getNamespaceURI(String prefix) {
        return PREF_MAP.get(prefix);
    }

    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}
