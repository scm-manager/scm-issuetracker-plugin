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
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.PullRequestIssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.HandlerEventType.CREATE;
import static sonia.scm.HandlerEventType.MODIFY;

@ExtendWith(MockitoExtension.class)
class PullRequestIssueHookTest {

  @Mock
  private PullRequestIssueTracker issueTracker;
  @Mock
  private IssueMatcher matcher;

  private PullRequestIssueHook hook;

  @BeforeEach
  void mockMatcher() {
    when(issueTracker.createMatcher(any())).thenReturn(Optional.of(matcher));
    when(matcher.getKeyPattern()).thenReturn(Pattern.compile("(#\\d+)"));
    when(matcher.getKey(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Matcher.class).group(0));
  }

  @BeforeEach
  void initHook() {
    hook = new PullRequestIssueHook(Collections.singleton(issueTracker));
  }

  @Test
  void shouldHandleCreatedPullRequestWithIssueInTitle() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest("Fix for #42", null);

    PullRequestEvent pullRequestEvent = new PullRequestEvent(repository, pullRequest, null, CREATE);

    hook.handle(pullRequestEvent);

    verify(issueTracker).handlePullRequestRequest(argThat(
      data -> assertCorrectRequestData(data, "created")
    ));
  }

  @Test
  void shouldHandleCreatedPullRequestWithIssueInDescription() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest(null, "This fixes #42");

    PullRequestEvent pullRequestEvent = new PullRequestEvent(repository, pullRequest, null, CREATE);

    hook.handle(pullRequestEvent);

    verify(issueTracker).handlePullRequestRequest(argThat(
      data -> assertCorrectRequestData(data, "created")
    ));
  }

  @Test
  void shouldHandleModifiedPullRequestWithNewIssueInTitle() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest oldPullRequest = createPullRequest("This is some fix", null);
    PullRequest newPullRequest = createPullRequest("This is some fix for #42", null);

    PullRequestEvent pullRequestEvent = new PullRequestEvent(repository, newPullRequest, oldPullRequest, MODIFY);

    hook.handle(pullRequestEvent);

    verify(issueTracker).handlePullRequestRequest(argThat(
      data -> assertCorrectRequestData(data, "modified")
    ));
  }

  @Test
  void shouldHandleModifiedPullRequestWithNewIssueInDescription() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest oldPullRequest = createPullRequest(null, "This is some fix");
    PullRequest newPullRequest = createPullRequest(null, "This is some fix for #42");

    PullRequestEvent pullRequestEvent = new PullRequestEvent(repository, newPullRequest, oldPullRequest, MODIFY);

    hook.handle(pullRequestEvent);

    verify(issueTracker).handlePullRequestRequest(argThat(
      data -> assertCorrectRequestData(data, "modified")
    ));
  }

  private boolean assertCorrectRequestData(PullRequestIssueTracker.PullRequestIssueRequestData data, String modified) {
    assertThat(data.getRequestType()).isEqualTo(modified);
    assertThat(data.getPullRequestId()).isEqualTo("1");
    assertThat(data.getIssueIds()).contains("#42");
    return true;
  }

  private PullRequest createPullRequest(String title, String description) {
    PullRequest pullRequest = new PullRequest("1", "feature", "main");
    pullRequest.setTitle(title);
    pullRequest.setDescription(description);
    return pullRequest;
  }
}
