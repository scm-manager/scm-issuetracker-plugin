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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Sebastian Sdorra
 * @deprecated use {@link sonia.scm.issuetracker.spi.IssueTrackerProvider} instead.
 */
@Deprecated
@ExtensionPoint(multi = true)
public abstract class IssueTracker
{

  /**
   * the logger for IssueTracker
   */
  private static final Logger logger =
    LoggerFactory.getLogger(IssueTracker.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public IssueTracker(String name)
  {
    this.name = name;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public abstract Optional<IssueMatcher> createMatcher(Repository repository);

  public abstract Optional<IssueLinkFactory> createLinkFactory(Repository repository);

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   */
  public abstract void markAsHandled(Repository repository,
    Changeset changeset);

  /**
   * Method description
   *
   *
   * @param repository
   */
  public abstract void removeHandledMarks(Repository repository);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   *
   * @return
   */
  public abstract boolean isHandled(Repository repository, Changeset changeset);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   */
  public void handleRequest(IssueRequest request)
  {
    try(ChangeStateHandler changeStateHandler = this.getChangeStateHandler(request))
    {

      if (changeStateHandler != null)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("check changeset {} for auto-close",
            request.getChangeset().getId());
        }

        String keyword = searchKeywords(changeStateHandler, request);

        if (!Strings.isNullOrEmpty(keyword))
        {
          closeIssues(changeStateHandler, request, keyword);
        }
        else
        {
          logger.debug("no keyword available on changeset {}",
            request.getChangeset().getId());

          commentIssues(request);
        }
      }
      else
      {
        logger.debug("change state is disabled or not supported by {}", name);
        commentIssues(request);
      }
    } catch (IOException e) {
        logger.error("Error on handling Issue Request",e);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected ChangeStateHandler getChangeStateHandler(IssueRequest request)
  {
    return null;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected CommentHandler getCommentHandler(IssueRequest request)
  {
    return null;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param csh
   * @param request
   * @param keyword
   */
  private void closeIssues(ChangeStateHandler csh, IssueRequest request,
    String keyword)
  {
    for (String issueKey : request.getIssueKeys())
    {
      logger.info("change state of issue {}, because of keyword {}", issueKey,
        keyword);

      try
      {
        csh.changeState(issueKey, keyword);
      }
      catch (Exception ex)
      {
        logger.error("could not change state of issue", ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   */
  private void commentIssues(IssueRequest request)
  {
    try (CommentHandler commentHandler = this.getCommentHandler(request))
    {

      if (commentHandler != null)
      {
        for (String issueKey : request.getIssueKeys())
        {
          logger.info("comment issue {}", issueKey);

          try
          {
            commentHandler.commentIssue(issueKey);
          }
          catch (Exception ex)
          {
            logger.error("could not comment issue", ex);
          }
        }
      }
      else
      {
        logger.debug("comments are disabled or not supported by {}", name);
      }
    } catch (IOException e) {
        logger.error("Error on commenting issue", e);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param ach
   * @param request
   *
   * @return
   */
  private String searchKeywords(ChangeStateHandler ach, IssueRequest request)
  {
    String description = request.getChangeset().getDescription();
    String keyword = null;
    String[] words = description.split("\\s");

    for (String w : words)
    {
      for (String kw : ach.getKeywords())
      {
        kw = kw.trim();

        if (w.equalsIgnoreCase(kw))
        {
          keyword = w;

          break;
        }
      }

      if (keyword != null)
      {
        break;
      }
    }

    return keyword;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String name;
}
