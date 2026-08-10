package de.shadow.nachtweber;

interface NachtweberStoreRuntime {
  void maintain(long nowMs);
  void close();
}
