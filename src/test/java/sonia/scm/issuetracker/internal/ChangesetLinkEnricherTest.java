/**
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
package sonia.scm.issuetracker.internal;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangesetLinkEnricherTest {

  @Mock
  private HalAppender.LinkArrayBuilder linkArrayBuilder;

  @Mock
  private HalAppender linkAppender;

  private ChangesetLinkEnricher enricher;

  @BeforeEach
  void setup() {
    when(linkAppender.linkArrayBuilder("issues")).thenReturn(linkArrayBuilder);
    IssueTrackerManager issueTrackerManager = new IssueTrackerManager(ImmutableSet.of(ExampleIssueTracker.getRedmine(), ExampleIssueTracker.getJira()));
    enricher = new ChangesetLinkEnricher(issueTrackerManager);
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
