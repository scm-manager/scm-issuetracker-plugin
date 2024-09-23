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

import com.cloudogu.scm.review.comment.service.Reply;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.issuetracker.IssueReferencingObjects;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestReplyLinkEnricherTest {

  @Mock
  private HalAppender.LinkArrayBuilder linkArrayBuilder;

  @Mock
  private HalAppender linkAppender;

  @Mock
  private IssueTracker issueTracker;

  @Mock
  private PullRequestCommentMapper mapper;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final Reply reply = new Reply();
  private PullRequestReplyLinkEnricher enricher;

  @BeforeEach
  void setup() {
    when(linkAppender.linkArrayBuilder("issues")).thenReturn(linkArrayBuilder);
    enricher = new PullRequestReplyLinkEnricher(issueTracker, mapper);
  }

  @Test
  void shouldAppendLinks() {
    IssueReferencingObject ref = IssueReferencingObjects.ref("reply", "9000");
    when(mapper.ref(repository, reply)).thenReturn(ref);
    when(issueTracker.findIssues(ref)).thenReturn(ImmutableMap.of(
      "CDE-456", "https://jira.hitchhiker.com/issues/CDE-456"
    ));

    HalEnricherContext ctx = HalEnricherContext.of(repository, reply);

    enricher.enrich(ctx, linkAppender);
    verify(linkArrayBuilder).append("CDE-456", "https://jira.hitchhiker.com/issues/CDE-456");
    verify(linkArrayBuilder).build();
  }
}
