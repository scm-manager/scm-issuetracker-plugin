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

import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
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
public class PullRequestSubscriber {

  private final IssueTracker issueTracker;
  private final PullRequestMapper mapper;

  @Inject
  public PullRequestSubscriber(IssueTracker issueTracker, PullRequestMapper mapper) {
    this.issueTracker = issueTracker;
    this.mapper = mapper;
  }

  @Subscribe
  public void handle(PullRequestEvent event) {
    if (PullRequestEvents.isSupported(event)) {
      IssueReferencingObject ref = mapper.ref(event.getRepository(), event.getItem(), false);
      issueTracker.process(ref);
    }
  }

  @Subscribe
  public void handle(PullRequestMergedEvent event) {
    IssueReferencingObject ref = mapper.ref(event.getRepository(), event.getPullRequest(), true);
    issueTracker.process(ref);
  }
}
