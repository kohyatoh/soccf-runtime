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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimedCoverageLogger {
  private final Timer mTimer;
  private final int mPeriod;
  private final List<Reporter> mReporters;
  private final List<Logger> mStatementLoggers;
  private final List<Logger> mBranchLoggers;
  
  public TimedCoverageLogger(int period) {
    mTimer = new Timer();
    mPeriod = period;
    mReporters = new ArrayList<>();
    mStatementLoggers = new ArrayList<>();
    mBranchLoggers = new ArrayList<>();
  }

  public void start() {
    mTimer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        log();
      }
    }, 0, mPeriod);
  }

  public void addReporter(Reporter reporter, String filename) {
    mReporters.add(reporter);
    mStatementLoggers.add(new Logger(filename + ".stmt"));
    mBranchLoggers.add(new Logger(filename + ".br"));
  }

  private void log() {
    for (int i = 0; i < mReporters.size(); i++) {
      Reporter reporter = mReporters.get(i);
      mStatementLoggers.get(i).log(reporter.getCoveredStatements());
      mBranchLoggers.get(i).log(reporter.getCoveredBranches());
    }
  }

  private static class Logger {
    private BufferedWriter mWriter;

    public Logger(String fileName) {
      try {
        mWriter = new BufferedWriter(new FileWriter(fileName, true));
      } catch (IOException e) {
      }
    }

    public void log(int num) {
      if (mWriter == null) return ;
      try {
        mWriter.write(String.valueOf(num));
        mWriter.write(System.lineSeparator());
        mWriter.flush();
      } catch (IOException e) {
      }
    }
  }
}
