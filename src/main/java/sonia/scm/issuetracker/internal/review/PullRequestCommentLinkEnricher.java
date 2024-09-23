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

import com.cloudogu.scm.review.comment.service.Comment;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;

import jakarta.inject.Inject;

@Extension
@Enrich(Comment.class)
@Requires("scm-review-plugin")
public class PullRequestCommentLinkEnricher extends PullRequestBasicCommentEnricher<Comment> {

  @Inject
  public PullRequestCommentLinkEnricher(IssueTracker issueTracker, PullRequestCommentMapper mapper) {
    super(Comment.class, issueTracker, mapper);
  }

}
