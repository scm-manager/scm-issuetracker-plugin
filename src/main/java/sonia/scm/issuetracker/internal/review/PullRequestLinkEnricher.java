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
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;

@Extension
@Enrich(PullRequest.class)
@Requires("scm-review-plugin")
public class PullRequestLinkEnricher implements HalEnricher {

  private final IssueTracker issueTracker;
  private final PullRequestMapper mapper;

  @Inject
  public PullRequestLinkEnricher(IssueTracker issueTracker, PullRequestMapper mapper) {
    this.issueTracker = issueTracker;
    this.mapper = mapper;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    PullRequest pullRequest = context.oneRequireByType(PullRequest.class);

    IssueReferencingObject ref = mapper.ref(repository, pullRequest, false);

    HalAppender.LinkArrayBuilder builder = appender.linkArrayBuilder("issues");
    issueTracker.findIssues(ref).forEach(builder::append);
    builder.build();
  }
}
