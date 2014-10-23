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
package soccf;

public class Gateway {
  private static Collector sCollector = new DefaultCollector();

  public static synchronized Collector getCollector() {
    return sCollector;
  }

  public static synchronized void setCollector(Collector collector) {
    sCollector = collector;
  }

  public static synchronized void stmt(int id) {
    if (sCollector != null) {
      sCollector.stmt(id);
    }
  }
  
  public static synchronized boolean branch(int id, boolean value) {
    if (sCollector != null) {
      sCollector.branch(id, value);
    }
    return value;
  }
}
