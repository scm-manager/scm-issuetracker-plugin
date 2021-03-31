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

package sonia.scm.issuetracker.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;

import java.io.IOException;
import java.util.*;

/**
 * Default implementation of the {@link IssueTracker}.
 *
 * @since 3.0.0
 */
class DefaultIssueTracker implements IssueTracker {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultIssueTracker.class);

  // must
  private final String name;
  private final IssueMatcher matcher;
  private final IssueLinkFactory linkFactory;

  // may - if write
  private final ProcessedStore store;
  private final ReferenceCommentRenderer referenceCommentRenderer;
  private final Commentator commentator;

  // may
  private final StateChangeCommentRenderer stateChangeCommentRenderer;
  private final StateChanger stateChanger;

  DefaultIssueTracker(String name, IssueMatcher matcher, IssueLinkFactory linkFactory) {
    this(name, matcher, linkFactory, null, null, null);
  }

  DefaultIssueTracker(String name,
                      IssueMatcher matcher,
                      IssueLinkFactory linkFactory,
                      ProcessedStore store,
                      ReferenceCommentRenderer referenceCommentRenderer,
                      Commentator commentator) {
    this(name, matcher, linkFactory, store, referenceCommentRenderer, commentator, null, null);
  }

  @SuppressWarnings("java:S107") // the large constructor is ok for this use case
  DefaultIssueTracker(String name,
                      IssueMatcher matcher,
                      IssueLinkFactory linkFactory,
                      ProcessedStore store,
                      ReferenceCommentRenderer referenceCommentRenderer,
                      Commentator commentator,
                      StateChangeCommentRenderer stateChangeCommentRenderer,
                      StateChanger stateChanger) {
    this.name = name;
    this.matcher = matcher;
    this.linkFactory = linkFactory;
    this.store = store;
    this.referenceCommentRenderer = referenceCommentRenderer;
    this.commentator = commentator;
    this.stateChangeCommentRenderer = stateChangeCommentRenderer;
    this.stateChanger = stateChanger;
  }

  @Override
  public void process(IssueReferencingObject object) {
    Set<String> issueKeys = Issues.find(matcher, object);
    if (!issueKeys.isEmpty()) {
      process(object, issueKeys);
    } else {
      LOG.debug("object {} of type {} does not reference any issues of issue tracker {}",
        object.getId(), object.getType(), name
      );
    }
  }

  @Override
  public Map<String, String> findIssues(IssueReferencingObject object) {
    Map<String,String> issues = new HashMap<>();
    for (String issueKey : Issues.find(matcher, object)) {
      issues.put(issueKey, linkFactory.createLink(issueKey));
    }
    return issues;
  }

  private void process(IssueReferencingObject object, Set<String> issueKeys) {
    for (String issueKey : issueKeys) {
      process(object, issueKey);
    }
  }

  private void process(IssueReferencingObject object, String issueKey) {
    if (stateChanger != null) {
      processWithStateChange(object, issueKey);
    } else if (commentator != null) {
      comment(object, issueKey);
    }
  }

  private void comment(IssueReferencingObject object, String issueKey) {
    if (store.isProcessed(issueKey, object)) {
      LOG.debug("{} is already commented", issueKey);
      return;
    }
    try {
      String comment = referenceCommentRenderer.render(object);
      commentator.comment(issueKey, comment);
      store.mark(issueKey, object);
    } catch (IOException ex) {
      LOG.warn("failed to create comment on issue {}", issueKey, ex);
    }
  }

  private void processWithStateChange(IssueReferencingObject object, String issueKey) {
    Iterable<String> keyWords = getKeyWords(issueKey);
    Optional<String> stateChange = Issues.detectStateChange(issueKey, keyWords, object);
    if (stateChange.isPresent()) {
      changeState(object, issueKey, stateChange.get());
    } else {
      comment(object, issueKey);
    }
  }

  private void changeState(IssueReferencingObject object, String issueKey, String keyWord) {
    if (store.isProcessed(issueKey, object, keyWord)) {
      LOG.debug("{} with key word {} was already processed", issueKey, keyWord);
      return;
    }
    try {
      String comment = stateChangeCommentRenderer.render(object, keyWord);
      stateChanger.changeState(issueKey, keyWord);
      commentator.comment(issueKey, comment);
      store.mark(issueKey, object, keyWord);
    } catch (IOException ex) {
      LOG.warn("failed to change state of {} to {}", issueKey, keyWord, ex);
    }
  }

  private Iterable<String> getKeyWords(String issueKey) {
    try {
      return stateChanger.getKeyWords(issueKey);
    } catch (IOException ex) {
      LOG.warn("failed to get key words for {}", issueKey, ex);
      return Collections.emptySet();
    }
  }

}
