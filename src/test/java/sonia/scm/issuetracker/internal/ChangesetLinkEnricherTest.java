package sonia.scm.issuetracker.internal;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangesetLinkEnricherTest {

  @Mock
  private LinkAppender.LinkArrayBuilder linkArrayBuilder;

  @Mock
  private LinkAppender linkAppender;

  private ChangesetLinkEnricher enricher;

  @BeforeEach
  void setup() {
    when(linkAppender.arrayBuilder("issues")).thenReturn(linkArrayBuilder);
    IssueTrackerManager issueTrackerManager = new IssueTrackerManager(ImmutableSet.of(ExampleIssueTracker.getRedmine(), ExampleIssueTracker.getJira()));
    enricher = new ChangesetLinkEnricher(issueTrackerManager);
  }

  @Test
  void shouldAppendLinkForSingleIssue() {
    Changeset changeset = new Changeset();
    changeset.setDescription("ABC-123 Blabla");
    LinkEnricherContext ctx = LinkEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

    enricher.enrich(ctx, linkAppender);
    verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
    verify(linkArrayBuilder).build();
  }

  @Test
  void shouldAppendLinksForMultipleIssues() {
    Changeset changeset = new Changeset();
    changeset.setDescription("ABC-123 Blabla DEF-42");
    LinkEnricherContext ctx = LinkEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

    enricher.enrich(ctx, linkAppender);
    verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
    verify(linkArrayBuilder).append("DEF-42", "https://jira.hitchhiker.com/issues/DEF-42");
    verify(linkArrayBuilder).build();
  }

  @Test
  void shouldAppendSingleLinkForDuplicateKey() {
    Changeset changeset = new Changeset();
    changeset.setDescription("ABC-123 Blabla ABC-123 ");
    LinkEnricherContext ctx = LinkEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

    enricher.enrich(ctx, linkAppender);
    verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
    verify(linkArrayBuilder).build();
  }

  @Test
  void shouldAppendLinksForDifferentIssueTrackers() {
    Changeset changeset = new Changeset();
    changeset.setDescription("ABC-123 Blabla #456 ");
    LinkEnricherContext ctx = LinkEnricherContext.of(RepositoryTestData.createHeartOfGold(), changeset);

    enricher.enrich(ctx, linkAppender);
    verify(linkArrayBuilder).append("ABC-123", "https://jira.hitchhiker.com/issues/ABC-123");
    verify(linkArrayBuilder).append("#456", "https://redmine.hitchhiker.com/issues/456");
    verify(linkArrayBuilder).build();
  }
}
