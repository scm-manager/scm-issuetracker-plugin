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
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Repository;

class PullRequestBasicCommentEnricher<T extends BasicComment> implements HalEnricher  {

  private final Class<T> commentType;
  private final IssueTracker issueTracker;
  private final PullRequestCommentMapper mapper;

  protected PullRequestBasicCommentEnricher(Class<T> commentType, IssueTracker issueTracker, PullRequestCommentMapper mapper) {
    this.commentType = commentType;
    this.issueTracker = issueTracker;
    this.mapper = mapper;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    BasicComment comment = context.oneRequireByType(commentType);

    HalAppender.LinkArrayBuilder builder = appender.linkArrayBuilder("issues");
    IssueReferencingObject ref = mapper.ref(repository, comment);
    issueTracker.findIssues(ref).forEach(builder::append);
    builder.build();
  }
}
