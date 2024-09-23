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

package sonia.scm.issuetracker.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangesetLinkEnricherTest {

  @Mock
  private HalAppender.LinkArrayBuilder linkArrayBuilder;

  @Mock
  private HalAppender linkAppender;

  @Mock
  private IssueTracker issueTracker;

  @Mock
  private ChangesetMapper mapper;

  @Mock
  private IssueReferencingObject ref;

  private ChangesetLinkEnricher enricher;

  @BeforeEach
  void setUpArrayBuilder() {
    when(linkAppender.linkArrayBuilder("issues")).thenReturn(linkArrayBuilder);
  }

  @Nested
  class NewApi {

    @BeforeEach
    void setUp() {
      enricher = new ChangesetLinkEnricher(
        new IssueTrackerManager(Collections.emptySet()),
        issueTracker,
        mapper
      );

    }

    @Test
    void shouldAppendIssues() {
      Repository repository = RepositoryTestData.createHeartOfGold();
      Changeset changeset = new Changeset();

      when(mapper.ref(repository, changeset)).thenReturn(ref);
      when(issueTracker.findIssues(ref)).thenReturn(
        ImmutableMap.of("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123")
      );

      HalEnricherContext ctx = HalEnricherContext.of(repository, changeset);

      enricher.enrich(ctx, linkAppender);
      verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
      verify(linkArrayBuilder).build();
    }
  }

  @Nested
  class Legacy {

    @BeforeEach
    void setup() {
      when(mapper.ref(any(), any())).thenReturn(ref);
      when(issueTracker.findIssues(ref)).thenReturn(Collections.emptyMap());

      IssueTrackerManager issueTrackerManager = new IssueTrackerManager(ImmutableSet.of(
        ExampleIssueTracker.getRedmine(), ExampleIssueTracker.getJira())
      );
      enricher = new ChangesetLinkEnricher(issueTrackerManager, issueTracker, mapper);
    }

    @Test
    void shouldAppendLinkForSingleIssue() {
      Changeset changeset = new Changeset();
      changeset.setDescription("ABC-123 Blabla");
      HalEnricherContext ctx = HalEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

      enricher.enrich(ctx, linkAppender);
      verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
      verify(linkArrayBuilder).build();
    }

    @Test
    void shouldAppendLinksForMultipleIssues() {
      Changeset changeset = new Changeset();
      changeset.setDescription("ABC-123 Blabla DEF-42");
      HalEnricherContext ctx = HalEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

      enricher.enrich(ctx, linkAppender);
      verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
      verify(linkArrayBuilder).append("DEF-42", "https://jira.hitchhiker.com/issues/DEF-42");
      verify(linkArrayBuilder).build();
    }

    @Test
    void shouldAppendSingleLinkForDuplicateKey() {
      Changeset changeset = new Changeset();
      changeset.setDescription("ABC-123 Blabla ABC-123 ");
      HalEnricherContext ctx = HalEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

      enricher.enrich(ctx, linkAppender);
      verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
      verify(linkArrayBuilder).build();
    }

    @Test
    void shouldAppendLinksForDifferentIssueTrackers() {
      Changeset changeset = new Changeset();
      changeset.setDescription("ABC-123 Blabla #456 ");
      HalEnricherContext ctx = HalEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

      enricher.enrich(ctx, linkAppender);
      verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
      verify(linkArrayBuilder).append("#456", "https://redmine.hitchhiker.com/issues/456");
      verify(linkArrayBuilder).build();
    }

  }
}
