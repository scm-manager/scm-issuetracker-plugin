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

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import org.junit.jupiter.api.BeforeEach;
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

  @Test
  void shouldProcessEvent() {
    IssueReferencingObject ref = IssueReferencingObjects.ref("pr", "21");
    when(mapper.ref(repository, pullRequest)).thenReturn(ref);

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
