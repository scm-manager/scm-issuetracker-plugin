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

import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Delegates and merges results of multiple {@link IssueTracker}.
 *
 * @since 3.0.0
 */
public final class CompositeIssueTracker implements IssueTracker {

  private final IssueTrackerBuilder builder;
  private final Set<IssueTrackerProvider> providers;

  @Inject
  public CompositeIssueTracker(IssueTrackerBuilder builder, Set<IssueTrackerProvider> providers) {
    this.builder = builder;
    this.providers = providers;
  }

  @Override
  public void process(IssueReferencingObject object) {
    for (IssueTracker tracker : trackers(object)) {
      tracker.process(object);
    }
  }

  @Override
  public Map<String,String> findIssues(IssueReferencingObject object) {
    Map<String,String> issues = new LinkedHashMap<>();
    for (IssueTracker tracker : trackers(object)) {
      Map<String, String> trackerMap = tracker.findIssues(object);
      if (trackerMap != null) {
        issues.putAll(trackerMap);
      }
    }
    return issues;
  }

  private Iterable<IssueTracker> trackers(IssueReferencingObject object) {
    return providers.stream()
      .map(provider -> provider.create(builder, object.getRepository()))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

}
