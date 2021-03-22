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
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.PullRequestIssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestMergeHookTest {

  @Mock
  private PullRequestIssueTracker issueTracker;

  @Mock
  private IssueMatcher matcher;

  private PullRequestMergeHook hook;

  @BeforeEach
  void mockMatcher() {
    when(issueTracker.getIssueStateKeywords()).thenReturn(Arrays.asList("create", "modify"));
    when(issueTracker.createMatcher(any())).thenReturn(Optional.of(matcher));
    when(matcher.getKeyPattern()).thenReturn(Pattern.compile("(#\\d+)"));
    when(matcher.getKey(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Matcher.class).group(0));
  }

  @BeforeEach
  void initHook() {
    hook = new PullRequestMergeHook(Collections.singleton(issueTracker));
  }

  @Test
  void shouldHandleCreatedPullRequestWithTransitionInTitle() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest("modify issue with id #42", null);

    PullRequestMergedEvent pullRequestMergedEvent = new PullRequestMergedEvent(repository, pullRequest);

    hook.handle(pullRequestMergedEvent);

    verify(issueTracker).handleMergePullRequestMergeRequest(argThat(this::assertCorrectRequestData));
  }

  @Test
  void shouldHandleCreatedPullRequestWithMultipleTransitionsInTitle() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest("modify issue with id #42 and #43", null);

    PullRequestMergedEvent pullRequestMergedEvent = new PullRequestMergedEvent(repository, pullRequest);

    hook.handle(pullRequestMergedEvent);

    verify(issueTracker).handleMergePullRequestMergeRequest(argThat(data -> {
      assertThat(data.getPullRequest().getId()).isEqualTo("1");
      assertThat(data.getStateTransitions()).containsEntry("#42", "modify");
      assertThat(data.getStateTransitions()).containsEntry("#43", "modify");
      return true;
    }));
  }

  @Test
  void shouldHandleCreatedPullRequestWithTransitionInDescription() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest(null, "modify issue with id #42");

    PullRequestMergedEvent pullRequestMergedEvent = new PullRequestMergedEvent(repository, pullRequest);

    hook.handle(pullRequestMergedEvent);

    verify(issueTracker).handleMergePullRequestMergeRequest(argThat(this::assertCorrectRequestData));
  }

  @Test
  void shouldHandleCreatedPullRequestWithTransitionInMultilineDescription() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    PullRequest pullRequest = createPullRequest(null, "modify issue with id #42 \n but not create issue with \n id #43 \n but neither create. Issue with id #44 \n not #45. but create #46");

    PullRequestMergedEvent pullRequestMergedEvent = new PullRequestMergedEvent(repository, pullRequest);

    hook.handle(pullRequestMergedEvent);

    verify(issueTracker).handleMergePullRequestMergeRequest(argThat(data -> {
      assertThat(data.getPullRequest().getId()).isEqualTo("1");
      assertThat(data.getStateTransitions()).containsEntry("#42", "modify");
      assertThat(data.getStateTransitions()).containsEntry("#46", "create");
      assertThat(data.getStateTransitions()).doesNotContainEntry("#43", "create");
      assertThat(data.getStateTransitions()).doesNotContainEntry("#44", "create");
      assertThat(data.getStateTransitions()).doesNotContainEntry("#45", "create");
      return true;
    }));
  }

  private boolean assertCorrectRequestData(PullRequestIssueTracker.PullRequestMergeRequestData data) {
    assertThat(data.getPullRequest().getId()).isEqualTo("1");
    assertThat(data.getStateTransitions()).containsEntry("#42", "modify");
    return true;
  }

  private PullRequest createPullRequest(String title, String description) {
    PullRequest pullRequest = new PullRequest("1", "feature", "main");
    pullRequest.setTitle(title);
    pullRequest.setDescription(description);
    return pullRequest;
  }

}
