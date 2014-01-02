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

import sonia.scm.issuetracker.IssueRequest;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */

@Extension
@EagerSingleton
public class IssuePostReceiveRepositoryHook
{

  /**
   * the logger for IssuePostReceiveRepositoryHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(IssuePostReceiveRepositoryHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   */
  @Inject
  public IssuePostReceiveRepositoryHook(IssueTrackerManager manager)
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
  public void handleEvent(PostReceiveRepositoryHookEvent event)
  {
    if (event != null)
    {
      Repository repository = event.getRepository();

      if (repository != null)
      {
        for (IssueTracker tracker : manager.getIssueTrackers())
        {
          handleEvent(event, repository, tracker);
        }
      }
      else
      {
        logger.warn("received event without repository");
      }
    }
    else
    {
      logger.warn("received null event");
    }
  }

  /**
   * Method description
   *
   *
   * @param matcher
   * @param p
   * @param description
   *
   * @return
   */
  private List<String> extractIssueKeys(IssueMatcher matcher, Pattern p,
    String description)
  {
    List<String> keys = Lists.newArrayList();

    if (!Strings.isNullOrEmpty(description))
    {
      Matcher m = p.matcher(description);

      while (m.find())
      {
        keys.add(matcher.getKey(m));
      }
    }

    return keys;
  }

  /**
   * Method description
   *
   *
   * @param tracker
   * @param repository
   * @param changeset
   * @param issueKeys
   */
  private void handleChangeset(IssueTracker tracker, Repository repository,
    Changeset changeset, List<String> issueKeys) 
  {
    IssueRequest request = new IssueRequest(repository, changeset, issueKeys);
    try {
      tracker.handleRequest(request);
      tracker.markAsHandled(repository, changeset);
    } catch ( Exception ex ){
      logger.error("error during issue request handling", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param event
   * @param repository
   * @param tracker
   */
  private void handleEvent(PostReceiveRepositoryHookEvent event,
    Repository repository, IssueTracker tracker)
  {
    IssueMatcher matcher = tracker.createMatcher(repository);

    if (matcher != null)
    {
      Iterable<Changeset> changesets = getChangesets(event, repository);

      if (changesets != null)
      {
        Pattern pattern = matcher.getKeyPattern();

        for (Changeset c : changesets)
        {
          if (!tracker.isHandled(repository, c))
          {

            List<String> issueKeys = extractIssueKeys(matcher, pattern,
                                       c.getDescription());

            if (!issueKeys.isEmpty())
            {
              handleChangeset(tracker, repository, c, issueKeys);
            }
            else
            {
              logger.trace("no issue keys found for changeset {} at {}",
                c.getId(), repository.getName());
            }
          }
          else
          {
            logger.debug("{} of repsoitory {} is already handled", c.getId(),
              repository.getName());
          }
        }
      }
      else
      {
        logger.warn("event without changesets");
      }
    }
    else
    {
      logger.debug("could not create issue matcher for tracker {}",
        tracker.getName());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   * @param repository
   *
   * @return
   */
  private Iterable<Changeset> getChangesets(
    PostReceiveRepositoryHookEvent event, Repository repository)
  {
    Iterable<Changeset> changesets = null;

    if (event.isContextAvailable())
    {
      HookContext context = event.getContext();

      if (context.isFeatureSupported(HookFeature.CHANGESET_PROVIDER))
      {
        //J-
        changesets = context.getChangesetProvider()
                            .setDisablePreProcessors(true)
                            .getChangesets();
        //J+

      }
      else
      {
        logger.debug("{} does not support changeset provider",
          repository.getType());
      }
    }
    else
    {
      logger.debug("{} has no hook context support", repository.getType());
    }

    if (changesets == null)
    {
      logger.debug("fall back to normal event getChangesets");
      changesets = event.getChangesets();
    }

    return changesets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final IssueTrackerManager manager;
}
