package org.swecourse.services;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import com.owlike.genson.ext.jaxrs.GensonJsonConverter;

/**
 * Main class.
 *
 */
public class Main {

  private static final Logger logger = Logger.getLogger(Main.class);
  //

  // Base URI the Grizzly HTTP server will listen on
  public static final String BASE_URI;
  public static final String LSTN_URI;
  private static final String PROTOCOL;
  private static final Optional < String > host;
  private static final Optional < String > lstn;
  private static final Optional < String > port;

  public static Integer i = new Integer(123);
  
  static {
    PROTOCOL = "http://";
    host = Optional.ofNullable(System.getenv("SERVICES_GJ_HOST"));
    lstn = Optional.ofNullable(System.getenv("SERVICES_GJ_LSTH"));
    port = Optional.ofNullable(System.getenv("SERVICES_GJ_PORT"));
    BASE_URI = PROTOCOL + host.orElse("localhost") + ":" + port.orElse("80") + "/";
    LSTN_URI = PROTOCOL + lstn.orElse("0.0.0.0") + ":" + port.orElse("80");
    String uname = "steve";
    String password = "blue";
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   * @return Grizzly HTTP server.
   */
  public static HttpServer createServer() {
    logger.info("Grizzly server URL " + LSTN_URI);
    // create a resource config that scans for JAX-RS resources and providers
    // in com.secourse package
    final ResourceConfig rc = new ResourceConfig().packages("org.swecourse.services");
    rc.register(GensonJsonConverter.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at LSTN_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(LSTN_URI), rc);
  }

  /**
   * Main method.
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    //
    logger.info("Initiliazing Grizzly server using " + LSTN_URI);
    CountDownLatch exitEvent = new CountDownLatch(1);
    HttpServer server = createServer();
    // register shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Stopping server ...");
      server.stop();
      exitEvent.countDown();
    }, "shutdownHook"));

    try {
      server.start();
      logger.info(String.format("Jersey app started with WADL available at %sapplication.wadl", BASE_URI));
      logger.info("Press CTRL^C to exit ...");
      exitEvent.await();
      logger.info("Exiting service ...");
    } catch (InterruptedException e) {
      logger.error("There was an error while starting Grizzly HTTP server.", e);
      Thread.currentThread().interrupt();
    }
  }
}
