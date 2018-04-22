package org.apache.dhval.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler that echos the incoming request entity.
 */
public class EchoHandler implements  HttpRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(EchoHandler.class);

    private List<String> files = new ArrayList<>();
    private Random random = new Random();

    public void addFiles(List<String> list) {
        files.addAll(list);
    }

    public void addFile(String file) {
        files.add(file);
    }


    /**
     * Handles a request by echoing the incoming request entity.
     * If there is no request entity, an empty document is returned.
     *
     * @param request   the request
     * @param response  the response
     * @param context   the context
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    public void handle(final HttpRequest request,
                       final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {

        String method = request.getRequestLine().getMethod()
                .toUpperCase(Locale.ENGLISH);
        if (!"GET".equals(method) && !"POST".equals(method)
                && !"PUT".equals(method)) {
            throw new MethodNotSupportedException(method
                    + " not supported by " + getClass().getName());
        }

        HttpEntity entity = null;
        if (request instanceof  HttpEntityEnclosingRequest)
            entity = ((HttpEntityEnclosingRequest) request).getEntity();

        byte[] data;
        if (!files.isEmpty()) {
            Path path = Paths.get(pickFile());
            data = Files.readAllBytes(path);
        } else if (entity == null) {
            data = new byte[0];
        } else {
            data = EntityUtils.toByteArray(entity);
        }

        ByteArrayEntity bae = new ByteArrayEntity(data);
        if (entity != null) {
            bae.setContentType(entity.getContentType());
        }
        entity = bae;

        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(entity);

    }

    private String pickFile() {
       String file =  files.get(random.nextInt(files.size()));
       LOG.info("Serving file: " + file);
       return file;
    }

}
