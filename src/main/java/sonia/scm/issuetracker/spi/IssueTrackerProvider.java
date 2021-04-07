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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

import java.util.Optional;

/**
 * Creates issue trackers for an external issue tracker system such as jira or redmine.
 * This is the main entry point for plugin developers that want to connect to a new issue tracker system.
 *
 * @since 3.0.0
 */
@ExtensionPoint
public interface IssueTrackerProvider {

  /**
   * Create a new issue tracker for the given repository wrapped in an {@link Optional} or
   * {@link Optional#empty()} if no issue tracker is configured or applicable.
   *
   * @param builder builder to create a default implementation of the issue tracker
   * @param repository target repository
   *
   * @return optional issue tracker
   */
  Optional<IssueTracker> create(IssueTrackerBuilder builder, Repository repository);

}
