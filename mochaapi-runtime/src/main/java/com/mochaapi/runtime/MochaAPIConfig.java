package com.mochaapi.runtime;

/**
 * Configuration class for MochaAPI applications.
 */
public class MochaAPIConfig {
    
    private String host = "0.0.0.0";
    private int port = 8080;
    private boolean enableMetrics = true;
    private boolean enableHealthCheck = true;
    private boolean enableOpenApi = true;
    private String openApiPath = "/docs";
    private int maxThreads = Runtime.getRuntime().availableProcessors();
    private int cpuBoundThreads = Runtime.getRuntime().availableProcessors();
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public boolean isEnableMetrics() {
        return enableMetrics;
    }
    
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }
    
    public boolean isEnableHealthCheck() {
        return enableHealthCheck;
    }
    
    public void setEnableHealthCheck(boolean enableHealthCheck) {
        this.enableHealthCheck = enableHealthCheck;
    }
    
    public boolean isEnableOpenApi() {
        return enableOpenApi;
    }
    
    public void setEnableOpenApi(boolean enableOpenApi) {
        this.enableOpenApi = enableOpenApi;
    }
    
    public String getOpenApiPath() {
        return openApiPath;
    }
    
    public void setOpenApiPath(String openApiPath) {
        this.openApiPath = openApiPath;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }
    
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    public int getCpuBoundThreads() {
        return cpuBoundThreads;
    }
    
    public void setCpuBoundThreads(int cpuBoundThreads) {
        this.cpuBoundThreads = cpuBoundThreads;
    }
}
