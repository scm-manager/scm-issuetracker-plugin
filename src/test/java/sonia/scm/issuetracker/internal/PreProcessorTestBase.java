/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.issuetracker.internal;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 * @param <T>
 */
public abstract class PreProcessorTestBase<T>
{

  /**
   * Method description
   *
   *
   * @param description
   *
   * @return
   */
  protected abstract T createItem(String description);

  /**
   * Method description
   *
   *
   * @param manager
   * @param repository
   *
   * @return
   */
  protected abstract AbstractPreProcessor<T> createPreProcessor(
    IssueTrackerManager manager, Repository repository);

  /**
   * Method description
   *
   */
  @Test
  public void testJiraProcess()
  {
    AbstractPreProcessor<T> app = createPreProcessor(new JiraIssueMatcher());
    T item = createItem("fix issue SCM-123");

    app.process(item);
    assertEquals(
      "fix issue <a href=\"https://jira.atlassian.com/issue/SCM-123\">SCM-123</a>",
      app.getDescription(item));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSimpleProcess()
  {
    AbstractPreProcessor<T> app = createPreProcessor(new SimpleIssueMatcher());
    T item = createItem("A should be B");

    app.process(item);
    assertEquals("B should be B", app.getDescription(item));
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param matcher
   *
   * @return
   */
  private IssueTrackerManager createManager(Repository repository,
    IssueMatcher matcher)
  {
    IssueTracker tracker = mock(IssueTracker.class);

    when(tracker.getName()).thenReturn("tracker1");
    when(tracker.createMatcher(repository)).thenReturn(matcher);

    Set<IssueTracker> trackers = ImmutableSet.of(tracker);

    return new IssueTrackerManager(trackers);
  }

  /**
   * Method description
   *
   *
   * @param matcher
   *
   * @return
   */
  private AbstractPreProcessor<T> createPreProcessor(IssueMatcher matcher)
  {
    Repository r = RepositoryTestData.createHappyVerticalPeopleTransporter();
    IssueTrackerManager manager = createManager(r, matcher);

    return createPreProcessor(manager, r);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/10/25
   * @author         Enter your name here...
   */
  private static class SimpleIssueMatcher implements IssueMatcher
  {

    /**
     * Method description
     *
     *
     * @param matcher
     *
     * @return
     */
    @Override
    public String getKey(Matcher matcher)
    {
      return matcher.group();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Pattern getKeyPattern()
    {
      return Pattern.compile("(A)");
    }

    /**
     * Method description
     *
     *
     * @param matcher
     *
     * @return
     */
    @Override
    public String getReplacement(Matcher matcher)
    {
      return "B";
    }
  }
}
