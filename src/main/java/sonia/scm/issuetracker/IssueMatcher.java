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

package sonia.scm.issuetracker;

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The matcher is responsible for finding issue keys in texts.
 *
 * @author Sebastian Sdorra
 */
public interface IssueMatcher {

  /**
   * Return the pattern to match issues.
   *
   * @return pattern to match issues
   */
  Pattern getKeyPattern();

  /**
   * Returns the issue key for the given matcher.
   *
   * @param matcher matcher create from the key pattern
   * @return issue key
   */
  String getKey(Matcher matcher);

}
