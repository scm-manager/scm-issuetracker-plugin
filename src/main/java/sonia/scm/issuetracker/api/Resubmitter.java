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

import java.io.IOException;

/**
 * Resend comments which could be added to issue in the past.
 *
 * @since 3.1.0
 */
public interface Resubmitter {

  /**
   * Adds the resubmit comment to the issue with the given issue key.
   *
   * @param issueKey issue key
   * @param comment comment to add
   *
   * @throws IOException if the comment could not be added
   */
  void resubmit(String issueKey, String comment) throws IOException;
}
