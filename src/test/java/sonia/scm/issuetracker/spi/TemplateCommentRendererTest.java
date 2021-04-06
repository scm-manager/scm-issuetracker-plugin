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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Person;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sonia.scm.issuetracker.IssueReferencingObjects.content;
import static sonia.scm.issuetracker.IssueReferencingObjects.ref;

@ExtendWith(MockitoExtension.class)
class TemplateCommentRendererTest {

  @Mock
  private TemplateEngineFactory engineFactory;

  @Mock
  private TemplateEngine engine;

  @Mock
  private Template template;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Subject subject;

  @BeforeEach
  void setUpSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject(){
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldRenderReferenceTemplate() throws IOException {
    when(engineFactory.getEngineByExtension("/tpls/changeset.mustache")).thenReturn(engine);
    when(engine.getTemplate("/tpls/changeset.mustache")).thenReturn(template);

    IssueReferencingObject ref = ref("changeset", "4211");
    doAnswer(ic -> {
      Writer writer = ic.getArgument(0);
      writer.write("Awesome");
      return null;
    }).when(template).execute(any(Writer.class), any(Map.class));

    TemplateCommentRendererFactory factory = new TemplateCommentRendererFactory(engineFactory);
    ReferenceCommentRenderer renderer = factory.reference("/tpls/{0}.mustache");

    String comment = renderer.render(ref);
    assertThat(comment).isEqualTo("Awesome");
  }

  @Test
  void shouldRenderChangeStateTemplate() throws IOException {
    when(engineFactory.getEngineByExtension("/tpls/pr.mustache")).thenReturn(engine);
    when(engine.getTemplate("/tpls/pr.mustache")).thenReturn(template);

    IssueReferencingObject ref = ref("pr", "42");
    doAnswer(ic -> {
      Writer writer = ic.getArgument(0);
      Map<String, Object> model = ic.getArgument(1);
      String msg = String.format("%s is %s", model.get("type"), model.get("keyWord"));
      writer.write(msg);
      return null;
    }).when(template).execute(any(Writer.class), any(Map.class));

    TemplateCommentRendererFactory factory = new TemplateCommentRendererFactory(engineFactory);
    StateChangeCommentRenderer renderer = factory.stateChange("/tpls/{0}.mustache");

    String comment = renderer.render(ref, "resolved");
    assertThat(comment).isEqualTo("pr is resolved");
  }

  @Test
  void shouldThrowTemplateNotFoundException() {
    when(engineFactory.getEngineByExtension("/tpls/pr.mustache")).thenReturn(engine);

    TemplateCommentRendererFactory factory = new TemplateCommentRendererFactory(engineFactory);
    ReferenceCommentRenderer renderer = factory.reference("/tpls/{0}.mustache");
    IssueReferencingObject ref = ref("pr", "42");

    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class, () -> renderer.render(ref));
    assertThat(exception.getTemplatePath()).isEqualTo("/tpls/pr.mustache");
  }

  @Test
  void shouldThrowTemplateEngineNotFoundException() {
    TemplateCommentRendererFactory factory = new TemplateCommentRendererFactory(engineFactory);
    ReferenceCommentRenderer renderer = factory.reference("/tpls/{0}.mustache");
    IssueReferencingObject ref = ref("pr", "42");

    TemplateEngineNotFoundException exception = assertThrows(TemplateEngineNotFoundException.class, () -> renderer.render(ref));
    assertThat(exception.getTemplatePath()).isEqualTo("/tpls/pr.mustache");
  }


  @Test
  @SuppressWarnings("unchecked")
  void shouldCreateRichModel() throws IOException {
    when(subject.getPrincipals().oneByType(User.class)).thenReturn(UserTestData.createTrillian());

    when(engineFactory.getEngineByExtension("/tpls/unit-test.mustache")).thenReturn(engine);
    when(engine.getTemplate("/tpls/unit-test.mustache")).thenReturn(template);

    IssueReferencingObject ref = content("Awesome", "Incredible");

    TemplateCommentRendererFactory factory = new TemplateCommentRendererFactory(engineFactory);
    ReferenceCommentRenderer renderer = factory.reference("/tpls/{0}.mustache");
    renderer.render(ref);

    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
    verify(template).execute(any(), captor.capture());

    Map<String, Object> model = captor.getValue();
    assertThat(model)
      .containsEntry("type", "unit-test")
      .containsEntry("id", "42")
      .containsEntry("repository", ref.getRepository())
      .hasEntrySatisfying("content", object -> {
        assertThat(object).isInstanceOfSatisfying(Map.class, map -> {
          assertThat(map).containsEntry("c0", "Awesome");
          assertThat(map).containsEntry("c1", "Incredible");
        });
      })
      .hasEntrySatisfying("principal", object -> {
        assertThat(object).isInstanceOfSatisfying(Person.class, person -> {
          assertThat(person.getName()).isEqualTo("Tricia McMillan");
          assertThat(person.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
        });
      });
  }

}
