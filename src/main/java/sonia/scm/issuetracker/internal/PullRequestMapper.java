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
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

@Requires("scm-review-plugin")
public class PullRequestMapper {

  static final String TYPE = "pull-request";

  private final ScmConfiguration configuration;
  private final UserDisplayManager userDisplayManager;

  @Inject
  public PullRequestMapper(ScmConfiguration configuration, UserDisplayManager userDisplayManager) {
    this.configuration = configuration;
    this.userDisplayManager = userDisplayManager;
  }

  public IssueReferencingObject ref(Repository repository, PullRequest pr) {
    return new IssueReferencingObject(
      repository,
      TYPE,
      pr.getId(),
      author(pr),
      date(pr),
      content(pr),
      link(repository, pr),
      pr
    );
  }

  private Person author(PullRequest pr) {
    return userDisplayManager.get(pr.getAuthor())
      .map(displayUser -> new Person(displayUser.getDisplayName(), displayUser.getMail()))
      .orElse(Person.toPerson(pr.getAuthor()));
  }

  private Instant date(PullRequest pr) {
    return MoreObjects.firstNonNull(pr.getLastModified(), pr.getCreationDate());
  }

  private List<Content> content(PullRequest pr) {
    return ImmutableList.of(
      new Content("title", pr.getTitle()),
      new Content("description", pr.getDescription())
    );
  }

  @SuppressWarnings("java:S1192")
  private String link(Repository repository, PullRequest pr) {
    return HttpUtil.concatenate(
      configuration.getBaseUrl(), "repo",
      repository.getNamespace(), repository.getName(),
      "pull-request", pr.getId()
    );
  }

}
