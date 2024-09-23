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
