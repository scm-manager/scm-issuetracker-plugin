/**
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

import org.junit.Before;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemplateBasedHandlerTest {

  private TemplateBasedHandler templateHandler;
  private IssueRequest request;
  private Changeset changeset;

  private String systemLineSeparator = "\n";

  @Test
  public void shouldNotSplitSingleLine() {
    when(changeset.getDescription()).thenReturn("description");
    Map<String, Object> env = templateHandler.createModel(request, null);
    assertEquals(asList("description"), env.get("descriptionLine"));
  }

  @Test
  public void shouldSplitWithUnixLineSeparator() {
    when(changeset.getDescription()).thenReturn("one\ntwo");
    Map<String, Object> env = templateHandler.createModel(request, null);
    assertEquals(asList("one", "two"), env.get("descriptionLine"));
  }

  @Test
  public void shouldSplitWithWindowsLineSeparator() {
    systemLineSeparator = "\r\n";
    when(changeset.getDescription()).thenReturn("one\r\ntwo");
    Map<String, Object> env = templateHandler.createModel(request, null);
    assertEquals(asList("one", "two"), env.get("descriptionLine"));
  }

  @Test
  public void shouldSplitWithUnixLineSeparatorEvenWhenOtherSeparatorIsConfigured() {
    systemLineSeparator = "\r\n";
    when(changeset.getDescription()).thenReturn("one\ntwo");
    Map<String, Object> env = templateHandler.createModel(request, null);
    assertEquals(asList("one", "two"), env.get("descriptionLine"));
  }

  @Before
  public void init() {
    changeset = mock(Changeset.class);
    when(changeset.getAuthor()).thenReturn(new Person("Arthur Dent"));
    request = new IssueRequest(new Repository("id", "git", "space", "X"), changeset, null, empty());
    ScmConfiguration configuration = mock(ScmConfiguration.class);
    templateHandler = new TemplateBasedHandler(null, new LinkHandler(configuration)) {
      @Override
      String getSystemLineSeparator() {
        return systemLineSeparator;
      }

      @Override
      protected Template loadTemplate(TemplateEngine engine) throws IOException {
        return null;
      }
    };
  }
}
