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

package sonia.scm.issuetracker.api;

import com.google.inject.ImplementedBy;
import sonia.scm.issuetracker.internal.CompositeIssueTracker;

import java.util.Map;
import java.util.Optional;

/**
 * Main api to find and process issues.
 *
 * @since 3.0.0
 */
@ImplementedBy(CompositeIssueTracker.class)
public interface IssueTracker {

  /**
   * Process the {@link IssueReferencingObject}.
   * What exactly processing means is up the implementation of the issue tracker.
   * This can be commenting or changing state of the issue.
   *
   * @param object issue referencing object
   */
  void process(IssueReferencingObject object);

  /**
   * Find issues in the {@link IssueReferencingObject}.
   * @param object issue referencing object
   * @return map of issues, key is the issue id and value is a link to issue
   */
  Map<String, String> findIssues(IssueReferencingObject object);

  /**
   * Returns the name of the issue tracker.
   * @return name of issue tracker
   * @since 3.1.0
   */
  default String getName() {
    return getClass().getName();
  }

  /**
   * Returns a {@link Resubmitter} if the issue tracker supports resubmitting of comments.
   * If the tracker does not support resubmitting the method returns an empty optional.
   *
   * @return optional with {@link Resubmitter} or empty if resubmitting is not supported
   * @since 3.1.0
   */
  default Optional<Resubmitter> getResubmitter() {
    return Optional.empty();
  }
}
