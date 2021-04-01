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

import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;

import javax.inject.Inject;

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
