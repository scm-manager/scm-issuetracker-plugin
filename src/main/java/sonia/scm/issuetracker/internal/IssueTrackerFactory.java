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

import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IssueTrackerFactory {

  private final IssueTrackerBuilder builder;
  private final Set<IssueTrackerProvider> providers;

  @Inject
  public IssueTrackerFactory(IssueTrackerBuilder builder, Set<IssueTrackerProvider> providers) {
    this.builder = builder;
    this.providers = providers;
  }

  public Iterable<IssueTracker> trackers(Repository repository) {
    return stream(repository)
      .collect(Collectors.toList());
  }

  public Optional<IssueTracker> tracker(Repository repository, String name) {
    return stream(repository)
      .filter(tracker -> name.equals(tracker.getName()))
      .findAny();
  }

  private Stream<IssueTracker> stream(Repository repository) {
    return providers.stream()
      .map(provider -> provider.create(builder, repository))
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

}
