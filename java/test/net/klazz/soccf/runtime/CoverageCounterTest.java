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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import net.klazz.soccf.runtime.CoverageCounter;

import org.junit.Test;

public class CoverageCounterTest {
  private static final String testText = "soccf_coverage 0.1\n" +
      "statements\n" +
      "2\n" +
      "1 2\n" + 
      "3 1\n" + 
      "true_branches\n" +
      "2\n" +
      "2 1\n" +
      "4 1\n" +
      "false_branches\n" +
      "2\n" +
      "4 2\n" +
      "9 1\n";

  @Test
  public void testStmt() {
    CoverageCounter counter = new CoverageCounter();
    assertNull(counter.getStatements().get(2));
    counter.stmt(2);
    assertEquals(1, counter.getStatements().get(2).longValue());
    counter.stmt(2);
    assertEquals(2, counter.getStatements().get(2).longValue());
    assertNull(counter.getStatements().get(0));
  }

  @Test
  public void testBranchTrue() {
    CoverageCounter counter = new CoverageCounter();
    assertNull(counter.getBranches().get(2));
    counter.branch(2, true);
    counter.branch(2, true);
    assertNull(counter.getBranches().get(2));
    assertEquals(2, counter.getTrueBranches().get(2).longValue());
    counter.branch(2, false);
    assertEquals(1, counter.getBranches().get(2).longValue());
  }

  @Test
  public void testBranchFalse() {
    CoverageCounter counter = new CoverageCounter();
    assertNull(counter.getBranches().get(2));
    counter.branch(2, false);
    counter.branch(2, false);
    assertNull(counter.getBranches().get(2));
    assertEquals(2, counter.getFalseBranches().get(2).longValue());
    counter.branch(2, true);
    assertEquals(1, counter.getBranches().get(2).longValue());
    counter.branch(2, true);
    counter.branch(2, true);
    assertEquals(2, counter.getBranches().get(2).longValue());
  }

  @Test
  public void testRead() throws IOException {
    CoverageCounter counter = new CoverageCounter();
    StringReader reader = new StringReader(testText);
    counter.read(reader);
    // statement
    assertNull(counter.getStatements().get(0));
    assertEquals(2, counter.getStatements().get(1).longValue());
    assertEquals(1, counter.getStatements().get(3).longValue());
    // true_branch
    assertEquals(1, counter.getTrueBranches().get(2).longValue());
    assertNull(counter.getFalseBranches().get(2));
    assertEquals(1, counter.getTrueBranches().get(4).longValue());
    // false_branch
    assertEquals(1, counter.getTrueBranches().get(4).longValue());
    assertEquals(1, counter.getFalseBranches().get(9).longValue());
    assertNull(counter.getTrueBranches().get(9));
    // branch
    assertNull(counter.getBranches().get(0));
    assertEquals(1, counter.getBranches().get(4).longValue());
  }

  @Test
  public void testWrite() throws IOException {
    CoverageCounter counter = new CoverageCounter();
    StringWriter writer = new StringWriter();
    counter.stmt(1);
    counter.stmt(1);
    counter.stmt(3);
    counter.branch(2, true);
    counter.branch(4, true);
    counter.branch(4, false);
    counter.branch(4, false);
    counter.branch(9, false);
    counter.write(writer);
    assertEquals(testText, writer.toString());
  }
}
