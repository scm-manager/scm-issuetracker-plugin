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

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Collections;
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
      Collections.emptyMap(),
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
