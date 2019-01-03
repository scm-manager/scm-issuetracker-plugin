package sonia.scm.issuetracker.internal;

import sonia.scm.issuetracker.IssueLinkFactory;

public class ExampleIssueLinkFactory implements IssueLinkFactory {

  private static final String JIRA_URL = "https://jira.hitchhiker.com/issues/";
  private static final String REDMINE_URL = "https://redmine.hitchhiker.com/issues/";

  private String trackerUrl;

  public ExampleIssueLinkFactory(String trackerUrl) {
    this.trackerUrl = trackerUrl;
  }

  @Override
  public String createLink(String key) {
    if (key.startsWith("#")) {
      return trackerUrl + key.substring(1);
    }
    return trackerUrl + key;
  }

  public static ExampleIssueLinkFactory createJira() {
    return new ExampleIssueLinkFactory(JIRA_URL);
  }

  public static ExampleIssueLinkFactory createRedmine() {
    return new ExampleIssueLinkFactory(REDMINE_URL);
  }
}
