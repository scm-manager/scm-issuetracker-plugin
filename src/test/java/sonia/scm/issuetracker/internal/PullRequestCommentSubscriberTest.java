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

import com.cloudogu.scm.review.comment.service.Comment;
import com.cloudogu.scm.review.comment.service.CommentEvent;
import com.cloudogu.scm.review.comment.service.Reply;
import com.cloudogu.scm.review.comment.service.ReplyEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PullRequestCommentSubscriberTest {

  @Mock
  private IssueTracker issueTracker;

  @Mock
  private PullRequestCommentMapper mapper;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final PullRequest pullRequest = new PullRequest();

  private PullRequestCommentSubscriber subscriber;

  @BeforeEach
  void setUp() {
    subscriber = new PullRequestCommentSubscriber(issueTracker, mapper);
  }

  @Test
  void shouldProcessCommentEvents() {
    Comment comment = new Comment();

    IssueReferencingObject ref = IssueReferencingObjects.ref("comment", "42");
    when(mapper.ref(repository, pullRequest, comment)).thenReturn(ref);

    CommentEvent event = new CommentEvent(repository, pullRequest, comment, null, HandlerEventType.CREATE);
    subscriber.handle(event);

    verify(issueTracker).process(ref);
  }

  @Test
  void shouldProcessReplyEvents() {
    Reply reply = new Reply();

    IssueReferencingObject ref = IssueReferencingObjects.ref("reply", "21");
    when(mapper.ref(repository, pullRequest, reply)).thenReturn(ref);

    ReplyEvent event = new ReplyEvent(repository, pullRequest, reply, null, new Comment(), HandlerEventType.CREATE);
    subscriber.handle(event);

    verify(issueTracker).process(ref);
  }

  @Test
  void shouldIgnoreUnsupportedEventTypes() {
    CommentEvent event = new CommentEvent(repository, pullRequest, new Comment(), null, HandlerEventType.BEFORE_CREATE);
    subscriber.handle(event);

    verify(issueTracker, never()).process(any());
  }

}
