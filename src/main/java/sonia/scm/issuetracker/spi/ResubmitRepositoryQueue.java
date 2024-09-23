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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.internal.resubmit.QueuedComment;
import sonia.scm.issuetracker.internal.resubmit.ResubmitQueue;

class ResubmitRepositoryQueue {

  private final ResubmitQueue queue;
  private final String repository;
  private final String issueTracker;

  ResubmitRepositoryQueue(ResubmitQueue queue, String repository, String issueTracker) {
    this.queue = queue;
    this.repository = repository;
    this.issueTracker = issueTracker;
  }

  public void append(String issueKey, String comment) {
    queue.append(new QueuedComment(repository, issueTracker, issueKey, comment));
  }

}
