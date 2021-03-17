package sonia.scm.issuetracker;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

import java.util.Collection;
import java.util.Optional;

@ExtensionPoint(multi = true)
public class PullRequestIssueTracker {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequestIssueTracker.class);

  private final PullRequestCommentHandlerProvider commentHandlerProvider;
  private final MatcherProvider matcherProvider;

  protected PullRequestIssueTracker(PullRequestCommentHandlerProvider commentHandlerProvider, MatcherProvider matcherProvider) {
    this.commentHandlerProvider = commentHandlerProvider;
    this.matcherProvider = matcherProvider;
  }

  public void handlePullRequestRequest(PullRequestIssueRequestData data) {
    try (PullRequestCommentHandler commentHandler = commentHandlerProvider.getCommentHandler(data)) {
      if (commentHandler != null) {
        data.getIssueIds().forEach(commentHandler::comment);
      }
    } catch (Exception e) {
      LOG.error("Error commenting issues for pull request", e);
    }
  }

  public Optional<IssueMatcher> createMatcher(Repository repository) {
    return matcherProvider.createMatcher(repository);
  }

  @Value
  public static class PullRequestIssueRequestData {
    String requestType;
    String pullRequestId;
    Collection<String> issueIds;
  }
}
