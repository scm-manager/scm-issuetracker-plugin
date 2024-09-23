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
