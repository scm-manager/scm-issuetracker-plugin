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
