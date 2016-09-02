package org.anhonesteffort.btc;

import java.util.concurrent.CompletableFuture;

public interface Service {

  public CompletableFuture<Void> shutdownFuture();

  public void start() throws Exception;

  public boolean shutdown();

}
