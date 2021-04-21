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
