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

package sonia.scm.issuetracker.internal.resubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.api.Resubmitter;
import sonia.scm.issuetracker.internal.IssueTrackerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResubmitProcessorTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private IssueTrackerFactory issueTrackerFactory;

  @Mock
  private ResubmitQueue queue;

  @InjectMocks
  private ResubmitProcessor processor;

  @Mock
  private IssueTracker issueTracker;

  @Mock
  private Resubmitter resubmitter;

  @Test
  void shouldRemoveAllIfRepositoryDoesNotExists() {
    List<QueuedComment> comments = Arrays.asList(
      comment("hog", "redmine"),
      comment("hog", "redmine")
    );
    when(queue.getComments("redmine")).thenReturn(comments);

    processor.resubmit("redmine");

    assertThat(processor.getRemove()).containsAll(comments);
  }

  @Test
  void shouldRequeueAllIfTrackerDoesNotExists() {
    List<QueuedComment> comments = Arrays.asList(
      comment("p42", "jira"),
      comment("p42", "jira")
    );
    when(queue.getComments("jira")).thenReturn(comments);
    when(repositoryManager.get("p42")).thenReturn(RepositoryTestData.create42Puzzle());

    processor.resubmit("jira");

    assertThat(processor.getRequeue()).containsAll(comments);
  }

  @Test
  void shouldRequeueAllIfTrackerDoesNotSupportResubmit() {
    List<QueuedComment> comments = Arrays.asList(
      comment("hvpt", "trac"),
      comment("hvpt", "trac")
    );
    when(queue.getComments("trac")).thenReturn(comments);
    Repository repository = RepositoryTestData.createHappyVerticalPeopleTransporter();
    when(repositoryManager.get("hvpt")).thenReturn(repository);
    when(issueTrackerFactory.tracker(repository, "trac")).thenReturn(Optional.of(issueTracker));

    processor.resubmit("trac");

    assertThat(processor.getRequeue()).containsAll(comments);
  }

  @Test
  void shouldRemoveResbumitted() throws IOException {
    QueuedComment one = comment("rateotu", "youtrack");
    QueuedComment two = comment("rateotu", "youtrack");
    List<QueuedComment> comments = Arrays.asList(one, two);

    when(queue.getComments("youtrack")).thenReturn(comments);
    Repository repository = RepositoryTestData.createRestaurantAtTheEndOfTheUniverse();
    when(repositoryManager.get("rateotu")).thenReturn(repository);
    when(issueTrackerFactory.tracker(repository, "youtrack")).thenReturn(Optional.of(issueTracker));
    when(issueTracker.getResubmitter()).thenReturn(Optional.of(resubmitter));

    processor.resubmit("youtrack");

    assertThat(processor.getRemove()).containsAll(comments);
    verify(resubmitter, times(2)).resubmit(anyString(), anyString());
    verify(resubmitter, atLeastOnce()).resubmit(one.getIssueKey(), one.getComment());
    verify(resubmitter, atLeastOnce()).resubmit(two.getIssueKey(), two.getComment());
  }

  @Test
  void shouldRequeueFailed() throws IOException {
    QueuedComment one = comment("hog", "bugzilla");
    QueuedComment two = comment("hog", "bugzilla");
    List<QueuedComment> comments = Arrays.asList(one, two);

    when(queue.getComments("bugzilla")).thenReturn(comments);
    Repository repository = RepositoryTestData.createRestaurantAtTheEndOfTheUniverse();
    when(repositoryManager.get("hog")).thenReturn(repository);
    when(issueTrackerFactory.tracker(repository, "bugzilla")).thenReturn(Optional.of(issueTracker));
    when(issueTracker.getResubmitter()).thenReturn(Optional.of(resubmitter));

    doNothing().when(resubmitter).resubmit(one.getIssueKey(), one.getComment());
    doThrow(new IOException("failed")).when(resubmitter).resubmit(two.getIssueKey(), two.getComment());

    processor.resubmit("bugzilla");

    assertThat(processor.getRemove()).containsOnly(one);
    assertThat(processor.getRequeue()).containsOnly(two);
  }

  private AtomicInteger counter;

  @BeforeEach
  void setUpCounter() {
    counter = new AtomicInteger();
  }

  private QueuedComment comment(String repository, String issueTracker) {
    return new QueuedComment(repository, issueTracker, "#" + counter.incrementAndGet(), "Awesome", 0);
  }
}
