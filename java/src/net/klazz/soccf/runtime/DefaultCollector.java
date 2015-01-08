/*
 * (C) Copyright 2014 Kohsuke Yatoh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.klazz.soccf.runtime;


public class DefaultCollector implements Collector, Reporter {
  private final CoverageCounter mCounter;

  public DefaultCollector() {
    this("soccf.cov.gz", true);
  }

  public DefaultCollector(String filename, boolean gzip) {
    final String _filename = filename;
    final boolean _gzip = gzip;
    mCounter = new CoverageCounter();
    mCounter.readFile(filename, gzip);
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() { mCounter.writeFile(_filename, _gzip); }
    }));
  }

  @Override
  public synchronized int getCoveredStatements() {
    return mCounter.getStatements().size();
  }

  @Override
  public synchronized int getCoveredBranches() {
    return mCounter.getBranches().size();
  }

  @Override
  public synchronized void stmt(int id) {
    mCounter.stmt(id);
  }

  @Override
  public synchronized void branch(int id, boolean value) {
    mCounter.branch(id,  value);
  }
}
