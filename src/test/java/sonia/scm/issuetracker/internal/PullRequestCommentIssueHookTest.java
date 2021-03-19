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
import com.cloudogu.scm.review.comment.service.CommentType;
import com.cloudogu.scm.review.comment.service.Reply;
import com.cloudogu.scm.review.comment.service.ReplyEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.PullRequestCommentIssueRequestData;
import sonia.scm.issuetracker.PullRequestIssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static sonia.scm.HandlerEventType.CREATE;
import static sonia.scm.HandlerEventType.MODIFY;

@ExtendWith(MockitoExtension.class)
class PullRequestCommentIssueHookTest {


  @Mock
  private PullRequestIssueTracker issueTracker;
  @Mock
  private IssueMatcher matcher;
  @Mock
  private UserDisplayManager userDisplayManager;

  private PullRequestCommentIssueHook hook;

  @BeforeEach
  void mockMatcher() {
    lenient().when(issueTracker.createMatcher(any())).thenReturn(of(matcher));
    lenient().when(matcher.getKeyPattern()).thenReturn(Pattern.compile("(#\\d+)"));
    lenient().when(matcher.getKey(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Matcher.class).group(0));
  }

  @BeforeEach
  void initHook() {
    hook = new PullRequestCommentIssueHook(Collections.singleton(issueTracker), userDisplayManager);
  }

  @BeforeEach
  void mockUser() {
    lenient().when(userDisplayManager.get("dent")).thenReturn(of(DisplayUser.from(new User("dent", "Arthur Dent", null))));
  }

  @Test
  void shouldHandleCreatedCommentWithIssue() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest("Fix for #42", null);
    Comment comment = createComment();

    CommentEvent commentEvent = new CommentEvent(repository, pullRequest, comment, null, CREATE);

    hook.handle(commentEvent);

    verify(issueTracker).handlePullRequestCommentRequest(argThat(
      data -> assertCorrectRequestData(data, "created")
    ));
  }

  @Test
  void shouldHandleModifiedCommentWithIssue() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest(null, "This fixes #42");
    Comment comment = createComment();
    Comment oldComment = createComment();

    CommentEvent commentEvent = new CommentEvent(repository, pullRequest, comment, oldComment, MODIFY);

    hook.handle(commentEvent);

    verify(issueTracker).handlePullRequestCommentRequest(argThat(
      data -> assertCorrectRequestData(data, "modified")
    ));
  }

  @Test
  void shouldHandleModifiedReplyWithIssue() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest(null, "This fixes #42");
    Comment comment = createComment();
    Reply reply = Reply.createReply("1", "Does not fix #42", "trillian");

    ReplyEvent replyEvent = new ReplyEvent(repository, pullRequest, reply, null, comment, MODIFY);

    hook.handle(replyEvent);

    verify(issueTracker).handlePullRequestCommentRequest(argThat(
      data -> assertCorrectRequestData(data, "modified")
    ));
  }

  @Test
  void shouldNotHandleModifiedCommentChangedCommentType() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest(null, "This fixes #42");
    Comment comment = createComment();
    Comment task = createComment();
    task.setType(CommentType.TASK_TODO);

    CommentEvent commentEvent = new CommentEvent(repository, pullRequest, comment, task, MODIFY);

    hook.handle(commentEvent);

    verify(issueTracker, never()).handlePullRequestCommentRequest(any());
  }

  private boolean assertCorrectRequestData(PullRequestCommentIssueRequestData data, String modified) {
    assertThat(data.getRequestType()).isEqualTo(modified);
    assertThat(data.getComment().getComment()).isEqualTo("Does not fix #42");
    assertThat(data.getIssueIds()).contains("#42");
    assertThat(data.getAuthor().getDisplayName()).isEqualTo("Arthur Dent");
    return true;
  }

  private Comment createComment() {
    return Comment.createComment("1", "Does not fix #42", "dent", null);
  }

  private PullRequest createPullRequest(String title, String description) {
    PullRequest pullRequest = new PullRequest("1", "feature", "main");
    pullRequest.setTitle(title);
    pullRequest.setDescription(description);
    pullRequest.setAuthor("dent");
    return pullRequest;
  }
}
