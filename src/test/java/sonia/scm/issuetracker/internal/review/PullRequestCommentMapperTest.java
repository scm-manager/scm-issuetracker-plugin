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

package sonia.scm.issuetracker.internal.review;

import com.cloudogu.scm.review.comment.service.Comment;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.internal.PersonMapper;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.UserDisplayManager;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PullRequestCommentMapperTest {

  @Mock
  private UserDisplayManager userDisplayManager;

  private PullRequestCommentMapper mapper;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final PullRequest pullRequest = PullRequest.builder().id("4211").build();

  @BeforeEach
  void setUpMapper() {
    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setBaseUrl("https://scm");
    mapper = new PullRequestCommentMapper(configuration, new PersonMapper(userDisplayManager));
  }

  @Test
  void shouldMapWithPullRequest() {
    Comment comment = Comment.createComment("3", "Awesome Comment", "tricia", null);
    IssueReferencingObject ref = mapper.ref(repository, pullRequest, comment);
    assertThat(ref.getId()).isEqualTo("3");
    assertThat(ref.getType()).isEqualTo(PullRequestCommentMapper.TYPE);
    assertThat(ref.getAuthor().getName()).isEqualTo("tricia");
    assertThat(ref.getDate()).isSameAs(comment.getDate());
    assertThat(ref.getContent()).first().satisfies(c -> {
      assertThat(c.getType()).isEqualTo("comment");
      assertThat(c.getValue()).isEqualTo("Awesome Comment");
    });
    assertThat(ref.getLink()).isEqualTo("https://scm/repo/hitchhiker/HeartOfGold/pull-request/4211/comments#comment-3");
    assertThat(ref.isTriggeringStateChange()).isFalse();
    assertThat(ref.getOrigin()).isSameAs(comment);
  }

  @Test
  void shouldMapLinkWithoutPullRequest() {
    Comment comment = Comment.createComment("21", "Incredible Comment", "dent", null);
    IssueReferencingObject ref = mapper.ref(repository, comment);
    assertThat(ref.getLink()).isEqualTo("https://scm/repo/hitchhiker/HeartOfGold/pull-requests");
  }
}
