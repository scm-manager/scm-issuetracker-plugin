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

import jakarta.inject.Inject;

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
