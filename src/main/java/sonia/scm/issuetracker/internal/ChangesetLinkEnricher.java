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

import com.google.common.base.Strings;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
@Enrich(Changeset.class)
public class ChangesetLinkEnricher implements HalEnricher {

  private final IssueTrackerManager legacyIssueTrackers;
  private final IssueTracker issueTracker;
  private final ChangesetMapper mapper;

  @Inject
  public ChangesetLinkEnricher(IssueTrackerManager legacyIssueTrackers, IssueTracker issueTracker, ChangesetMapper mapper) {
    this.legacyIssueTrackers = legacyIssueTrackers;
    this.issueTracker = issueTracker;
    this.mapper = mapper;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    Changeset changeset = context.oneRequireByType(Changeset.class);

    HalAppender.LinkArrayBuilder halArrayBuilder = appender.linkArrayBuilder("issues");

    findIssues(repository, changeset).forEach(halArrayBuilder::append);
    findIssuesFromLegacyTrackers(repository, changeset).forEach(halArrayBuilder::append);

    halArrayBuilder.build();
  }

  private Map<String, String> findIssuesFromLegacyTrackers(Repository repository, Changeset changeset) {
    Map<String, String> issues = new LinkedHashMap<>();

    for (sonia.scm.issuetracker.IssueTracker legacyTracker : legacyIssueTrackers.getIssueTrackers()) {
      Optional<IssueMatcher> issueMatcher = legacyTracker.createMatcher(repository);
      if (!issueMatcher.isPresent()) continue;

      Pattern pattern = issueMatcher.get().getKeyPattern();
      Matcher matcher = pattern.matcher(Strings.nullToEmpty(changeset.getDescription()));

      Optional<IssueLinkFactory> issueLinkFactory = legacyTracker.createLinkFactory(repository);

      if (issueLinkFactory.isPresent()) {
        while (matcher.find()) {
          String key = issueMatcher.get().getKey(matcher);
          String link = issueLinkFactory.get().createLink(key);
          issues.put(key, link);
        }
      }
    }
    return issues;
  }

  private Map<String, String> findIssues(Repository repository, Changeset changeset) {
    IssueReferencingObject ref = mapper.ref(repository, changeset);
    return issueTracker.findIssues(ref);
  }
}
