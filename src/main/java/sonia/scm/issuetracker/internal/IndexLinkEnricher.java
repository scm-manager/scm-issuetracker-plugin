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

import javax.inject.Inject;
import javax.inject.Provider;
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
