package vn.thanhtd.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Collector.*;
import io.prometheus.client.Histogram.Timer;

public class PrometheusFilter implements Filter {

    private static Histogram requestLatencyHistogram = Histogram
            .build("request_latency_seconds", "Request latency by second")
            .buckets(0.1, 0.2, 0.3, 0.5, 0.7, 0.9, 1, 1.5, 2).labelNames("app","instance","api").register();
    private static Gauge requestAPIStatus = Gauge.build("request_api_status", "request status of api")
            .labelNames("app","instance","api", "status").register();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Go Through filter");
        HttpServletRequest req = (HttpServletRequest) request;
        String pathInfo = req.getRequestURI();
        String hostName = request.getRemoteHost();

        if (pathInfo.equals("/metrics")) {
            chain.doFilter(request, response);
            return;
        }
        Timer timer = requestLatencyHistogram.labels("default",hostName,pathInfo).startTimer();
        chain.doFilter(request, response);
        timer.observeDuration();
        HttpServletResponse resp = (HttpServletResponse) response;
        requestAPIStatus.labels("default",hostName,pathInfo, String.valueOf(resp.getStatus())).inc();
    }

    public static List<MetricFamilySamples> getMetrics() {
        return requestLatencyHistogram.collect();
    }

    public static CollectorRegistry getRegistry() {
        CollectorRegistry collectorRegistry = new CollectorRegistry();
        collectorRegistry.register(requestAPIStatus);
        collectorRegistry.register(requestLatencyHistogram);
        return collectorRegistry;
    }

    @Override
    public void destroy() {
    }
}