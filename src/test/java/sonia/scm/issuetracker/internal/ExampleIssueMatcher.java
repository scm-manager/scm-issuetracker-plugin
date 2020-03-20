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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.issuetracker.IssueMatcher;

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
