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
