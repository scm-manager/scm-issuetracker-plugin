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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Map;

class TemplateCommentRenderer implements ReferenceCommentRenderer, StateChangeCommentRenderer {

  private final TemplateEngineFactory templateEngineFactory;
  private final ObjectMapper mapper;
  private final String resourcePathTemplate;

  TemplateCommentRenderer(TemplateEngineFactory templateEngineFactory, ObjectMapper mapper, String resourcePathTemplate) {
    this.templateEngineFactory = templateEngineFactory;
    this.mapper = mapper;
    this.resourcePathTemplate = resourcePathTemplate;
  }

  @Override
  public String render(IssueReferencingObject object) throws IOException {
    return render(object.getType(), object);
  }

  @Override
  public String render(IssueReferencingObject object, String keyWord) throws IOException {
    Object model = createModelWithKeyWord(object, keyWord);
    return render(object.getType(), model);
  }

  private String render(String type, Object model) throws IOException {
    Template template = findTemplate(type);
    return execute(template, model);
  }

  private String execute(Template template, Object model) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, model);
    return writer.toString();
  }

  private Object createModelWithKeyWord(IssueReferencingObject object, String keyWord) {
    Map<?,?> refMap = mapper.convertValue(object, Map.class);
    return ImmutableMap.builder()
      .putAll(refMap)
      .put("keyWord", keyWord)
      .build();
  }

  private Template findTemplate(String type) throws IOException {
    String path = MessageFormat.format(resourcePathTemplate, type);
    return templateEngineFactory.getEngineByExtension(path).getTemplate(path);
  }

}
