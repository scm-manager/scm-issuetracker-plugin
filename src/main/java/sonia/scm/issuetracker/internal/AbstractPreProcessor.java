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

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.PreProcessor;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractPreProcessor<T> implements PreProcessor<T>
{

  /**
   * the logger for IssueChangesetPreProcessor
   */
  private static final Logger logger =
    LoggerFactory.getLogger(IssueChangesetPreProcessor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param manager
   */
  public AbstractPreProcessor(Repository repository,
    IssueTrackerManager manager)
  {
    this.repository = repository;
    this.manager = manager;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  protected abstract String getDescription(T item);

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   * @param description
   */
  protected abstract void setDescription(T item, String description);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  public void process(T item)
  {
    for (IssueTracker tracker : manager.getIssueTrackers())
    {
      process(tracker, item);
    }
  }

  /**
   * Method description
   *
   *
   * @param tracker
   * @param c
   * @param item
   */
  private void process(IssueTracker tracker, T item)
  {
    IssueMatcher matcher = tracker.createMatcher(repository);

    if (matcher != null)
    {
      String description = getDescription(item);

      description = replaceIssueKeys(matcher, description);
      setDescription(item, description);
    }
    else
    {
      logger.debug("could not create issue matcher for {}", tracker.getName());
    }
  }

  /**
   * Method description
   *
   *
   * @param matcher
   * @param description
   *
   * @return
   */
  private String replaceIssueKeys(IssueMatcher matcher, String description)
  {
    if (!Strings.isNullOrEmpty(description))
    {
      StringBuffer buffer = new StringBuffer();

      Pattern p = matcher.getKeyPattern();
      Matcher m = p.matcher(description);

      while (m.find())
      {
        String r = matcher.getReplacement(m);

        if (r != null)
        {
          m.appendReplacement(buffer, r);
        }
      }

      description = m.appendTail(buffer).toString();
    }

    return description;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final IssueTrackerManager manager;

  /** Field description */
  private final Repository repository;
}
