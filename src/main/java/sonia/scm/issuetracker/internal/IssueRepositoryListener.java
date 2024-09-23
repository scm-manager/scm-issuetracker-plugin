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
