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

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;

import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Delegates and merges results of multiple {@link IssueTracker}.
 *
 * @since 3.0.0
 */
public final class CompositeIssueTracker implements IssueTracker {

  @VisibleForTesting
  static final String NAME = "composite";

  private final IssueTrackerFactory issueTrackerFactory;

  @Inject
  public CompositeIssueTracker(IssueTrackerFactory issueTrackerFactory) {
    this.issueTrackerFactory = issueTrackerFactory;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void process(IssueReferencingObject object) {
    for (IssueTracker tracker : trackers(object)) {
      tracker.process(object);
    }
  }

  @Override
  public Map<String,String> findIssues(IssueReferencingObject object) {
    Map<String,String> issues = new LinkedHashMap<>();
    for (IssueTracker tracker : trackers(object)) {
      Map<String, String> trackerMap = tracker.findIssues(object);
      if (trackerMap != null) {
        issues.putAll(trackerMap);
      }
    }
    return issues;
  }

  private Iterable<IssueTracker> trackers(IssueReferencingObject object) {
    return issueTrackerFactory.trackers(object.getRepository());
  }
}
