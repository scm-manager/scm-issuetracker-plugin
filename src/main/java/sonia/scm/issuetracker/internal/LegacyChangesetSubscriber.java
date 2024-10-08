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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueRequest;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 *
 * @author Sebastian Sdorra
 */

@Extension
@EagerSingleton
public class LegacyChangesetSubscriber
{

  /**
   * the logger for IssuePostReceiveRepositoryHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(LegacyChangesetSubscriber.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   */
  @Inject
  public LegacyChangesetSubscriber(IssueTrackerManager manager)
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
    Optional<User> committer = getCommitter();
    IssueRequest request = new IssueRequest(repository, changeset, issueKeys, committer);
    try {
      tracker.handleRequest(request);
      tracker.markAsHandled(repository, changeset);
    } catch ( Exception ex ){
      logger.error("error during issue request handling", ex);
    }
  }

  private Optional<User> getCommitter() {
    try {
      return ofNullable(SecurityUtils.getSubject().getPrincipals().oneByType(User.class));
    } catch (Exception e) {
      // reading the logged in user should not let the comment fail
      logger.info("could not read current user from SecurityUtils", e);
      return empty();
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
    Optional<IssueMatcher> matcher = tracker.createMatcher(repository);

    if (matcher.isPresent())
    {
      Iterable<Changeset> changesets = getChangesets(event, repository);

      if (changesets != null)
      {
        Pattern pattern = matcher.get().getKeyPattern();

        for (Changeset c : changesets)
        {
          if (!tracker.isHandled(repository, c))
          {

            List<String> issueKeys = extractIssueKeys(matcher.get(), pattern,
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

    if (event.getContext() != null)
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

    return changesets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final IssueTrackerManager manager;
}
