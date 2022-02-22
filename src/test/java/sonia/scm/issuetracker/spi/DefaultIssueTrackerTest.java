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

package sonia.scm.issuetracker.spi;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.ExampleIssueLinkFactory;
import sonia.scm.issuetracker.ExampleIssueMatcher;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.internal.resubmit.ResubmitQueue;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static sonia.scm.issuetracker.IssueReferencingObjects.content;

class DefaultIssueTrackerTest {

  @Nested
  @ExtendWith(MockitoExtension.class)
  class Reading {

    private IssueTracker tracker;
    @Mock
    private ResubmitQueue resubmitQueue;
    @Mock
    private TemplateCommentRendererFactory rendererFactory;

    @BeforeEach
    void setUpIssueTracker() {
      tracker = new IssueTrackerBuilder(new InMemoryDataStoreFactory(), resubmitQueue, rendererFactory)
        .start("testing", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine())
        .build();
    }

    @Test
    void shouldReturnName() {
      assertThat(tracker.getName()).isEqualTo("testing");
    }

    @Test
    void shouldReturnEmptyOptionalForResubmitter() {
      assertThat(tracker.getResubmitter()).isEmpty();
    }

    @Test
    void shouldCollectIssues() {
      Map<String, String> issues = tracker.findIssues(content("Fix #42"));
      assertThat(issues).containsEntry("#42", "https://redmine.hitchhiker.com/issues/42");
    }

    @Test
    void shouldCollectMultipleIssues() {
      Map<String, String> issues = tracker.findIssues(content("This should fix #42 and #21", "Perhaps #3 too"));
      assertThat(issues)
        .containsEntry("#42", "https://redmine.hitchhiker.com/issues/42")
        .containsEntry("#21", "https://redmine.hitchhiker.com/issues/21")
        .containsEntry("#3", "https://redmine.hitchhiker.com/issues/3");
    }

  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class Commenting {

    @Mock
    private ResubmitQueue resubmitQueue;

    @Mock
    private TemplateCommentRendererFactory rendererFactory;

    @Mock
    private ReferenceCommentRenderer renderer;

    @Mock
    private Commentator commentator;

    private IssueTracker tracker;

    @BeforeEach
    void setUpIssueTracker() {
      tracker = new IssueTrackerBuilder(new InMemoryDataStoreFactory(), resubmitQueue, rendererFactory)
        .start("testing", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine())
        .commenting(RepositoryTestData.createHeartOfGold(), commentator)
        .renderer(renderer)
        .build();
    }

    @Test
    void shouldReturnResubmitter() {
      assertThat(tracker.getResubmitter()).isPresent();
    }

    @Test
    void shouldSendComment() throws IOException {
      IssueReferencingObject ref = content("Comment #42");
      when(renderer.render(ref)).thenReturn("Awesome");

      tracker.process(ref);

      verify(commentator).comment("#42", "Awesome");
    }

    @Test
    void shouldSendCommentsOnlyOnce() throws IOException {
      IssueReferencingObject ref = content("Comment #21");
      when(renderer.render(ref)).thenReturn("Incredible");

      tracker.process(ref);
      verify(commentator).comment("#21", "Incredible");

      tracker.process(ref);
      verifyNoMoreInteractions(commentator);
    }

    @Test
    void shouldSendMultipleComments() throws IOException {
      IssueReferencingObject ref = content("Comment #21", "And comment #42");
      when(renderer.render(ref)).thenReturn("Super");

      tracker.process(ref);
      verify(commentator).comment("#21", "Super");
      verify(commentator).comment("#42", "Super");
    }

  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class StateChange {

    @Mock
    private ResubmitQueue resubmitQueue;

    @Mock
    private TemplateCommentRendererFactory rendererFactory;

    @Mock
    private ReferenceCommentRenderer referenceCommentRenderer;

    @Mock
    private StateChangeCommentRenderer stateChangeCommentRenderer;

    @Mock
    private Commentator commentator;

    @Mock
    private StateChanger stateChanger;

    private IssueTracker tracker;

    @BeforeEach
    void setUpIssueTracker() {
      tracker = new IssueTrackerBuilder(new InMemoryDataStoreFactory(), resubmitQueue, rendererFactory)
        .start("testing", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine())
        .commenting(RepositoryTestData.createHeartOfGold(), commentator)
        .renderer(referenceCommentRenderer)
        .stateChanging(stateChanger)
        .renderer(stateChangeCommentRenderer)
        .build();
    }

    @Nested
    class WithActivatedStateChange {

      @BeforeEach
      void activateStateChange() {
        when(stateChanger.isStateChangeActivatedFor("unit-test")).thenReturn(true);
      }

      @Test
      void shouldChangeState() throws IOException {
        IssueReferencingObject ref = content("Fixes #42");
        when(stateChanger.getKeyWords("#42")).thenReturn(Collections.singleton("fixes"));
        when(stateChangeCommentRenderer.render(ref, "fixes")).thenReturn("Incredible");

        tracker.process(ref);
        verify(stateChanger).changeState("#42", "fixes");
        verify(commentator).comment("#42", "Incredible");
      }

      @Test
      void shouldChangeStateOnlyOnce() throws IOException {
        IssueReferencingObject ref = content("Resolves #21");
        when(stateChanger.getKeyWords("#21")).thenReturn(Collections.singleton("resolves"));
        when(stateChangeCommentRenderer.render(ref, "resolves")).thenReturn("Awesome");

        tracker.process(ref);
        verify(stateChanger).changeState("#21", "resolves");
        verify(commentator).comment("#21", "Awesome");

        tracker.process(ref);
        verifyNoMoreInteractions(stateChanger, commentator);
      }

      @Test
      void shouldChangeStateOfMultipleIssues() throws IOException {
        IssueReferencingObject ref = content("Resolves #21 and #12", "Fixes #42 too");
        Set<String> keywords = ImmutableSet.of("resolves", "fixes");
        when(stateChanger.getKeyWords("#21")).thenReturn(keywords);
        when(stateChanger.getKeyWords("#12")).thenReturn(keywords);
        when(stateChanger.getKeyWords("#42")).thenReturn(keywords);
        when(stateChangeCommentRenderer.render(ref, "resolves")).thenReturn("Awesome");
        when(stateChangeCommentRenderer.render(ref, "fixes")).thenReturn("Incredible");

        tracker.process(ref);
        verify(stateChanger).changeState("#21", "resolves");
        verify(commentator).comment("#21", "Awesome");
        verify(stateChanger).changeState("#12", "resolves");
        verify(commentator).comment("#12", "Awesome");
        verify(stateChanger).changeState("#42", "fixes");
        verify(commentator).comment("#42", "Incredible");
      }
    }

    @Test
    void shouldNotChangeStateForCommitIfDisabled() throws IOException {
      IssueReferencingObject ref = content("Fixes #42");
      when(stateChanger.getKeyWords("#42")).thenReturn(Collections.singleton("fixes"));
      when(stateChangeCommentRenderer.render(ref, "fixes")).thenReturn("Incredible");

      tracker.process(ref);
      verify(stateChanger, never()).changeState("#42", "fixes");
      verify(commentator).comment("#42", "Incredible");
    }

    @Test
    void shouldNotChangeStateForRefWhichDoesNotTriggerStateChanges() throws IOException {
      IssueReferencingObject ref = content(false, "Fixes #21");
      when(referenceCommentRenderer.render(ref)).thenReturn("Great");

      tracker.process(ref);
      verify(commentator).comment("#21", "Great");
      verify(stateChanger, never()).isStateChangeActivatedFor(anyString());
    }

    @Test
    void shouldAddCommentWithoutKeyWord() throws IOException {
      IssueReferencingObject ref = content("#42 is great");
      when(stateChanger.getKeyWords("#42")).thenReturn(ImmutableSet.of("resolves", "fixes"));
      when(referenceCommentRenderer.render(ref)).thenReturn("Great");

      tracker.process(ref);
      verify(commentator).comment("#42", "Great");
      verify(stateChanger, never()).isStateChangeActivatedFor(anyString());
    }
  }
}
