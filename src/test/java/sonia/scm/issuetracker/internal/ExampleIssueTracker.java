package sonia.scm.issuetracker.internal;

import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.util.Optional;

public class ExampleIssueTracker extends IssueTracker {


  private IssueMatcher issueMatcher;
  private IssueLinkFactory issueLinkFactory;

  public ExampleIssueTracker() {
    super("example");
  }

  public ExampleIssueTracker(String name, IssueMatcher issueMatcher, IssueLinkFactory issueLinkFactory) {
    super(name);
    this.issueMatcher = issueMatcher;
    this.issueLinkFactory = issueLinkFactory;
  }

  public static ExampleIssueTracker getJira() {

    return new ExampleIssueTracker("Jira", ExampleIssueMatcher.createJira(), ExampleIssueLinkFactory.createJira());
  }

  public static ExampleIssueTracker getRedmine() {
    return new ExampleIssueTracker("Redmine", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine());

  }

  @Override
  public Optional<IssueMatcher> createMatcher(Repository repository) {
    return Optional.of(issueMatcher);
  }

  @Override
  public Optional<IssueLinkFactory> createLinkFactory(Repository repository) {
    return Optional.of(issueLinkFactory);
  }

  @Override
  public void markAsHandled(Repository repository, Changeset changeset) {

  }

  @Override
  public void removeHandledMarks(Repository repository) {

  }

  @Override
  public boolean isHandled(Repository repository, Changeset changeset) {
    return false;
  }
}
