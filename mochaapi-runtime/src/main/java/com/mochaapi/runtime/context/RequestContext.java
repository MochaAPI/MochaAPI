package com.mochaapi.runtime.context;

import java.util.Map;
import java.util.HashMap;

/**
 * Context object that holds information about the current HTTP request.
 */
public class RequestContext {
    
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> pathParams;
    private String body;
    private String contentType;
    
    public RequestContext() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.pathParams = new HashMap<>();
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }
    
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }
    
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }
    
    public void setQueryParam(String name, String value) {
        this.queryParams.put(name, value);
    }
    
    public Map<String, String> getPathParams() {
        return pathParams;
    }
    
    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }
    
    public String getPathParam(String name) {
        return pathParams.get(name);
    }
    
    public void setPathParam(String name, String value) {
        this.pathParams.put(name, value);
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
