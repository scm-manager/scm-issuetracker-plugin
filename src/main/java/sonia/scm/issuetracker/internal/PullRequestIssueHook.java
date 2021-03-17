package sonia.scm.issuetracker.internal;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.github.legman.Subscribe;
import com.google.common.base.Strings;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.PullRequestIssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
@EagerSingleton
public class PullRequestIssueHook {

  private final Set<PullRequestIssueTracker> issueTracker;

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
      tracker.handlePullRequestRequest(new PullRequestIssueTracker.PullRequestIssueRequestData(eventType, pullRequest.getId(), issueKeys));
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
