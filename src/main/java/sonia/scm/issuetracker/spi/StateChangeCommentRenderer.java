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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.api.IssueReferencingObject;

import java.io.IOException;

/**
 * Renders comments after a state of an issue was changed.
 *
 * @since 3.0.0
 */
public interface StateChangeCommentRenderer {

  /**
   * Creates a comment for a state change.
   *
   * @param object object triggering the state change of the issue
   * @param keyWord keyword that has triggered the state change
   * @return comment that will be added to the issue
   * @throws IOException when the comment could not be rendered
   */
  String render(IssueReferencingObject object, String keyWord) throws IOException;
}
