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

package sonia.scm.issuetracker.spi;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Person;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
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
    put(builder, "contributors", object.getContributors());
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

    TemplateEngine engine = templateEngineFactory.getEngineByExtension(path);
    if (engine == null) {
      throw new TemplateEngineNotFoundException(path);
    }

    Template template = engine.getTemplate(path);
    if (template == null) {
      throw new TemplateNotFoundException(path);
    }

    return template;
  }

}
