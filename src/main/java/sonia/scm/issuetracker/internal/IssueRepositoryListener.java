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
