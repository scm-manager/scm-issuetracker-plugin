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

package sonia.scm.issuetracker.internal;

import com.cloudogu.scm.review.comment.service.BasicComment;
import com.cloudogu.scm.review.comment.service.BasicCommentEvent;
import com.cloudogu.scm.review.comment.service.Comment;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.PullRequestCommentIssueRequestData;
import sonia.scm.issuetracker.PullRequestIssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class PullRequestCommentIssueHook {

  private final Set<PullRequestIssueTracker> issueTracker;
  private final UserDisplayManager userDisplayManager;

  @Inject
  public PullRequestCommentIssueHook(Set<PullRequestIssueTracker> issueTracker, UserDisplayManager userDisplayManager) {
    this.issueTracker = issueTracker;
    this.userDisplayManager = userDisplayManager;
  }

  @Subscribe
  public void handle(BasicCommentEvent event) {
    switch (event.getEventType()) {
      case MODIFY:
        if (shouldHandleModifyEvent(event)) {
          issueTracker.forEach(tracker -> handleEvent("modified", tracker, event.getRepository(), event.getPullRequest(), event.getItem()));
        }
        break;
      case CREATE:
        issueTracker.forEach(tracker -> handleEvent("created", tracker, event.getRepository(), event.getPullRequest(), event.getItem()));
        break;
      default:
        // nothing to do
    }
  }

  private boolean shouldHandleModifyEvent(BasicCommentEvent event) {
    BasicComment comment = event.getItem();
    // Should not handle event if task type was changed, e.g.: COMMENT -> Task_TODO
    if (comment instanceof Comment) {
      return ((Comment) comment).getType().equals(((Comment) event.getOldItem()).getType());
    }

    return true;
  }

  private void handleEvent(String eventType, PullRequestIssueTracker tracker, Repository repository, PullRequest pullRequest, BasicComment comment) {
    Optional<IssueMatcher> matcher = tracker.createMatcher(repository);
    if (matcher.isPresent()) {
      Collection<String> commentIssueKeys = IssueKeys.extract(matcher.get(), matcher.get().getKeyPattern(), comment.getComment());
      tracker.handlePullRequestCommentRequest(createRequestData(eventType, repository, pullRequest, comment, commentIssueKeys));
    }
  }

  private PullRequestCommentIssueRequestData createRequestData(String eventType, Repository repository, PullRequest pullRequest, BasicComment comment, Collection<String> issueKeys) {
    DisplayUser author = userDisplayManager.get(pullRequest.getAuthor()).orElse(null);
    return new PullRequestCommentIssueRequestData(eventType, repository, pullRequest, author, issueKeys, comment);
  }
}

