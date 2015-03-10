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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CoverageCounter implements Collector {
  private final Map<Integer, Long> mStatements;
  private final Map<Integer, Long> mBranches;
  private final Map<Integer, Long> mTrueBranches;
  private final Map<Integer, Long> mFalseBranches;

  public CoverageCounter() {
    mStatements = new HashMap<>();
    mBranches = new HashMap<>();
    mTrueBranches = new HashMap<>();
    mFalseBranches = new HashMap<>();
  }

  public Map<Integer, Long> getStatements() {
    return Collections.unmodifiableMap(mStatements);
  }

  public Map<Integer, Long> getBranches() {
    return Collections.unmodifiableMap(mBranches);
  }

  public Map<Integer, Long> getTrueBranches() {
    return Collections.unmodifiableMap(mTrueBranches);
  }

  public Map<Integer, Long> getFalseBranches() {
    return Collections.unmodifiableMap(mFalseBranches);
  }

  @Override
  public synchronized void stmt(int id) {
    increment(mStatements, id);
  }

  @Override
  public synchronized void branch(int id, boolean value) {
    increment(value ? mTrueBranches : mFalseBranches, id);
    Long trueCount = mTrueBranches.get(id);
    Long falseCount = mFalseBranches.get(id);
    if (trueCount != null && falseCount != null) {
      mBranches.put(id, Math.min(trueCount, falseCount));
    }
  }

  private void increment(Map<Integer, Long> idToCount, int id) {
    Long prev = idToCount.get(id);
    idToCount.put(id, prev == null ? 1 : prev + 1);
  }

  public void readFile(String filename, boolean gzip) {
    InputStream in = null;
    Reader reader = null;
    try {
      in = new FileInputStream(filename);
      if (gzip) {
        in = new GZIPInputStream(in);
      }
      reader = new InputStreamReader(in);
      read(reader);
    } catch (FileNotFoundException e) {
      // if file is not found, it's the first run
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      doClose(reader);
      doClose(in);
    }
  }

  public void read(Reader reader) throws IOException {
    BufferedReader buffered = new BufferedReader(reader);
    String sig = buffered.readLine();
    assertSignature("soccf_coverage 0.1", sig);
    // TODO: make exception-safe
    readCountMap(mStatements, "statements", buffered);
    readCountMap(mTrueBranches, "true_branches", buffered);
    readCountMap(mFalseBranches, "false_branches", buffered);
    updateBranches();
  }

  public synchronized void writeFile(String filename, boolean gzip) {
    OutputStream out = null;
    BufferedWriter writer = null;
    try {
      out = new FileOutputStream(filename);
      if (gzip) {
        out = new GZIPOutputStream(out);
      }
      writer = new BufferedWriter(new OutputStreamWriter(out));
      write(writer);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      doClose(writer);
      doClose(out);
    }
  }

  public synchronized void write(Writer writer) throws IOException {
    String sep = System.lineSeparator();
    writer.write("soccf_coverage 0.1");
    writer.write(sep);
    writeCountMap(mStatements, "statements", writer);
    writeCountMap(mTrueBranches, "true_branches", writer);
    writeCountMap(mFalseBranches, "false_branches", writer);
  }

  private void readCountMap(Map<Integer, Long> counts, String signature, BufferedReader reader) throws IOException {
    String sig = reader.readLine();
    assertSignature(signature, sig);
    int num = Integer.valueOf(reader.readLine());
    for (int i = 0; i < num; i++) {
      String[] words = reader.readLine().split(" ");
      Integer id = Integer.valueOf(words[0]);
      Long cnt = Long.valueOf(words[1]);
      counts.put(id, cnt);
    }
  }

  private void writeCountMap(Map<Integer, Long> counts, String signature, Writer writer) throws IOException {
    String sep = System.lineSeparator();
    writer.write(signature);
    writer.write(sep);
    writer.write(String.valueOf(counts.size()));
    writer.write(sep);
    // sort keys
    for (Integer key : new TreeSet<Integer>(counts.keySet())) {
      writer.write(key.toString());
      writer.write(' ');
      writer.write(counts.get(key).toString());
      writer.write(sep);
    }
  }

  private void updateBranches() {
    mBranches.clear();
    for (Integer id : mTrueBranches.keySet()) {
      Long trueCount = mTrueBranches.get(id);
      Long falseCount = mFalseBranches.get(id);
      if (trueCount != null && falseCount != null) {
        mBranches.put(id, Math.min(trueCount, falseCount));
      }
    }
  }

  private void doClose(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
      }
    }
  }

  private void assertSignature(String signature, String actual) throws IOException {
    if (signature.equals(actual) == false) {
      throw new IOException("invalid soccf_coverage format");
    }
  }
}
