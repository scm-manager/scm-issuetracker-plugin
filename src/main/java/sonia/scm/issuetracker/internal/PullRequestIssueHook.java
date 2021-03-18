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

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.github.legman.Subscribe;
import com.google.common.base.Strings;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.PullRequestIssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class PullRequestIssueHook {

  private final Set<PullRequestIssueTracker> issueTracker;

  @Inject
  public PullRequestIssueHook(Set<PullRequestIssueTracker> issueTracker) {
    this.issueTracker = issueTracker;
  }

  @Subscribe
  public void handle(PullRequestEvent event) {
    switch (event.getEventType()) {
      case MODIFY:
        issueTracker.forEach(tracker -> handleEvent("modified", tracker, event.getRepository(), event.getPullRequest()));
        break;
      case CREATE:
        issueTracker.forEach(tracker -> handleEvent("created", tracker, event.getRepository(), event.getPullRequest()));
        break;
      default:
        // nothing to do
    }
  }

  private void handleEvent(String eventType, PullRequestIssueTracker tracker, Repository repository, PullRequest pullRequest) {
    Optional<IssueMatcher> matcher = tracker.createMatcher(repository);
    if (matcher.isPresent()) {
      Collection<String> titleIssueKeys = extractIssueKeys(matcher.get(), matcher.get().getKeyPattern(), pullRequest.getTitle());
      Collection<String> descriptionIssueKeys = extractIssueKeys(matcher.get(), matcher.get().getKeyPattern(), pullRequest.getDescription());
      Collection<String> issueKeys = new HashSet<>();
      issueKeys.addAll(titleIssueKeys);
      issueKeys.addAll(descriptionIssueKeys);
      tracker.handlePullRequestRequest(new PullRequestIssueTracker.PullRequestIssueRequestData(eventType, repository, pullRequest.getId(), issueKeys));
    }
  }

  private Collection<String> extractIssueKeys(IssueMatcher matcher, Pattern p, String text) {
    Collection<String> keys = new HashSet<>();

    if (!Strings.isNullOrEmpty(text)) {
      Matcher m = p.matcher(text);

      while (m.find()) {
        keys.add(matcher.getKey(m));
      }
    }

    return keys;
  }
}
