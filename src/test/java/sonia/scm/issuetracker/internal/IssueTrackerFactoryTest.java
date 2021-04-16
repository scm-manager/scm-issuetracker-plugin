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
