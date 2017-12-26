package com.orshachar.knownissue.http.method;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orshachar.knownissue.http.auth.Auth;
import com.orshachar.knownissue.http.exceptions.MethodBuildException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.lang.String.format;

public class MethodBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MethodBuilder.class);

    private static final String PARSE_BODY_FAILED = "Failed to parse JSON from body: %s";

    public static final int IGNORE_STATUS_CODE = -1;

    private static LayeredConnectionSocketFactory sslSocketFactory;

    private HttpRequestBase request;
    private String uri;
    private Auth auth;
    private ObjectMapper objectMapper;

    private Map<String, String> headers = Maps.newHashMap();
    private List<NameValuePair> params = Lists.newArrayList();
    private int statusCode = IGNORE_STATUS_CODE;

    public static void setSslSocketFactory(LayeredConnectionSocketFactory sslSocketFactory) {
        MethodBuilder.sslSocketFactory = sslSocketFactory;
    }

    public static LayeredConnectionSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public static MethodBuilder newPost(String uri) {
        return new MethodBuilder(new HttpPost(), uri);
    }

    public static MethodBuilder newPut(String uri) {
        return new MethodBuilder(new HttpPut(), uri);
    }

    public static MethodBuilder newPatch(String uri) {
        return new MethodBuilder(new HttpPatch(), uri);
    }

    public static MethodBuilder newGet(String uri) {
        return new MethodBuilder(new HttpGet(), uri);
    }

    public static MethodBuilder newDelete(String uri) {
        return new MethodBuilder(new HttpDelete(), uri);
    }

    public static MethodBuilder newHead(String uri) {
        return new MethodBuilder(new HttpHead(), uri);
    }

    private MethodBuilder(HttpRequestBase request, String uri) {
        this.request = request;
        this.uri = uri;
        auth = null;
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public MethodBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public MethodBuilder withAuth(Auth auth) {
        this.auth = auth;
        return this;
    }

    public MethodBuilder withHeader(Header header) {
        headers.put(header.getName(), header.getValue());
        return this;
    }

    public MethodBuilder withHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public MethodBuilder withContentType(ContentType contentType) {
        headers.put("Content-Type", contentType.getMimeType());
        return this;
    }

    public MethodBuilder removeHeader(String name) {
        headers.remove(name);
        return this;
    }

    public MethodBuilder withBody(String body) {
        if (request instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(body, Charset.forName("UTF-8")));
        } else {
            throw new MethodBuildException("Can only assign body to Post or Put methods.");
        }
        return this;
    }

    public MethodBuilder withBody(Object body) {
        try {
            return withBody(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            logger.error(format(PARSE_BODY_FAILED, body.toString()), e);
            throw new MethodBuildException(format(PARSE_BODY_FAILED, body.toString()), e);
        }
    }

    public MethodBuilder withUrlEncodedParams(List<NameValuePair> parameters) {
        try {
            ((HttpEntityEnclosingRequest) request).setEntity(new UrlEncodedFormEntity(parameters));
        } catch (UnsupportedEncodingException e) {
            logger.error(format(PARSE_BODY_FAILED, parameters.toString()), e);
            throw new MethodBuildException(format(PARSE_BODY_FAILED, parameters.toString()), e);
        }
        return this;
    }

    public MethodBuilder withFile(File file, ContentType mimeType) {
        if (file != null) {
            FileEntity fileEntity = new FileEntity(file, mimeType);
            if (request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) request).setEntity(fileEntity);
            } else {
                throw new MethodBuildException("Can only File to Post or Put methods.");
            }
        }
        return this;
    }

    public MethodBuilder withFormParams(List<NameValuePair> formParams) {
        if (formParams.size() > 0) {
            UrlEncodedFormEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(formParams, "UTF-8");
                ((HttpEntityEnclosingRequest) request).setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to add form params to request", e);
                throw new MethodBuildException("Failed to add form params to request", e);
            }
        }
        return this;
    }

    public MethodBuilder expectedCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getHeaderValue(String name) {
        return headers.get(name);
    }

    public MethodBuilder withParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
        return this;
    }

    public MethodBuilder removeParam(String name, String value) {
        params.remove(new BasicNameValuePair(name, value));
        return this;
    }

    public MethodBuilder clearParams() {
        params.clear();
        return this;
    }

    public MethodBuilder withObjectMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
        return this;
    }

    public ExecutableMethodBuilder build() {
        request.setURI(buildURI());
        addHeaders(request);

        return new ExecutableMethodBuilder(objectMapper, request, auth, statusCode, sslSocketFactory);
    }

    private URI buildURI() {
        try {
            URIBuilder builder = new URIBuilder(uri);
            for (NameValuePair param : params) {
                builder = builder.addParameter(param.getName(), param.getValue());
            }
            URI uri = builder.build();
            uri = new URI(StringUtils.replace(uri.toString(), "+", "%20"));
            return uri;

        } catch (URISyntaxException e) {
            logger.error("Failed to build URI from uri: " + uri, e);
            throw new MethodBuildException("Failed to build URI from uri: " + uri, e);
        }
    }

    private void addHeaders(HttpRequestBase request) {
        for (String headerName : headers.keySet()) {
            request.addHeader(headerName, headers.get(headerName));
        }
    }
}
