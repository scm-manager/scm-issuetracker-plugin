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

package sonia.scm.issuetracker.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.internal.resubmit.ResubmitQueue;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryDataStoreFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class IssueTrackerBuilderTest {

  @Mock
  private ResubmitQueue resubmitQueue;

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
    builder = new IssueTrackerBuilder(new InMemoryDataStoreFactory(), resubmitQueue, templateCommentRendererFactory);
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
