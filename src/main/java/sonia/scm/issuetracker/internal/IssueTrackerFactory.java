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

import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IssueTrackerFactory {

  private final IssueTrackerBuilder builder;
  private final Set<IssueTrackerProvider> providers;

  @Inject
  public IssueTrackerFactory(IssueTrackerBuilder builder, Set<IssueTrackerProvider> providers) {
    this.builder = builder;
    this.providers = providers;
  }

  public Iterable<IssueTracker> trackers(Repository repository) {
    return stream(repository)
      .collect(Collectors.toList());
  }

  public Optional<IssueTracker> tracker(Repository repository, String name) {
    return stream(repository)
      .filter(tracker -> name.equals(tracker.getName()))
      .findAny();
  }

  private Stream<IssueTracker> stream(Repository repository) {
    return providers.stream()
      .map(provider -> provider.create(builder, repository))
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

}
