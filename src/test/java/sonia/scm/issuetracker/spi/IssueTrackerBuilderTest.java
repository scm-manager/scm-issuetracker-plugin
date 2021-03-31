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

package sonia.scm.issuetracker.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStoreFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class IssueTrackerBuilderTest {

  @Mock
  private TemplateCommentRendererFactory templateCommentRendererFactory;

  private IssueTrackerBuilder builder;

  @Mock
  private IssueMatcher matcher;

  @Mock
  private IssueLinkFactory issueLinkFactory;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Mock
  private Commentator commentator;

  @Mock
  private ReferenceCommentRenderer referenceCommentRenderer;

  @Mock
  private StateChanger stateChanger;

  @BeforeEach
  void setUpBuilder() {
    builder = new IssueTrackerBuilder(new InMemoryDataStoreFactory(), templateCommentRendererFactory);
  }

  @Test
  void shouldNotAllowEmptyOrNullNames() {
    assertThrows(IllegalArgumentException.class, () -> builder.start(null, matcher, issueLinkFactory));
    assertThrows(IllegalArgumentException.class, () -> builder.start("", matcher, issueLinkFactory));
  }

  @Test
  void shouldNotAllowNullForMatcher() {
    assertThrows(NullPointerException.class, () -> builder.start("hitchhiker", null, issueLinkFactory));
  }

  @Test
  void shouldNotAllowNullForIssueLinkFactory() {
    assertThrows(NullPointerException.class, () -> builder.start("hitchhiker", matcher, null));
  }

  @Nested
  class ReadStage {

    private IssueTrackerBuilder.ReadStage stage;

    @BeforeEach
    void prepareStage() {
      stage = builder.start("hitchhiker", matcher, issueLinkFactory);
    }

    @Test
    void shouldNotAllowNullForRepository() {
      assertThrows(NullPointerException.class, () -> stage.commenting(null, commentator));
    }

    @Test
    void shouldNotAllowNullForCommentator() {
      assertThrows(NullPointerException.class, () -> stage.commenting(repository, null));
    }

  }

  @Nested
  class CommentingStage {

    private IssueTrackerBuilder.CommentingStage stage;

    @BeforeEach
    void prepareStage() {
      stage = builder.start("hitchhiker", matcher, issueLinkFactory)
        .commenting(repository, commentator);
    }

    @Test
    void shouldNotAllowNullAsRenderer() {
      assertThrows(NullPointerException.class, () -> stage.renderer(null));
    }

    @Test
    void shouldNotAllowNullOrEmptyAsTemplatePath() {
      assertThrows(IllegalArgumentException.class, () -> stage.template(null));
      assertThrows(IllegalArgumentException.class, () -> stage.template(""));
    }
  }

  @Nested
  class ChangeStateStage {

    private IssueTrackerBuilder.ChangeStateStage stage;

    @BeforeEach
    void prepareStage() {
      stage = builder.start("hitchhiker", matcher, issueLinkFactory)
        .commenting(repository, commentator)
        .renderer(referenceCommentRenderer);
    }

    @Test
    void shouldNotAllowNullAsStateChanger() {
      assertThrows(NullPointerException.class, () -> stage.stateChanging(null));
    }

  }

  @Nested
  class ChangeStateRendererStage {

    private IssueTrackerBuilder.ChangeStateRenderStage stage;

    @BeforeEach
    void prepareStage() {
      stage = builder.start("hitchhiker", matcher, issueLinkFactory)
        .commenting(repository, commentator)
        .renderer(referenceCommentRenderer)
        .stateChanging(stateChanger);
    }

    @Test
    void shouldNotAllowNullAsRenderer() {
      assertThrows(NullPointerException.class, () -> stage.renderer(null));
    }

    @Test
    void shouldNotAllowNullOrEmptyAsTemplatePath() {
      assertThrows(IllegalArgumentException.class, () -> stage.template(null));
      assertThrows(IllegalArgumentException.class, () -> stage.template(""));
    }

  }

}
