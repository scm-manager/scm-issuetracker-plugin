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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.issuetracker.internal.resubmit.ResubmitResource;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.HashMap;
import java.util.Map;

@Extension
@Enrich(Index.class)
public class IndexLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public IndexLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Map<String, String> links = new HashMap<>();

    if (Permissions.isResubmitPermitted()) {
      appendResubmitLink(links);
      appendResubmitConfigurationLink(links);
    }

    if (!links.isEmpty()) {
      append(appender, links);
    }
  }

  private void appendResubmitConfigurationLink(Map<String, String> links) {
    String resubmitConfigurationLink = new LinkBuilder(scmPathInfoStore.get().get(), IssueTrackerResource.class, ResubmitResource.class)
      .method("resubmits")
      .parameters()
      .method("getConfiguration")
      .parameters()
      .href();

    links.put("resubmitConfiguration", resubmitConfigurationLink);
  }

  private void appendResubmitLink(Map<String, String> links) {
    String resubmitLink = new LinkBuilder(scmPathInfoStore.get().get(), IssueTrackerResource.class)
      .method("resubmits")
      .parameters()
      .href();

    links.put("resubmit", resubmitLink);
  }

  private void append(HalAppender appender, Map<String, String> links) {
    HalAppender.LinkArrayBuilder builder = appender.linkArrayBuilder("issueTracker");
    for (Map.Entry<String, String> e : links.entrySet()) {
      builder.append(e.getKey(), e.getValue());
    }
    builder.build();
  }

}
