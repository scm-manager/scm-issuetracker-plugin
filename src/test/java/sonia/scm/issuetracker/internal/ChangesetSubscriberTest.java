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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.IssueReferencingObjects;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangesetSubscriberTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HookContext context;

  @Mock
  private PostReceiveRepositoryHookEvent event;

  @Mock
  private IssueTracker issueTracker;

  @Mock
  private ChangesetMapper mapper;

  @InjectMocks
  private ChangesetSubscriber subscriber;

  @BeforeEach
  void prepare() {
    when(event.getContext()).thenReturn(context);
  }

  @Test
  void shouldIgnoreUnsupportedEvents() {
    subscriber.handle(event);
    verify(issueTracker, never()).process(any());
  }

  @Test
  void shouldProcessEvents() {
    Repository repository = RepositoryTestData.create42Puzzle();
    when(event.getRepository()).thenReturn(repository);
    when(context.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)).thenReturn(true);

    Changeset one = new Changeset();
    one.setId("1");
    Changeset two = new Changeset();
    two.setId("2");

    when(context.getChangesetProvider().setDisablePreProcessors(true).getChangesets()).thenReturn(
      ImmutableList.of(one, two)
    );

    IssueReferencingObject refOne = IssueReferencingObjects.ref("sample", "one");
    doReturn(refOne).when(mapper).ref(repository, one);
    IssueReferencingObject refTwo = IssueReferencingObjects.ref("sample", "two");
    doReturn(refTwo).when(mapper).ref(repository, two);

    subscriber.handle(event);

    verify(issueTracker).process(refOne);
    verify(issueTracker).process(refTwo);
  }
}
