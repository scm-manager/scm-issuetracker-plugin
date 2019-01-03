package sonia.scm.issuetracker.internal;

import com.google.common.base.Strings;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkEnricher;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
@Enrich(Changeset.class)
public class ChangesetLinkEnricher implements LinkEnricher {

  private IssueTrackerManager issueTrackerManager;

  @Inject
  public ChangesetLinkEnricher(IssueTrackerManager issueTrackerManager) {
    this.issueTrackerManager = issueTrackerManager;
  }

  @Override
  public void enrich(LinkEnricherContext context, LinkAppender appender) {

    Repository repository = context.oneRequireByType(Repository.class);
    Changeset changeset = context.oneRequireByType(Changeset.class);

    Map<String, String> map = createIssueLinkMap(repository, changeset);

    if (!map.isEmpty()) {
      LinkAppender.LinkArrayBuilder linkArrayBuilder = appender.arrayBuilder("issues");
      map.forEach(linkArrayBuilder::append);
      linkArrayBuilder.build();
    }
  }

  private Map<String, String> createIssueLinkMap(Repository repository, Changeset changeset) {
    Map<String, String> map = new LinkedHashMap<>();

    for (IssueTracker issueTracker : issueTrackerManager.getIssueTrackers()) {
      Optional<IssueMatcher> issueMatcher = issueTracker.createMatcher(repository);

      if (!issueMatcher.isPresent()) continue;

      Pattern pattern = issueMatcher.get().getKeyPattern();
      Matcher matcher = pattern.matcher(Strings.nullToEmpty(changeset.getDescription()));

      Optional<IssueLinkFactory> issueLinkFactory = issueTracker.createLinkFactory(repository);

      if (issueLinkFactory.isPresent()) {
        while (matcher.find()) {
          String key = issueMatcher.get().getKey(matcher);
          String link = issueLinkFactory.get().createLink(key);
          map.put(key, link);
        }
      }
    }

    return map;
  }
}

