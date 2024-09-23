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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.api.Resubmitter;

import java.io.IOException;
import java.util.*;

/**
 * Default implementation of the {@link IssueTracker}. This will be build using a {@link IssueTrackerBuilder}.
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
  private final ResubmittingCommentator commentator;

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
                      ResubmittingCommentator commentator) {
    this(name, matcher, linkFactory, store, referenceCommentRenderer, commentator, null, null);
  }

  @SuppressWarnings("java:S107") // the large constructor is ok for this use case
  DefaultIssueTracker(String name,
                      IssueMatcher matcher,
                      IssueLinkFactory linkFactory,
                      ProcessedStore store,
                      ReferenceCommentRenderer referenceCommentRenderer,
                      ResubmittingCommentator commentator,
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
  public String getName() {
    return name;
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
    if (stateChanger != null && object.isTriggeringStateChange()) {
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
      if (stateChanger.isStateChangeActivatedFor(object.getType())) {
        changeState(object, issueKey, stateChange.get());
      } else {
        LOG.trace("ignoring state change for type '{}' in repository {}, because state change for this type is disabled", object.getType(), object.getRepository());
        comment(object, issueKey);
      }
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

  @Override
  public Optional<Resubmitter> getResubmitter() {
    return Optional.ofNullable(commentator);
  }
}
