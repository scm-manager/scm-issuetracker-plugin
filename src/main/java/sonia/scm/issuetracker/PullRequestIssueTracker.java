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
package sonia.scm.issuetracker;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@ExtensionPoint
public class PullRequestIssueTracker {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequestIssueTracker.class);

  private final PullRequestCommentHandlerProvider commentHandlerProvider;
  private final MatcherProvider matcherProvider;
  private final IssueHandledTracker issueHandledTracker;
  private final ChangeStateHandler changeStateHandler;

  @Inject
  protected PullRequestIssueTracker(PullRequestCommentHandlerProvider commentHandlerProvider, MatcherProvider matcherProvider, ChangeStateHandler changeStateHandler, String name, DataStoreFactory dataStoreFactory) {
    this(commentHandlerProvider, matcherProvider, changeStateHandler, new DataStoreBasedIssueHandledTracker(name, dataStoreFactory));
  }

  protected PullRequestIssueTracker(PullRequestCommentHandlerProvider commentHandlerProvider, MatcherProvider matcherProvider, ChangeStateHandler changeStateHandler, IssueHandledTracker issueHandledTracker) {
    this.commentHandlerProvider = commentHandlerProvider;
    this.matcherProvider = matcherProvider;
    this.issueHandledTracker = issueHandledTracker;
    this.changeStateHandler = changeStateHandler;
  }

  public void handlePullRequestRequest(PullRequestIssueRequestData data) {
    try (PullRequestCommentHandler commentHandler = commentHandlerProvider.getCommentHandler(data)) {
      if (commentHandler != null) {
        data.getIssueIds().forEach(commentHandler::mentionedInTitleOrDescription);
        markAsHandled(data);
      }
    } catch (Exception e) {
      LOG.error("Error commenting issues for pull request", e);
    }
  }

  public void handleMergePullRequestMergeRequest(PullRequestMergeRequestData data) {
    if (changeStateHandler != null) {
      for (Map.Entry<String, String> stateTransition : data.stateTransitions.entrySet()) {
        changeStateHandler.changeState(stateTransition.getKey(), stateTransition.getValue());
      }
    }
  }

  public void handlePullRequestCommentRequest(PullRequestCommentIssueRequestData data) {
    try (PullRequestCommentHandler commentHandler = commentHandlerProvider.getCommentHandler(data)) {
      if (commentHandler != null) {
        data.getIssueIds().forEach(commentHandler::mentionedInComment);
        markAsHandled(data);
      }
    } catch (Exception e) {
      LOG.error("Error commenting issues for pull request comment", e);
    }
  }

  private void markAsHandled(PullRequestIssueRequestData data) {
    issueHandledTracker.markAsHandled(data.getRepository(), data);
  }

  public Optional<IssueMatcher> createMatcher(Repository repository) {
    return matcherProvider.createMatcher(repository);
  }

  public Iterable<String> getIssueStateKeywords() {
    return changeStateHandler.getKeywords();
  }

  @Value
  public static class PullRequestMergeRequestData {
    Repository repository;
    PullRequest pullRequest;
    Map<String, String> stateTransitions;
  }
}
