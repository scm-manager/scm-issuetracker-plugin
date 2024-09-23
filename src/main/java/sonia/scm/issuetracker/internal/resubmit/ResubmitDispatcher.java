/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.issuetracker.internal.resubmit;

import sonia.scm.issuetracker.internal.Permissions;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class ResubmitDispatcher implements Closeable {

  private final ResubmitProcessorFactory processorFactory;
  private final ResubmitQueue queue;
  private final ExecutorService executorService;

  private boolean inProgress = false;

  @Inject
  public ResubmitDispatcher(ResubmitProcessorFactory processorFactory, ResubmitQueue queue) {
    this.processorFactory = processorFactory;
    this.queue = queue;
    this.executorService = Executors.newSingleThreadExecutor();
  }

  synchronized void resubmitAsync(String issueTrackerName) {
    Permissions.checkResubmit(issueTrackerName);
    executorService.execute(() -> resubmit(issueTrackerName));
  }

  synchronized void resubmit(String issueTrackerName) {
    Permissions.checkResubmit(issueTrackerName);
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

  @Override
  public void close() {
    executorService.shutdown();
  }
}
