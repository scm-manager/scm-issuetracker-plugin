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

import com.google.common.base.Strings;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.Repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IssueLinkEnricherUtils {
  private IssueLinkEnricherUtils() {}

  static void enrich(IssueTrackerManager issueTrackerManager, Repository repository, HalAppender appender, String text) {
    Map<String, String> map = new LinkedHashMap<>();

    for (IssueTracker issueTracker : issueTrackerManager.getIssueTrackers()) {
      Optional<IssueMatcher> issueMatcher = issueTracker.createMatcher(repository);

      if (!issueMatcher.isPresent()) continue;

      Pattern pattern = issueMatcher.get().getKeyPattern();
      Matcher matcher = pattern.matcher(Strings.nullToEmpty(text));

      Optional<IssueLinkFactory> issueLinkFactory = issueTracker.createLinkFactory(repository);

      if (issueLinkFactory.isPresent()) {
        while (matcher.find()) {
          String key = issueMatcher.get().getKey(matcher);
          String link = issueLinkFactory.get().createLink(key);
          map.put(key, link);
        }
      }
    }

    if (!map.isEmpty()) {
      HalAppender.LinkArrayBuilder halArrayBuilder = appender.linkArrayBuilder("issues");
      map.forEach(halArrayBuilder::append);
      halArrayBuilder.build();
    }
  }
}