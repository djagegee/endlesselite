package de.shadow.endlesselite.core;

public interface ManagedModule {
  String id();
  void start();
  void stop();
}
