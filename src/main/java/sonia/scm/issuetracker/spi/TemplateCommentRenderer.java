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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Person;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.user.User;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TemplateCommentRenderer implements ReferenceCommentRenderer, StateChangeCommentRenderer {

  private final TemplateEngineFactory templateEngineFactory;
  private final String resourcePathTemplate;

  TemplateCommentRenderer(TemplateEngineFactory templateEngineFactory, String resourcePathTemplate) {
    this.templateEngineFactory = templateEngineFactory;
    this.resourcePathTemplate = resourcePathTemplate;
  }

  @Override
  public String render(IssueReferencingObject object) throws IOException {
    Map<String, Object> model = model(object).build();
    return render(object.getType(), model);
  }

  @Override
  public String render(IssueReferencingObject object, String keyWord) throws IOException {
    Map<String, Object> model = model(object).put("keyWord", keyWord).build();
    return render(object.getType(), model);
  }

  private String render(String type, Map<String, Object> model) throws IOException {
    Template template = findTemplate(type);
    return execute(template, model);
  }

  private String execute(Template template, Map<String, Object> model) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, model);
    return writer.toString();
  }

  private ImmutableMap.Builder<String, Object> model(IssueReferencingObject object) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    put(builder, "repository", object.getRepository());
    put(builder, "type", object.getType());
    put(builder, "id", object.getId());
    put(builder, "author", object.getAuthor());
    put(builder, "date", object.getDate());
    put(builder, "content", object.getContent());
    put(builder, "link", object.getLink());
    put(builder, "origin", object.getOrigin());
    put(builder, "principal", findPrincipal());
    return builder;
  }

  private void put(ImmutableMap.Builder<String, Object> builder, String key, List<Content> content) {
    Map<String,String> map = new LinkedHashMap<>();
    content.forEach(c -> map.put(c.getType(), c.getValue()));
    put(builder, key, map);
  }

  private void put(ImmutableMap.Builder<String, Object> builder, String key, Object value) {
    if (value != null) {
      builder.put(key, value);
    }
  }

  private Person findPrincipal() {
    PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
    if (principals != null) {
      User user = principals.oneByType(User.class);
      if (user != null) {
        return new Person(
          MoreObjects.firstNonNull(user.getDisplayName(), user.getName()),
          user.getMail()
        );
      }
    }
    return null;
  }

  private Template findTemplate(String type) throws IOException {
    String path = MessageFormat.format(resourcePathTemplate, type);
    return templateEngineFactory.getEngineByExtension(path).getTemplate(path);
  }

}
