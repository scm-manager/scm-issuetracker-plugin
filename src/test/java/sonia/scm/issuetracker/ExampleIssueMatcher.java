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

//~--- non-JDK imports --------------------------------------------------------

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public class ExampleIssueMatcher implements IssueMatcher
{

  private static final Pattern JIRA_KEY_PATTERN = Pattern.compile("\\b([A-Z]+-\\d+)");
  private static final Pattern REDMINE_KEY_PATTERN = Pattern.compile("\\B(#\\d+)");

  private Pattern pattern;


  public ExampleIssueMatcher(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public String getKey(Matcher matcher)
  {
    return matcher.group();
  }


  @Override
  public Pattern getKeyPattern()
  {
    return pattern;
  }

  public static ExampleIssueMatcher createJira() {
    return new ExampleIssueMatcher(JIRA_KEY_PATTERN);
  }

  public static ExampleIssueMatcher createRedmine() {
    return new ExampleIssueMatcher(REDMINE_KEY_PATTERN);
  }
}
