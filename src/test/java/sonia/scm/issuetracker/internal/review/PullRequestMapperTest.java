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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class PullRequestMapperTest {

  @Mock
  private UserDisplayManager userDisplayManager;

  private PullRequestMapper mapper;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void setUp() {
    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setBaseUrl("https://hitchhiker.com");
    mapper = new PullRequestMapper(configuration, new PersonMapper(userDisplayManager));
  }

  @Test
  void shouldMap() {
    Instant now = Instant.now();
    PullRequest pr = PullRequest.builder()
      .id("42")
      .author("tricia")
      .title("Awesome")
      .description("This pr so awesome")
      .lastModified(now)
      .build();

    IssueReferencingObject ref = mapper.ref(repository, pr, false);
    assertThat(ref.getRepository()).isSameAs(repository);
    assertThat(ref.getType()).isEqualTo(PullRequestMapper.TYPE);
    assertThat(ref.getAuthor().getName()).isEqualTo("tricia");
    assertThat(ref.getDate()).isSameAs(now);
    assertThat(ref.getContent())
      .anyMatch(c -> "title".equals(c.getType()) && "Awesome".equals(c.getValue()))
      .anyMatch(c -> "description".equals(c.getType()) && "This pr so awesome".equals(c.getValue()));
    assertThat(ref.getLink()).isEqualTo("https://hitchhiker.com/repo/hitchhiker/HeartOfGold/pull-request/42");
    assertThat(ref.getOrigin()).isSameAs(pr);
  }

  @Test
  void shouldPreferLastModifiedOverCreationDate() {
    Instant creationDate = Instant.now();
    Instant lastModified = Instant.now();
    PullRequest pr = PullRequest.builder()
      .id("21")
      .author("dent")
      .title("Incredible")
      .description("This pr so ...")
      .lastModified(lastModified)
      .creationDate(creationDate)
      .build();

    IssueReferencingObject ref = mapper.ref(repository, pr, false);
    assertThat(ref.getDate()).isSameAs(lastModified);
  }

  @Test
  void shouldUseCreationDateWithoutLastModified() {
    Instant creationDate = Instant.now();
    PullRequest pr = PullRequest.builder()
      .id("42")
      .author("tricia")
      .title("Awesome")
      .description("This pr so awesome")
      .creationDate(creationDate)
      .build();

    IssueReferencingObject ref = mapper.ref(repository, pr, false);
    assertThat(ref.getDate()).isSameAs(creationDate);
  }

  @Test
  void shouldMapTriggeringStateChange() {
    Instant creationDate = Instant.now();
    PullRequest pr = PullRequest.builder()
      .id("42")
      .author("tricia")
      .title("Awesome")
      .description("This pr so awesome")
      .creationDate(creationDate)
      .build();

    IssueReferencingObject ref = mapper.ref(repository, pr, true);
    assertThat(ref.getDate()).isSameAs(creationDate);
    assertThat(ref.isTriggeringStateChange()).isTrue();
  }

}
