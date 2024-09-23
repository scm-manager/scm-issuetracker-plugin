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

package sonia.scm.issuetracker.internal.review;

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.issuetracker.IssueReferencingObjects;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PullRequestSubscriberTest {

  @Mock
  private IssueTracker issueTracker;

  @Mock
  private PullRequestMapper mapper;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final PullRequest pullRequest = new PullRequest();

  private PullRequestSubscriber subscriber;

  @BeforeEach
  void setUp() {
    subscriber = new PullRequestSubscriber(issueTracker, mapper);
  }

  @Nested
  class CreateOrModify {

    @Test
    void shouldProcessEvent() {
      IssueReferencingObject ref = IssueReferencingObjects.ref("pr", "21");
      when(mapper.ref(repository, pullRequest, false)).thenReturn(ref);

      PullRequestEvent event = new PullRequestEvent(repository, pullRequest, null, HandlerEventType.CREATE);
      subscriber.handle(event);

      verify(issueTracker).process(ref);
    }

    @Test
    void shouldIgnoreUnsupportedEventTypes() {
      PullRequestEvent event = new PullRequestEvent(repository, pullRequest, null, HandlerEventType.DELETE);
      subscriber.handle(event);

      verify(issueTracker, never()).process(any());
    }

  }

  @Nested
  class Merge {

    @Test
    void shouldProcessMergeEvent() {
      IssueReferencingObject ref = IssueReferencingObjects.ref("pull-request", "42");
      when(mapper.ref(repository, pullRequest, true)).thenReturn(ref);

      PullRequestMergedEvent event = new PullRequestMergedEvent(repository, pullRequest);
      subscriber.handle(event);

      verify(issueTracker).process(ref);
    }

  }

}
