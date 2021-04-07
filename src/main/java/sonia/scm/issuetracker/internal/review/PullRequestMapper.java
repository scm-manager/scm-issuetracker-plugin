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

package sonia.scm.issuetracker.internal.review;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.internal.PersonMapper;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

@Requires("scm-review-plugin")
public class PullRequestMapper {

  static final String TYPE = "pull-request";

  private final ScmConfiguration configuration;
  private final PersonMapper personMapper;

  @Inject
  public PullRequestMapper(ScmConfiguration configuration, PersonMapper personMapper) {
    this.configuration = configuration;
    this.personMapper = personMapper;
  }

  public IssueReferencingObject ref(Repository repository, PullRequest pr, boolean triggeringStateChange) {
    return new IssueReferencingObject(
      repository,
      TYPE,
      pr.getId(),
      personMapper.person(pr.getAuthor()),
      date(pr),
      content(pr),
      link(repository, pr),
      triggeringStateChange,
      pr
    );
  }

  private Instant date(PullRequest pr) {
    return MoreObjects.firstNonNull(pr.getLastModified(), pr.getCreationDate());
  }

  private List<Content> content(PullRequest pr) {
    ImmutableList.Builder<Content> builder = ImmutableList.builder();
    if (!Strings.isNullOrEmpty(pr.getTitle())) {
      builder.add(new Content("title", pr.getTitle()));
    }
    if (!Strings.isNullOrEmpty(pr.getDescription())) {
      builder.add(new Content("description", pr.getDescription()));
    }
    return builder.build();
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
