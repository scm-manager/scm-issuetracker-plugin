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
