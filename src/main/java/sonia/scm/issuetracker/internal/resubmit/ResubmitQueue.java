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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import lombok.NoArgsConstructor;
import sonia.scm.issuetracker.internal.Permissions;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
public class ResubmitQueue {

  private static final String STORE_NAME = "issue-tracker-resubmit-queue";
  private static final int DEFAULT_QUEUE_SIZE = 1000;

  private final DataStore<StoreEntry> store;
  private final NotificationService notificationService;
  private final int queueSize;

  @Inject
  public ResubmitQueue(DataStoreFactory dataStoreFactory, NotificationService notificationService) {
    this(dataStoreFactory, notificationService, DEFAULT_QUEUE_SIZE);
  }

  @VisibleForTesting
  ResubmitQueue(DataStoreFactory dataStoreFactory, NotificationService notificationService, int queueSize) {
    this.store = dataStoreFactory.withType(StoreEntry.class)
      .withName(STORE_NAME)
      .build();
    this.notificationService = notificationService;
    this.queueSize = queueSize;
  }

  public synchronized void append(QueuedComment comment) {
    StoreEntry entry = entry(comment.getIssueTracker());
    entry.getComments().add(comment);
    store.put(comment.getIssueTracker(), entry);
    notificationService.notifyComment(comment);
  }

  public Multimap<String, QueuedComment> getComments() {
    Permissions.Checker checker = Permissions.resubmitChecker();
    Multimap<String, QueuedComment> comments = HashMultimap.create();
    for (Map.Entry<String, StoreEntry> entry : store.getAll().entrySet()) {
      String issueTrackerName = entry.getKey();
      if (checker.isPermitted(issueTrackerName)) {
        comments.putAll(issueTrackerName, ImmutableList.copyOf(entry.getValue().getComments()));
      }
    }
    return comments;
  }

  public List<QueuedComment> getComments(String issueTracker) {
    Permissions.checkResubmit(issueTracker);
    return ImmutableList.copyOf(entry(issueTracker).getComments());
  }

  public synchronized void clear(String issueTracker) {
    Permissions.checkResubmit(issueTracker);
    StoreEntry entry = entry(issueTracker);
    entry.getComments().clear();
    store.put(issueTracker, entry);
  }

  public synchronized void sync(String issueTracker, Collection<QueuedComment> remove, Collection<QueuedComment> requeue) {
    Permissions.checkResubmit(issueTracker);
    StoreEntry entry = entry(issueTracker);
    entry.getComments().removeAll(remove);
    for (QueuedComment comment : entry.getComments()) {
      if (requeue.contains(comment)) {
        comment.retried();
      }
    }
    store.put(issueTracker, entry);
    notificationService.notifyResubmit(issueTracker, remove, requeue);
  }

  private StoreEntry entry(String issueTracker) {
    return store.getOptional(issueTracker).orElseGet(() -> new StoreEntry(queueSize));
  }

  @NoArgsConstructor
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class StoreEntry {

    @XmlElement(name = "store-size")
    private int storeSize;

    @XmlElement(name = "comments")
    private EvictingQueue<QueuedComment> comments;

    public StoreEntry(int storeSize) {
      this.storeSize = storeSize;
    }

    public EvictingQueue<QueuedComment> getComments() {
      if (comments == null){
        comments = EvictingQueue.create(storeSize);
      }
      return comments;
    }
  }
}
