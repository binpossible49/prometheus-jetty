package vn.thanhtd;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.xml.ws.WebServiceContext;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import vn.thanhtd.filter.PrometheusFilter;
import vn.thanhtd.handler.HelloHandler;
import vn.thanhtd.servlet.HelloServlet;
/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8081);
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(HelloServlet.class, "/hello");
        servletHandler.addFilterWithMapping(PrometheusFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        CollectorRegistry registry = PrometheusFilter.getRegistry();
        MetricsServlet metricsServlet = new MetricsServlet(registry);
        servletHandler.addServletWithMapping(metricsServlet.getClass(), "/metrics");
        server.setHandler(servletHandler);
        server.start();
        server.join();
    }
}
