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

import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.plugin.ExtensionPoint;

/**
 * Creates a model object for comment template rendering from a {@link sonia.scm.issuetracker.api.IssueReferencingObject}.
 * @since 3.0.0
 */
@ExtensionPoint
public interface TemplateModelProvider {

  /**
   * Returns {@code true} if the {@link sonia.scm.issuetracker.api.IssueReferencingObject} is supported.
   *
   * @param object issue referencing object
   * @return {@code true} if supported
   */
  boolean isSupported(IssueReferencingObject object);

  /**
   * Creates a model for template rendering from the given {@link sonia.scm.issuetracker.api.IssueReferencingObject}.
   *
   * @param object issue referencing object
   * @param keyWord key word of state change or null for reference comments
   *
   * @return model for template rendering
   */
  Object createModel(IssueReferencingObject object, String keyWord);

}
