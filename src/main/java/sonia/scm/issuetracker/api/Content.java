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

package sonia.scm.issuetracker.api;

import lombok.Value;

/**
 * Represents single content of an {@link IssueReferencingObject},
 * e.g. 'description' for changesets, 'title' or 'description' for pull requests.
 * <br>
 * The {@link #getType()} will give you the type (e.g. 'description'), {@link #getValue()}
 * the actual value.
 *
 * @since 3.0.0
 */
@Value
public class Content {
  /**
   * Type of content, e.g. 'title'.
   */
  String type;
  /**
   * Value of content, e.g. the actual title of a pull request.
   */
  String value;
}
