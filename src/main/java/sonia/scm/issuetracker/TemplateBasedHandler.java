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
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringWriter;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class TemplateBasedHandler
{

  /** Field description */
  private static final String ENV_AUTOCLOSEWORD = "autoCloseWord";

  /** Field description */
  private static final String ENV_CHANGESET = "changeset";

  /** Field description */
  private static final String ENV_DIFFRESTURL = "diffRestUrl";

  /** Field description */
  private static final String ENV_DIFFURL = "diffUrl";

  /** Field description */
  private static final String ENV_REPOSITORY = "repository";

  /** Field description */
  private static final String ENV_REPOSITORYURL = "repositoryUrl";

  /**
   * the logger for TemplateBasedHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(TemplateBasedHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param templateEngineFactory
   * @param linkHandler
   */
  protected TemplateBasedHandler(TemplateEngineFactory templateEngineFactory,
    LinkHandler linkHandler)
  {
    this.templateEngineFactory = templateEngineFactory;
    this.linkHandler = linkHandler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param engine
   *
   * @return
   *
   * @throws IOException
   */
  protected abstract Template loadTemplate(TemplateEngine engine)
    throws IOException;

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected String createComment(IssueRequest request)
  {
    return createComment(request, null);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param autoCloseWord
   *
   * @return
   */
  protected String createComment(IssueRequest request, String autoCloseWord)
  {
    Object model = createModel(request, autoCloseWord);

    return createComment(model);
  }

  /**
   * Method description
   *
   *
   * @param model
   *
   * @return
   */
  protected String createComment(Object model)
  {
    String comment = null;
    TemplateEngine engine = getTemplateEngine(templateEngineFactory);

    if (engine != null)
    {
      try
      {
        Template template = loadTemplate(engine);

        if ((template != null) && (model != null))
        {
          StringWriter writer = new StringWriter();

          template.execute(writer, model);
          comment = writer.toString();
        }
        else
        {
          logger.warn("template or model is not available");
        }
      }
      catch (IOException ex)
      {
        logger.error("could not load/render template", ex);
      }
    }
    else
    {
      logger.warn("could not create template engine");
    }

    return comment;
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param autoCloseWord
   *
   * @return
   */
  protected Object createModel(IssueRequest request, String autoCloseWord)
  {
    Map<String, Object> model = Maps.newHashMap();

    model.put(ENV_REPOSITORY, request.getRepository());
    model.put(ENV_CHANGESET, request.getChangeset());
    model.put(ENV_AUTOCLOSEWORD, Strings.nullToEmpty(autoCloseWord));
    model.put(ENV_DIFFURL, linkHandler.getDiffUrl(request));
    model.put(ENV_DIFFRESTURL, linkHandler.getDiffRestUrl(request));
    model.put(ENV_REPOSITORYURL, linkHandler.getRepositoryUrl(request));

    return model;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param factory
   *
   * @return
   */
  protected TemplateEngine getTemplateEngine(TemplateEngineFactory factory)
  {
    return factory.getDefaultEngine();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected final LinkHandler linkHandler;

  /** Field description */
  private final TemplateEngineFactory templateEngineFactory;
}
