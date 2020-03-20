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

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
public class IssueRepositoryListener
{

  /**
   * the logger for IssueRepositoryListener
   */
  private static final Logger logger =
    LoggerFactory.getLogger(IssueRepositoryListener.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   */
  @Inject
  public IssueRepositoryListener(IssueTrackerManager manager)
  {
    this.manager = manager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void handleRepositoryEvent(RepositoryEvent event)
  {
    if (event.getEventType() == HandlerEventType.DELETE)
    {
      Repository repository = event.getItem();

      if (repository != null)
      {
        logger.debug("handle repository delete event");

        for (IssueTracker tracker : manager.getIssueTrackers())
        {
          removeHandledMarks(tracker, repository);
        }
      }
      else
      {
        logger.warn("received repository event without repository");
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param tracker
   * @param repository
   */
  private void removeHandledMarks(IssueTracker tracker, Repository repository)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace(
        "remove handled marks of repository {} from issue tracker {}",
        repository.getName(), tracker.getName());
    }

    try
    {
      tracker.removeHandledMarks(repository);
    }
    catch (Exception ex)
    {
      logger.error("could not remove handled marks", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final IssueTrackerManager manager;
}
