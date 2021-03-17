package sonia.scm.issuetracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.PullRequestIssueTracker.PullRequestIssueRequestData;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestIssueTrackerTest {

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
      PullRequestIssueRequestData requestData = new PullRequestIssueRequestData("pullRequestCreated", "42", asList("#23", "#1337"));

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
      PullRequestIssueRequestData requestData = new PullRequestIssueRequestData("pullRequestCreated", "42", asList("#23", "#1337"));

      issueTracker.handlePullRequestRequest(requestData);

      verify(commentHandlerProvider).getCommentHandler(requestData);
      verify(commentHandler, never()).comment(any());
    }
  }
}
