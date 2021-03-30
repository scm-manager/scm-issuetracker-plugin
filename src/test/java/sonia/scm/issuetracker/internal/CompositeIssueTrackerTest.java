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
import static sonia.scm.issuetracker.IssueReferencingObjects.ref;

@ExtendWith(MockitoExtension.class)
class CompositeIssueTrackerTest {

  @Mock
  private IssueTrackerBuilder builder;

  @Mock
  private IssueTracker redmine;

  @Mock
  private IssueTracker jira;

  @Test
  void shouldDelegateProcessToPresentIssueTrackers() {
    CompositeIssueTracker tracker = tracker(redmine, null, jira);

    IssueReferencingObject ref = ref();
    tracker.process(ref);
    verify(redmine).process(ref);
    verify(jira).process(ref);
  }

  @Test
  void shouldMergeIssues() {
    IssueReferencingObject ref = ref();
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
    return new CompositeIssueTracker(builder, ImmutableSet.copyOf(providers));
  }

  private IssueTrackerProvider provider(IssueTracker two) {
    return (b, r) -> Optional.ofNullable(two);
  }

}
