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

    IssueReferencingObject ref = mapper.ref(repository, pr);
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

    IssueReferencingObject ref = mapper.ref(repository, pr);
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

    IssueReferencingObject ref = mapper.ref(repository, pr);
    assertThat(ref.getDate()).isSameAs(creationDate);
  }

}
