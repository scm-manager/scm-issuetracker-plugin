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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;

class TemplateCommentRenderer implements ReferenceCommentRenderer, StateChangeCommentRenderer {

  static final String COMMENT_TYP_REFERENCE = "reference";
  static final String COMMENT_TYP_STATECHANGE = "statechange";

  private final TemplateEngineFactory templateEngineFactory;
  private final Set<TemplateModelProvider> modelProviders;
  private final String commentType;
  private final String resourcePathTemplate;

  TemplateCommentRenderer(TemplateEngineFactory templateEngineFactory, Set<TemplateModelProvider> modelProviders, String commentType, String resourcePathTemplate) {
    this.templateEngineFactory = templateEngineFactory;
    this.modelProviders = modelProviders;
    this.commentType = commentType;
    this.resourcePathTemplate = resourcePathTemplate;
  }

  @Override
  public String render(IssueReferencingObject object) throws IOException {
    return render(COMMENT_TYP_REFERENCE, object, null);
  }

  @Override
  public String render(IssueReferencingObject object, String keyWord) throws IOException {
    return render(COMMENT_TYP_STATECHANGE, object, keyWord);
  }

  private String render(String commentType, IssueReferencingObject object, String keyWord) throws IOException {
    Template template = findTemplate(commentType, object);
    Object model = createModel(object, keyWord);
    return execute(template, model);
  }

  private String execute(Template template, Object model) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, model);
    return writer.toString();
  }

  private Object createModel(IssueReferencingObject object, String keyWord) {
    Optional<TemplateModelProvider> provider = modelProviders.stream().filter(p -> p.isSupported(object)).findFirst();
    return provider.map(p -> p.createModel(object, keyWord)).orElse(object);
  }

  private Template findTemplate(String commentType, IssueReferencingObject object) throws IOException {
    String path = MessageFormat.format(resourcePathTemplate, object.getType(), commentType);
    TemplateEngine engine = templateEngineFactory.getEngineByExtension(path);
    return engine.getTemplate(path);
  }

}
