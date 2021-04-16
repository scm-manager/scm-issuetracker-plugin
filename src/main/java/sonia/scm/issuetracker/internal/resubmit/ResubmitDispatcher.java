/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.issuetracker.internal.resubmit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ResubmitDispatcher {

  private final ResubmitProcessorFactory processorFactory;
  private final ResubmitQueue queue;

  private boolean inProgress = false;

  @Inject
  public ResubmitDispatcher(ResubmitProcessorFactory processorFactory, ResubmitQueue queue) {
    this.processorFactory = processorFactory;
    this.queue = queue;
  }

  synchronized void resubmit(String issueTrackerName) {
    inProgress = true;
    try {
      ResubmitProcessor processor = processorFactory.create();
      processor.resubmit(issueTrackerName);
      queue.sync(issueTrackerName, processor.getRemove(), processor.getRequeue());
    } finally {
      inProgress = false;
    }
  }

  public boolean isInProgress() {
    return inProgress;
  }
}
