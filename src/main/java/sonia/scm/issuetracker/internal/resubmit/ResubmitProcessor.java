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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.api.Resubmitter;
import sonia.scm.issuetracker.internal.IssueTrackerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

class ResubmitProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ResubmitProcessor.class);

  private final List<QueuedComment> remove = new ArrayList<>();
  private final List<QueuedComment> requeue = new ArrayList<>();

  private final RepositoryManager repositoryManager;
  private final IssueTrackerFactory issueTrackerFactory;
  private final ResubmitQueue queue;

  ResubmitProcessor(RepositoryManager repositoryManager, IssueTrackerFactory issueTrackerFactory, ResubmitQueue queue) {
    this.repositoryManager = repositoryManager;
    this.issueTrackerFactory = issueTrackerFactory;
    this.queue = queue;
  }

  void resubmit(String issueTrackerName) {
    Multimap<IssueTrackerId, QueuedComment> comments = comments(issueTrackerName);
    LOG.info("resubmit {} comments", comments.size());
    for (IssueTrackerId issueTrackerId : comments.keySet()) {
      process(issueTrackerId, comments.get(issueTrackerId));
    }
  }

  public Collection<QueuedComment> getRemove() {
    return remove;
  }

  public Collection<QueuedComment> getRequeue() {
    return requeue;
  }

  private void process(IssueTrackerId id, Collection<QueuedComment> queuedComments) {
    Repository repository = repositoryManager.get(id.getRepository());
    if (repository != null) {
      process(repository, id.getIssueTracker(), queuedComments);
    } else {
      LOG.warn("repository with id {} does not exists, remove {} comments from queue", id.getRepository(), queuedComments.size());
      remove.addAll(queuedComments);
    }
  }

  private void process(Repository repository, String issueTracker, Collection<QueuedComment> queuedComments) {
    Optional<IssueTracker> tracker = issueTrackerFactory.tracker(repository, issueTracker);
    if (tracker.isPresent()) {
      process(tracker.get(), queuedComments);
    } else {
      LOG.warn("could not find tracker {} for repository {}, requeue {} comments", issueTracker, repository.getNamespaceAndName(), queuedComments.size());
      requeue.addAll(queuedComments);
    }
  }

  private void process(IssueTracker tracker, Collection<QueuedComment> queuedComments) {
    Optional<Resubmitter> resubmitter = tracker.getResubmitter();
    if (resubmitter.isPresent()) {
      process(resubmitter.get(), queuedComments);
    } else {
      LOG.warn("tracker {} does not support resubmit, requeue {} comments", tracker.getName(), queuedComments.size());
      requeue.addAll(queuedComments);
    }
  }

  private void process(Resubmitter resubmitter, Collection<QueuedComment> queuedComments) {
    for (QueuedComment queuedComment : queuedComments) {
      resubmit(resubmitter, queuedComment);
    }
  }

  private void resubmit(Resubmitter resubmitter, QueuedComment queuedComment) {
    try {
      resubmitter.resubmit(queuedComment.getIssueKey(), queuedComment.getComment());
      LOG.debug("successfully resubmit queued comment for tracker {} and issue key {}", queuedComment.getIssueTracker(), queuedComment.getIssueKey());
      remove.add(queuedComment);
    } catch (IOException e) {
      LOG.warn("failed to resubmit queued comment for tracker {} and issue key {}", queuedComment.getIssueTracker(), queuedComment.getIssueKey(), e);
      requeue.add(queuedComment);
    }
  }

  private Multimap<IssueTrackerId, QueuedComment> comments(String issueTrackerName) {
    Multimap<IssueTrackerId, QueuedComment> comments = HashMultimap.create();
    for (QueuedComment comment : queue.getComments(issueTrackerName)) {
      comments.put(new IssueTrackerId(comment.getRepository(), comment.getIssueTracker()), comment);
    }
    return comments;
  }

  @Value
  private static class IssueTrackerId {
    String repository;
    String issueTracker;
  }
}
