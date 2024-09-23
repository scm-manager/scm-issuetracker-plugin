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

import sonia.scm.issuetracker.internal.IssueTrackerFactory;
import sonia.scm.repository.RepositoryManager;

import jakarta.inject.Inject;

public class ResubmitProcessorFactory {

  private final RepositoryManager repositoryManager;
  private final IssueTrackerFactory issueTrackerFactory;
  private final ResubmitQueue queue;

  @Inject
  public ResubmitProcessorFactory(RepositoryManager repositoryManager, IssueTrackerFactory issueTrackerFactory, ResubmitQueue queue) {
    this.repositoryManager = repositoryManager;
    this.queue = queue;
    this.issueTrackerFactory = issueTrackerFactory;
  }

  public ResubmitProcessor create() {
    return new ResubmitProcessor(repositoryManager, issueTrackerFactory, queue);
  }
}
