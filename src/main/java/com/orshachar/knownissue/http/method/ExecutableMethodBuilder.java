package com.orshachar.knownissue.http.method;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.orshachar.knownissue.http.auth.Auth;
import com.orshachar.knownissue.http.auth.OAuth;
import com.orshachar.knownissue.http.exceptions.MethodHttpException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.String.format;

public class ExecutableMethodBuilder {

        private static final String UNEXPECTED_STATUS_CODE_MSG = "Unexpected status code for %s to %s. Expected ( %d ), actual ( %d ).";
        private static final String IO_EXCEPTION_MSG = "IOException occurred reading response of %s to %s.\n Status line: %s";
        private static final String DESERIALIZE_ERROR_MSG = "Failed to deserialize json: %s to class: %s";

        private static final Logger logger = LoggerFactory.getLogger(ExecutableMethodBuilder.class);

        private ObjectMapper objectMapper;
        private HttpRequestBase request;
        private Auth auth;
        private int expectedCode;

        private static CloseableHttpClient client;

        public CloseableHttpClient getClient() {
            return client;
        }

    public ExecutableMethodBuilder(ObjectMapper objectMapper, HttpRequestBase request, Auth auth, int expectedCode,
                                   LayeredConnectionSocketFactory sslSocketFactory) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.expectedCode = expectedCode;
        this.auth = auth;

        setClient(sslSocketFactory);
    }

    private synchronized void setClient(LayeredConnectionSocketFactory sslSocketFactory) {
        if (client == null) {
            client = HttpClientBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
        }
    }


    public <T> T execute(Class<T> tClass) throws MethodHttpException {
        HttpResponse response = executeInner();
        if (tClass == File.class) {
            return (T) downloadFile(response);
        }
        return convertBody(response, tClass);
    }

    public <T> T execute(TypeReference<T> type) throws MethodHttpException {
        HttpResponse response = executeInner();
        return convertBody(response, type);
    }

    public void execute() throws MethodHttpException {
        executeInner();
        request.releaseConnection();
    }

    public ExecutableMethodBuilder withAuth(Auth auth) {
        this.auth = auth;
        return this;
    }

    private HttpResponse executeInner() throws MethodHttpException {

        HttpRequestBase requestToExecute = null;
        try {
            requestToExecute = (HttpRequestBase) request.clone();

            HttpContext localContext = new BasicHttpContext();
            CookieStore cookieStore = new BasicCookieStore();

            logRequest(requestToExecute, expectedCode);

            if (auth != null) {
                auth.addAuth(requestToExecute, cookieStore);
            }

            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);


            HttpResponse response = client.execute(requestToExecute, localContext);

            // If OAuth and need to refresh token
            if (auth != null && auth instanceof OAuth && ((OAuth) auth).doRefresh(response)) {
                requestToExecute = (HttpRequestBase) request.clone();
                response = client.execute(requestToExecute, localContext);
            }

            int statusCode = response.getStatusLine().getStatusCode();

            if (isToPrint() && statusCode == expectedCode) {
                logger.error("Request Headers:\n" +
                        "**************\n" + prettyHeaders(requestToExecute.getAllHeaders()));
                if (!isGetMethod()) {
                    logger.error("Request Body:\n" +
                            "**************\n" +
                            prettyBody(EntityUtils.toString(((HttpEntityEnclosingRequest) request).getEntity())));
                }
            }

            if (expectedCode != MethodBuilder.IGNORE_STATUS_CODE && statusCode != expectedCode) {
                String body = extractStringBody(response);
                String errorMessage = format(UNEXPECTED_STATUS_CODE_MSG, requestToExecute.getMethod(), requestToExecute.getURI(), expectedCode, statusCode);

                logger.error(errorMessage);
                if (requestToExecute instanceof HttpEntityEnclosingRequest && ((HttpEntityEnclosingRequest) requestToExecute).getEntity() != null) {
                    if (!isGetMethod()) {
                        throw new MethodHttpException(errorMessage + "\n" +
                                "\nRequest Headers:\n" +
                                "**************\n" + prettyHeaders(requestToExecute.getAllHeaders()) +
                                "\nRequest Body:\n" +
                                "**************\n" + prettyBody(EntityUtils.toString(((HttpEntityEnclosingRequest) requestToExecute).getEntity())) +
                                "\nResponse Headers:\n" +
                                "**************\n" + prettyHeaders(response.getAllHeaders()) +
                                "\nResponse Body:\n" +
                                "**************\n" + prettyBody(body), statusCode);
                    }
                }

                throw new MethodHttpException(errorMessage +
                        "\nRequest Headers:\n**************\n" + prettyHeaders(requestToExecute.getAllHeaders()) +
                        "\nResponse Headers:\n**************\n" + prettyHeaders(response.getAllHeaders()) +
                        "\nResponse Body:\n**************\n" + prettyBody(body), statusCode);
            }

            return response;
        } catch (IOException e) {
            if (requestToExecute != null) {
                requestToExecute.releaseConnection();
            }
            String message = format("IOException executing %s request to %s. Original msg: %s", request.getMethod(), request.getURI(), e.getMessage());
            throw new MethodHttpException(message, e);
        } catch (CloneNotSupportedException e) {
            String message = format("Unable to clone request %s request to %s. Original msg: %s", request.getMethod(), request.getURI(), e.getMessage());
            throw new MethodHttpException(message, e);

            }

    }

    private String extractStringBody(HttpResponse response) throws MethodHttpException {
        String method = request.getMethod();
        try {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            String responseBody = EntityUtils.toString(entity);

            return responseBody;
        } catch (Exception e) {
            String message = format(IO_EXCEPTION_MSG, method, request.getURI(), response.getStatusLine());
            logger.error(message);
            throw new MethodHttpException(message, e);
        } finally {
            request.releaseConnection();
        }
    }

    private void logRequest(HttpRequestBase req, int statusCode) {
        if (auth != null) {
            logger.info(format("%s to %s as %s. Status code: %s", req.getMethod(), req.getURI().toString(), auth.asText(), statusCode));
        } else {
            logger.info(format("%s to %s. Status code: %s", req.getMethod(), req.getURI().toString(), statusCode));
        }
    }


    private String parseETagHeader(String etag) {
        String str = ", \"" + ETAG + "\" : ";

        try {
            return str + Long.parseLong(etag) + " }";
        } catch (Exception e) {
        }
        return str + "\"" + etag + "\" }";
    }

    private final String ETAG = "ETag";
    private final String REDIRECT_URI = "redirectUri";
    private final String LOCATION = "Location";

    private <T> T convertBody(HttpResponse response, Class<T> tClass) throws MethodHttpException {

        String body = extractStringBody(response);

        if (response.getHeaders(ETAG).length > 0 && body.length() > 0) {
            body = body.substring(0, body.length() - 1) + parseETagHeader(response.getHeaders(ETAG)[0].getValue());
        }

        if (isToPrint() && response.getStatusLine().getStatusCode() == expectedCode) {
            logger.warn("Response Headers:\n" +
                    "**************\n" + prettyHeaders(response.getAllHeaders()));
            logger.warn("Response Body:\n" +
                    "**************\n" + prettyBody(body));
        }

        if (Strings.isNullOrEmpty(body) &&
                (tClass == String.class) &&
                (response.getHeaders(LOCATION).length > 0) &&
                (request.getURI().getQuery().contains(REDIRECT_URI)) &&
                (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)) {
            return (T) response.getHeaders(LOCATION)[0].getValue();
        }

        if (!shouldConvertBody(response, body)) {
            return null;
        }
        // To handle V3 APIs
        if (body.startsWith("throw 'allowIllegalResourceCall")) {
            body = body.substring(body.indexOf(";") + 1);
        }

        if (tClass == void.class) {
            return null;
        } else if (tClass == String.class) {
            return (T) body;
        } else if (tClass == HttpEntity.class) {
            return (T) response.getEntity();
        } else {
            try {
                return getObjectMapper().readValue(body, tClass);
            } catch (Exception e) {
                String message = format(DESERIALIZE_ERROR_MSG, body, tClass.toString());
                logger.error("Failed to deserialize response json: " + prettyBody(body) + " to Class: " + tClass.toString(), e);
                throw new MethodHttpException(message, e);
            }
        }
    }

    private <T> T convertBody(HttpResponse response, TypeReference<T> type) throws MethodHttpException {
        String body = extractStringBody(response);

        if (response.getHeaders(ETAG).length > 0) {
            body = body.substring(0, body.length() - 1) + parseETagHeader(response.getHeaders(ETAG)[0].getValue());
        }

        if (isToPrint() && response.getStatusLine().getStatusCode() == expectedCode) {
            logger.warn("Response Headers:\n" +
                    "**************\n" + prettyHeaders(response.getAllHeaders()));
            logger.warn("Response Body:\n" +
                    "**************\n" + prettyBody(body));
        }

        if (!shouldConvertBody(response, body)) {
            return null;
        }

        if (Strings.isNullOrEmpty(body) &&
                (response.getHeaders(LOCATION).length > 0) &&
                (request.getURI().getQuery().contains(REDIRECT_URI)) &&
                (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)) {
            return (T) response.getHeaders(LOCATION)[0].getValue();
        }

        // To handle V3 APIs
        if (body.startsWith("throw 'allowIllegalResourceCall")) {
            body = body.substring(body.indexOf(";") + 1);
        }

        try {

            if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299) {
                return getObjectMapper().readValue(body, type);
            } else {
                return null;
            }
        } catch (Exception e) {
            String message = format(DESERIALIZE_ERROR_MSG, body, type.getClass().toString());
            logger.error("Failed to deserialize response json: " + body + " to Class: " + type.getClass().toString(), e);
            throw new MethodHttpException(message, e);
        }
    }

    private boolean shouldConvertBody(HttpResponse response, String body) {
        if ((Strings.isNullOrEmpty(body)) || // Empty body
                ((response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299))) { // Error code
            return false;
        }
        return true;
    }

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private String prettyHeaders(Header[] headers) {
        String pretty = "";

        for (int i = 0; i < headers.length; i++) {
            pretty += headers[i].getName() + ":" + headers[i].getValue() + "\n";
        }

        return pretty;
    }

    private String prettyBody(String body) {
        try {
            if (body.startsWith("{") || body.startsWith("[")) { // Json
                ObjectMapper mapper = getObjectMapper();
                Object json = mapper.readValue(body, Object.class);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            }
        } catch (Exception e) {
            // Do nothing
        }
        return body;
    }

    private File downloadFile(HttpResponse response) {
        File dir = new File("downloadedFiles");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File outputFile = new File("downloadedFiles/temp" + RandomStringUtils.randomAlphanumeric(3));
        try {

            IOUtils.copyLarge(response.getEntity().getContent(), new FileOutputStream(outputFile));
            return outputFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            request.releaseConnection();
        }
    }

    public boolean isToPrint() {
        if (toPrint) {
            logger.warn("Print request & response: ACTIVATED");
        }
        return toPrint;
    }

    private boolean isGetMethod() {
        return request.getMethod().equals("GET") ? true : false;
    }

    public void setToPrint(boolean toPrint) {
        this.toPrint = toPrint;
    }

    private boolean toPrint = false;

    public ExecutableMethodBuilder toPrintRequestAndResponse(boolean toPrintRequestAndResponse) throws MethodHttpException {
        setToPrint(toPrintRequestAndResponse);
        return this;
    }

}