/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.issuetracker;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

/**
 *
 * @author Sebastian Sdorra
 */
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
  public abstract IssueMatcher createMatcher(Repository repository);

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   */
  public abstract void markAsHandled(Repository repository,
    Changeset changeset);

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
    AutoCloseHandler autoCloseHandler = null;

    try
    {
      autoCloseHandler = getAutoCloseHandler(request);

      if (autoCloseHandler != null)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("check changeset {} for auto-close",
            request.getChangeset().getId());
        }

        String autoCloseWord = searchAutoCloseWord(autoCloseHandler, request);

        if (!Strings.isNullOrEmpty(autoCloseWord))
        {
          closeIssues(autoCloseHandler, request, autoCloseWord);
        }
        else
        {
          logger.debug("no auto close word available on changeset {}",
            request.getChangeset().getId());

          commentIssues(request);
        }
      }
      else
      {
        logger.debug("auto close is disabled or not supported by {}", name);
        commentIssues(request);
      }
    }
    finally
    {
      Closeables.closeQuietly(autoCloseHandler);
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
  protected AutoCloseHandler getAutoCloseHandler(IssueRequest request)
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
   * @param ach
   * @param request
   * @param autoCloseWord
   */
  private void closeIssues(AutoCloseHandler ach, IssueRequest request,
    String autoCloseWord)
  {
    for (String issueKey : request.getIssueKeys())
    {
      logger.info("close issue {}, because of auto close word {}", issueKey,
        autoCloseWord);

      try
      {
        ach.closeIssue(issueKey, autoCloseWord);
      }
      catch (Exception ex)
      {
        logger.error("could not close issue", ex);
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
    CommentHandler commentHandler = null;

    try
    {
      commentHandler = getCommentHandler(request);

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
    }
    finally
    {
      Closeables.closeQuietly(commentHandler);
    }
  }

  /**
   * Method description
   *
   *
   * @param changeset
   *
   * @param ach
   * @param request
   *
   * @return
   */
  private String searchAutoCloseWord(AutoCloseHandler ach, IssueRequest request)
  {
    String description = request.getChangeset().getDescription();
    String autoCloseWord = null;
    String[] words = description.split("\\s");

    for (String w : words)
    {
      for (String acw : ach.getAutoCloseWords())
      {
        acw = acw.trim();

        if (w.equalsIgnoreCase(acw))
        {
          autoCloseWord = w;

          break;
        }
      }

      if (autoCloseWord != null)
      {
        break;
      }
    }

    return autoCloseWord;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String name;
}
