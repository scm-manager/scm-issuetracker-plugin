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
import jakarta.inject.Inject;
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
      Collections.emptyMap(),
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
