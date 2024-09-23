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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sonia.scm.issuetracker.IssueReferencingObjects.content;

@ExtendWith(MockitoExtension.class)
class CompositeIssueTrackerTest {

  @Mock
  private IssueTrackerBuilder builder;

  @Mock
  private IssueTracker redmine;

  @Mock
  private IssueTracker jira;

  @Test
  void shouldReturnName() {
    CompositeIssueTracker tracker = tracker();
    assertThat(tracker.getName()).isEqualTo(CompositeIssueTracker.NAME);
  }

  @Test
  void shouldDelegateProcessToPresentIssueTrackers() {
    CompositeIssueTracker tracker = tracker(redmine, null, jira);

    IssueReferencingObject ref = content();
    tracker.process(ref);
    verify(redmine).process(ref);
    verify(jira).process(ref);
  }

  @Test
  void shouldMergeIssues() {
    IssueReferencingObject ref = content();
    when(redmine.findIssues(ref)).thenReturn(ImmutableMap.of("#42", "https://redmine/42"));
    when(jira.findIssues(ref)).thenReturn(ImmutableMap.of("ABC-21", "https://jira/ABC-21"));

    CompositeIssueTracker tracker = tracker(redmine, null, jira);
    Map<String, String> issues = tracker.findIssues(ref);

    assertThat(issues)
      .containsEntry("#42", "https://redmine/42")
      .containsEntry("ABC-21", "https://jira/ABC-21");
  }

  private CompositeIssueTracker tracker(IssueTracker... trackers) {
    Set<IssueTrackerProvider> providers = Arrays.stream(trackers).map(this::provider).collect(Collectors.toSet());
    IssueTrackerFactory factory = new IssueTrackerFactory(builder, ImmutableSet.copyOf(providers));
    return new CompositeIssueTracker(factory);
  }

  private IssueTrackerProvider provider(IssueTracker two) {
    return (b, r) -> Optional.ofNullable(two);
  }

}
