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

package sonia.scm.issuetracker.internal;

import sonia.scm.issuetracker.*;
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
