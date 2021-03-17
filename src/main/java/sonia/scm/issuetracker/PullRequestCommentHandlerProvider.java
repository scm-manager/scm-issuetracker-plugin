package sonia.scm.issuetracker;

public interface PullRequestCommentHandlerProvider {
  PullRequestCommentHandler getCommentHandler(PullRequestIssueTracker.PullRequestIssueRequestData data);
}
