package sonia.scm.issuetracker;

public interface PullRequestCommentHandler extends AutoCloseable {
  void comment(String issueId);
}
