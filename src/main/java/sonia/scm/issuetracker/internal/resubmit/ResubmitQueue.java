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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import lombok.NoArgsConstructor;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class ResubmitQueue {

  private static final String STORE_NAME = "issue-tracker-resubmit-queue";
  private static final int DEFAULT_QUEUE_SIZE = 1000;

  private final DataStore<StoreEntry> store;
  private final int queueSize;

  @Inject
  public ResubmitQueue(DataStoreFactory dataStoreFactory) {
    this(dataStoreFactory, DEFAULT_QUEUE_SIZE);
  }

  @VisibleForTesting
  ResubmitQueue(DataStoreFactory dataStoreFactory, int queueSize) {
    this.store = dataStoreFactory.withType(StoreEntry.class)
      .withName(STORE_NAME)
      .build();
    this.queueSize = queueSize;
  }

  public synchronized void append(QueuedComment comment) {
    StoreEntry entry = entry(comment.getIssueTracker());
    entry.getComments().add(comment);
    store.put(comment.getIssueTracker(), entry);
  }

  public Multimap<String, QueuedComment> getComments() {
    Multimap<String, QueuedComment> comments = HashMultimap.create();
    for (Map.Entry<String, StoreEntry> entry : store.getAll().entrySet()) {
      comments.putAll(entry.getKey(), ImmutableList.copyOf(entry.getValue().getComments()));
    }
    return comments;
  }

  public List<QueuedComment> getComments(String issueTracker) {
    return ImmutableList.copyOf(entry(issueTracker).getComments());
  }

  public synchronized void clear(String issueTracker) {
    StoreEntry entry = entry(issueTracker);
    entry.getComments().clear();
    store.put(issueTracker, entry);
  }

  public synchronized void sync(String issueTracker, Set<QueuedComment> remove, Set<QueuedComment> requeue) {
    StoreEntry entry = entry(issueTracker);
    entry.getComments().removeAll(remove);
    for (QueuedComment comment : entry.getComments()) {
      if (requeue.contains(comment)) {
        comment.retried();
      }
    }
    store.put(issueTracker, entry);
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
