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
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sonia.scm.issuetracker.IssueReferencingObjects.ref;

class DefaultIssueTrackerTest {

  @Nested
  class Reading {

    private IssueTracker tracker;

    @BeforeEach
    void setUpIssueTracker() {
      tracker = new IssueTrackerBuilder(new InMemoryDataStoreFactory())
        .start("testing", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine())
        .build();
    }

    @Test
    void shouldCollectIssues() {
      Map<String, String> issues = tracker.findIssues(ref("Fix #42"));
      assertThat(issues).containsEntry("#42", "https://redmine.hitchhiker.com/issues/42");
    }

    @Test
    void shouldCollectMultipleIssues() {
      Map<String, String> issues = tracker.findIssues(ref("This should fix #42 and #21", "Perhaps #3 too"));
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
    private CommentRenderer renderer;

    @Mock
    private Commentator commentator;

    private IssueTracker tracker;

    @BeforeEach
    void setUpIssueTracker() {
      tracker = new IssueTrackerBuilder(new InMemoryDataStoreFactory())
        .start("testing", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine())
        .commenting(RepositoryTestData.createHeartOfGold(), renderer, commentator)
        .build();
    }

    @Test
    void shouldSendComment() throws IOException {
      IssueReferencingObject ref = ref("Comment #42");
      when(renderer.reference(ref)).thenReturn("Awesome");

      tracker.process(ref);

      verify(commentator).comment("#42", "Awesome");
    }

    @Test
    void shouldSendCommentsOnlyOnce() throws IOException {
      IssueReferencingObject ref = ref("Comment #21");
      when(renderer.reference(ref)).thenReturn("Incredible");

      tracker.process(ref);
      verify(commentator).comment("#21", "Incredible");

      tracker.process(ref);
      verifyNoMoreInteractions(commentator);
    }

    @Test
    void shouldSendMultipleComments() throws IOException {
      IssueReferencingObject ref = ref("Comment #21", "And comment #42");
      when(renderer.reference(ref)).thenReturn("Super");

      tracker.process(ref);
      verify(commentator).comment("#21", "Super");
      verify(commentator).comment("#42", "Super");
    }

  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class StateChange {

    @Mock
    private CommentRenderer renderer;

    @Mock
    private Commentator commentator;

    @Mock
    private StateChanger stateChanger;

    private IssueTracker tracker;

    @BeforeEach
    void setUpIssueTracker() {
      tracker = new IssueTrackerBuilder(new InMemoryDataStoreFactory())
        .start("testing", ExampleIssueMatcher.createRedmine(), ExampleIssueLinkFactory.createRedmine())
        .commenting(RepositoryTestData.createHeartOfGold(), renderer, commentator)
        .stateChanging(stateChanger)
        .build();
    }

    @Test
    void shouldChangeState() throws IOException {
      IssueReferencingObject ref = ref("Fixes #42");
      when(stateChanger.getKeyWords("#42")).thenReturn(Collections.singleton("fixes"));
      when(renderer.stateChange(ref, "fixes")).thenReturn("Incredible");

      tracker.process(ref);
      verify(stateChanger).changeState("#42", "fixes");
      verify(commentator).comment("#42", "Incredible");
    }

    @Test
    void shouldChangeStateOnlyOnce() throws IOException {
      IssueReferencingObject ref = ref("Resolves #21");
      when(stateChanger.getKeyWords("#21")).thenReturn(Collections.singleton("resolves"));
      when(renderer.stateChange(ref, "resolves")).thenReturn("Awesome");

      tracker.process(ref);
      verify(stateChanger).changeState("#21", "resolves");
      verify(commentator).comment("#21", "Awesome");

      tracker.process(ref);
      verifyNoMoreInteractions(stateChanger, commentator);
    }

    @Test
    void shouldChangeStateOfMultipleIssues() throws IOException {
      IssueReferencingObject ref = ref("Resolves #21 and #12", "Fixes #42 too");
      Set<String> keywords = ImmutableSet.of("resolves", "fixes");
      when(stateChanger.getKeyWords("#21")).thenReturn(keywords);
      when(stateChanger.getKeyWords("#12")).thenReturn(keywords);
      when(stateChanger.getKeyWords("#42")).thenReturn(keywords);
      when(renderer.stateChange(ref, "resolves")).thenReturn("Awesome");
      when(renderer.stateChange(ref, "fixes")).thenReturn("Incredible");

      tracker.process(ref);
      verify(stateChanger).changeState("#21", "resolves");
      verify(commentator).comment("#21", "Awesome");
      verify(stateChanger).changeState("#12", "resolves");
      verify(commentator).comment("#12", "Awesome");
      verify(stateChanger).changeState("#42", "fixes");
      verify(commentator).comment("#42", "Incredible");
    }

    @Test
    void shouldAddCommentWithoutKeyWord() throws IOException {
      IssueReferencingObject ref = ref("#42 is great");
      when(stateChanger.getKeyWords("#42")).thenReturn(ImmutableSet.of("resolves", "fixes"));
      when(renderer.reference(ref)).thenReturn("Great");

      tracker.process(ref);
      verify(commentator).comment("#42", "Great");
    }

  }

}
