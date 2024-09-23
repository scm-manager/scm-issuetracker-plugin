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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

import java.util.Optional;

/**
 * Creates issue trackers for an external issue tracker system such as jira or redmine.
 * This is the main entry point for plugin developers that want to connect to a new issue tracker system.
 *
 * @since 3.0.0
 */
@ExtensionPoint
public interface IssueTrackerProvider {

  /**
   * Create a new issue tracker for the given repository wrapped in an {@link Optional} or
   * {@link Optional#empty()} if no issue tracker is configured or applicable.
   *
   * @param builder builder to create a default implementation of the issue tracker
   * @param repository target repository
   *
   * @return optional issue tracker
   */
  Optional<IssueTracker> create(IssueTrackerBuilder builder, Repository repository);

}
