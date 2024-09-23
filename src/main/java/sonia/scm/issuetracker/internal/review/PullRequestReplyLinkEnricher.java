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

import com.cloudogu.scm.review.comment.service.Reply;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;

import jakarta.inject.Inject;

@Extension
@Enrich(Reply.class)
@Requires("scm-review-plugin")
public class PullRequestReplyLinkEnricher extends PullRequestBasicCommentEnricher<Reply> {
  @Inject
  public PullRequestReplyLinkEnricher(IssueTracker issueTracker, PullRequestCommentMapper mapper) {
    super(Reply.class, issueTracker, mapper);
  }
}
