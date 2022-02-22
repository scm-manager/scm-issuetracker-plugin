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

import com.cloudogu.scm.review.comment.service.BasicComment;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.internal.PersonMapper;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Requires("scm-review-plugin")
public class PullRequestCommentMapper {

  public static final String TYPE = "comment";

  private final ScmConfiguration configuration;
  private final PersonMapper personMapper;

  @Inject
  public PullRequestCommentMapper(ScmConfiguration configuration, PersonMapper personMapper) {
    this.configuration = configuration;
    this.personMapper = personMapper;
  }

  public IssueReferencingObject ref(Repository repository, BasicComment comment) {
    return ref(repository, null, comment);
  }

  public IssueReferencingObject ref(Repository repository, PullRequest pullRequest, BasicComment comment) {
    return new IssueReferencingObject(
      repository,
      TYPE,
      comment.getId(),
      personMapper.person(comment.getAuthor()),
      comment.getDate(),
      content(comment),
      link(repository, pullRequest, comment),
      false,
      comment
    );
  }

  @SuppressWarnings("java:S1192") // same content different meaning
  private List<Content> content(BasicComment comment) {
    if (Strings.isNullOrEmpty(comment.getComment())){
      return Collections.emptyList();
    }
    return ImmutableList.of(
      new Content("comment", comment.getComment())
    );
  }

  private String link(Repository repository, @Nullable PullRequest pr, BasicComment comment) {
    String repoUrl = HttpUtil.concatenate(
      configuration.getBaseUrl(), "repo", repository.getNamespace(), repository.getName()
    );
    if (pr == null) {
      return HttpUtil.concatenate(repoUrl, "pull-requests");
    }
    return HttpUtil.concatenate(repoUrl, "pull-request", pr.getId(), "comments#comment-" + comment.getId());
  }

}
