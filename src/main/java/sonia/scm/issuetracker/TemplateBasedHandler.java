/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.issuetracker;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringWriter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class TemplateBasedHandler {

  private static final String ENV_CHANGESET = "changeset";
  private static final String ENV_DIFFURL = "diffUrl";
  private static final String ENV_AUTHOR = "author";
  private static final String ENV_COMMITTER = "committer";
  private static final String ENV_KEYWORD = "keyword";
  private static final String ENV_REPOSITORY = "repository";
  private static final String ENV_REPOSITORYURL = "repositoryUrl";
  private static final String ENV_BRANCHES = "branches";
  private static final String ENV_BOOKMARKS = "bookmarks";
  private static final String ENV_DESCRIPTION_LINE = "descriptionLine";

  private static final String UNIX_LINE_SEPARATOR = "\n";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator", UNIX_LINE_SEPARATOR);

  private static final Logger logger = LoggerFactory.getLogger(TemplateBasedHandler.class);

  protected final LinkHandler linkHandler;
  private final TemplateEngineFactory templateEngineFactory;

  protected TemplateBasedHandler(TemplateEngineFactory templateEngineFactory,
                                 LinkHandler linkHandler) {
    this.templateEngineFactory = templateEngineFactory;
    this.linkHandler = linkHandler;
  }


  protected abstract Template loadTemplate(TemplateEngine engine) throws IOException;

  protected String createComment(IssueRequest request) {
    return createComment(request, null);
  }

  protected String createComment(IssueRequest request, String keyword) {
    Object model = createModel(request, keyword);

    return createComment(model);
  }

  protected String createComment(Object model) {
    String comment = null;
    TemplateEngine engine = getTemplateEngine(templateEngineFactory);

    if (engine != null) {
      try {
        Template template = loadTemplate(engine);

        if ((template != null) && (model != null)) {
          StringWriter writer = new StringWriter();

          template.execute(writer, model);
          comment = writer.toString();
        } else {
          logger.warn("template or model is not available");
        }
      } catch (IOException ex) {
        logger.error("could not load/render template", ex);
      }
    } else {
      logger.warn("could not create template engine");
    }

    return comment;
  }

  protected Map<String, Object> createModel(IssueRequest request, String keyword) {
    Map<String, Object> model = Maps.newHashMap();

    String author = request.getChangeset().getAuthor().getName();
    model.put(ENV_AUTHOR, author);
    Optional<String> committer = request.getCommitter().map(User::getDisplayName);
    if (committer.isPresent() && !author.equals(committer.get())) {
      model.put(ENV_COMMITTER, committer.get());
    }

    model.put(ENV_REPOSITORY, request.getRepository());
    model.put(ENV_CHANGESET, request.getChangeset());
    model.put(ENV_KEYWORD, Strings.nullToEmpty(keyword));
    model.put(ENV_DIFFURL, linkHandler.getDiffUrl(request));
    model.put(ENV_REPOSITORYURL, linkHandler.getRepositoryUrl(request));
    model.put(ENV_DESCRIPTION_LINE, splitIntoLines(request.getChangeset()));
    model.put(ENV_BRANCHES, request.getChangeset().getBranches()); // TODO:  Mercurial has empty branches for "default" ...
    model.put(ENV_BOOKMARKS, request.getChangeset().getProperty("hg.bookmarks"));

    return model;
  }

  protected TemplateEngine getTemplateEngine(TemplateEngineFactory factory) {
    return factory.getDefaultEngine();
  }

  private List<String> splitIntoLines(Changeset changeset) {
    List<String> lines = splitDescriptionWith(changeset, getSystemLineSeparator());
    if (descriptionMayHaveOtherLineSeparatorThanConfigured(lines)) {
      return splitDescriptionWith(changeset, UNIX_LINE_SEPARATOR);
    }
    return lines;
  }

  private List<String> splitDescriptionWith(Changeset changeset, String separator) {
    return Arrays.asList(changeset.getDescription().split(separator));
  }

  private boolean descriptionMayHaveOtherLineSeparatorThanConfigured(List<String> lines) {
    return lines.size() == 1 && !UNIX_LINE_SEPARATOR.equals(getSystemLineSeparator());
  }

  String getSystemLineSeparator() {
    return LINE_SEPARATOR;
  }
}
