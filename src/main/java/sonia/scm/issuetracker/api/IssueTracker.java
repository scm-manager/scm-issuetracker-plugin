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

package sonia.scm.issuetracker.api;

import com.google.inject.ImplementedBy;
import sonia.scm.issuetracker.internal.CompositeIssueTracker;

import java.util.Map;

/**
 * Main api to find and process issues.
 *
 * @since 3.0.0
 */
@ImplementedBy(CompositeIssueTracker.class)
public interface IssueTracker {

  /**
   * Process the {@link IssueReferencingObject}.
   * What exactly processing means is up the implementation of the issue tracker.
   * This can be commenting or changing state of the issue.
   *
   * @param object issue referencing object
   */
  void process(IssueReferencingObject object);

  /**
   * Find issues in the {@link IssueReferencingObject}.
   * @param object issue referencing object
   * @return map of issues, key is the issue id and value is a link to issue
   */
  Map<String,String> findIssues(IssueReferencingObject object);
}
