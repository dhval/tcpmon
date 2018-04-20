package com.dhval.echo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

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

/**
 * A handler that echos the incoming request entity.
 */
public class EchoHandler implements  HttpRequestHandler {

    public EchoHandler() {}
    public EchoHandler(String file) {
        this.file = file;
    }

    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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
        if (file != null) {
            Path path = Paths.get(file);
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

}
