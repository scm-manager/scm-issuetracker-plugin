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
package sonia.scm.issuetracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.PullRequestIssueTracker.PullRequestIssueRequestData;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestIssueTrackerTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private PullRequestCommentHandler commentHandler;
  @Mock
  private PullRequestCommentHandlerProvider commentHandlerProvider;

  @InjectMocks
  private PullRequestIssueTracker issueTracker;

  @Nested
  class WithCommentHandler {

    @BeforeEach
    void initializeCommentHandlerProvider() {
      when(commentHandlerProvider.getCommentHandler(any())).thenReturn(commentHandler);
    }

    @Test
    void shouldCreateCommentForRequest() {
      PullRequestIssueRequestData requestData = new PullRequestIssueRequestData("pullRequestCreated", REPOSITORY, "42", asList("#23", "#1337"));

      issueTracker.handlePullRequestRequest(requestData);

      verify(commentHandlerProvider).getCommentHandler(requestData);
      verify(commentHandler).comment("#23");
      verify(commentHandler).comment("#1337");
    }
  }

  @Nested
  class WithoutCommentHandler {

    @BeforeEach
    void initializeCommentHandlerProvider() {
      when(commentHandlerProvider.getCommentHandler(any())).thenReturn(null);
    }

    @Test
    void shouldDoNothingForRequest() {
      PullRequestIssueRequestData requestData = new PullRequestIssueRequestData("pullRequestCreated", REPOSITORY,"42", asList("#23", "#1337"));

      issueTracker.handlePullRequestRequest(requestData);

      verify(commentHandlerProvider).getCommentHandler(requestData);
      verify(commentHandler, never()).comment(any());
    }
  }
}
