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

package sonia.scm.issuetracker.internal;

import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import javax.inject.Inject;

@Extension
@EagerSingleton
public class ChangesetSubscriber {

  private static final Logger LOG = LoggerFactory.getLogger(ChangesetSubscriber.class);

  private final IssueTracker issueTracker;
  private final ChangesetMapper mapper;

  @Inject
  public ChangesetSubscriber(IssueTracker issueTracker, ChangesetMapper mapper) {
    this.issueTracker = issueTracker;
    this.mapper = mapper;
  }

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    HookContext context = event.getContext();
    if (isSupported(context)) {
      context.getChangesetProvider()
        .setDisablePreProcessors(true)
        .getChangesets()
        .forEach(changeset ->  process(event.getRepository(), changeset));
    } else {
      LOG.debug("hook does not support changeset provider");
    }
  }

  private void process(Repository repository, Changeset changeset) {
    IssueReferencingObject ref = mapper.ref(repository, changeset);
    issueTracker.process(ref);
  }

  private boolean isSupported(HookContext context) {
    return context.isFeatureSupported(HookFeature.CHANGESET_PROVIDER);
  }
}
