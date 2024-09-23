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

import com.cloudogu.scm.review.comment.service.BasicCommentEvent;
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;

import jakarta.inject.Inject;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class PullRequestCommentSubscriber {

  private final IssueTracker issueTracker;
  private final PullRequestCommentMapper commentMapper;

  @Inject
  public PullRequestCommentSubscriber(IssueTracker issueTracker, PullRequestCommentMapper commentMapper) {
    this.issueTracker = issueTracker;
    this.commentMapper = commentMapper;
  }

  @Subscribe
  public void handle(BasicCommentEvent<?> event) {
    if (PullRequestEvents.isSupported(event)) {
      IssueReferencingObject ref = commentMapper.ref(event.getRepository(), event.getPullRequest(), event.getItem());
      issueTracker.process(ref);
    }
  }
}
