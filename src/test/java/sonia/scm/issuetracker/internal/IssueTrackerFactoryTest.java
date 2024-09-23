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


import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;
import sonia.scm.repository.RepositoryTestData;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueTrackerFactoryTest {

  @Mock
  private IssueTracker redmine;

  @Mock
  private IssueTracker jira;

  @Mock
  private IssueTrackerBuilder builder;

  @Test
  void shouldReturnEmptyIterableWithoutIssueTrackers() {
    IssueTrackerFactory factory = factory();
    Iterable<IssueTracker> trackers = factory.trackers(RepositoryTestData.createHeartOfGold());
    assertThat(trackers).isEmpty();
  }

  @Test
  void shouldReturnIssueTrackers() {
    IssueTrackerFactory factory = factory(redmine, jira);
    Iterable<IssueTracker> trackers = factory.trackers(RepositoryTestData.createHeartOfGold());
    assertThat(trackers).hasSize(2);
  }

  @Test
  void shouldReturnEmptyOptionalWithIssueTrackers() {
    IssueTrackerFactory factory = factory();
    Optional<IssueTracker> tracker = factory.tracker(RepositoryTestData.create42Puzzle(), "redmine");
    assertThat(tracker).isEmpty();
  }

  @Test
  void shouldReturnEmptyOptionalWithoutMatchingTrackers() {
    IssueTrackerFactory factory = factory(redmine, jira);
    Optional<IssueTracker> tracker = factory.tracker(RepositoryTestData.create42Puzzle(), "track");
    assertThat(tracker).isEmpty();
  }

  @Test
  void shouldReturnIssueTracker() {
    when(redmine.getName()).thenReturn("redmine");
    IssueTrackerFactory factory = factory(redmine, jira);
    Optional<IssueTracker> tracker = factory.tracker(RepositoryTestData.create42Puzzle(), "redmine");
    assertThat(tracker).contains(redmine);
  }

  private IssueTrackerFactory factory(IssueTracker... trackers) {
    Set<IssueTrackerProvider> providers = Arrays.stream(trackers)
      .map(this::provider)
      .collect(Collectors.toSet());
    return new IssueTrackerFactory(builder, ImmutableSet.copyOf(providers));
  }

  private IssueTrackerProvider provider(IssueTracker tracker) {
    return (b, r) -> Optional.ofNullable(tracker);
  }
}
