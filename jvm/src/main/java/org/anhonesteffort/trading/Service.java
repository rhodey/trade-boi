package org.anhonesteffort.trading;

import java.util.concurrent.CompletableFuture;

public interface Service {

  CompletableFuture<Void> shutdownFuture();

  void start() throws Exception;

  boolean shutdown();

}
