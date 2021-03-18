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

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateBasedHandlerTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final PullRequest PULL_REQUEST = new PullRequest("42", "feature", "develop");
  private static final DisplayUser DISPLAY_USER = DisplayUser.from(new User("dent", "Arthur Dent", ""));

  @Mock
  private TemplateEngineFactory templateEngineFactory;
  @Mock
  private TemplateEngine templateEngine;
  @Mock
  private Template template;

  private TemplateBasedHandler templateHandler;
  private IssueRequest request;
  private Changeset changeset;

  private String systemLineSeparator = "\n";

  @Nested
  class ForChangesets {

    @BeforeEach
    void initChangeset() {
      when(changeset.getAuthor()).thenReturn(new Person("Arthur Dent"));
    }

    @Test
    void shouldNotSplitSingleLine() {
      when(changeset.getDescription()).thenReturn("description");
      Map<String, Object> env = templateHandler.createModel(request, null);
      assertThat(env.get("descriptionLine")).asList().containsExactly("description");
    }

    @Test
    void shouldSplitWithUnixLineSeparator() {
      when(changeset.getDescription()).thenReturn("one\ntwo");
      Map<String, Object> env = templateHandler.createModel(request, null);
      assertThat(env.get("descriptionLine")).asList().containsExactly("one", "two");
    }

    @Test
    void shouldSplitWithWindowsLineSeparator() {
      systemLineSeparator = "\r\n";
      when(changeset.getDescription()).thenReturn("one\r\ntwo");
      Map<String, Object> env = templateHandler.createModel(request, null);
      assertThat(env.get("descriptionLine")).asList().containsExactly("one", "two");
    }

    @Test
    void shouldSplitWithUnixLineSeparatorEvenWhenOtherSeparatorIsConfigured() {
      systemLineSeparator = "\r\n";
      when(changeset.getDescription()).thenReturn("one\ntwo");
      Map<String, Object> env = templateHandler.createModel(request, null);
      assertThat(env.get("descriptionLine")).asList().containsExactly("one", "two");
    }
  }

  @Nested
  class ForPullRequests {

    @Test
    void shouldCreatePullRequestModel() throws IOException {
      templateHandler.createComment(new PullRequestIssueRequestData("created", REPOSITORY, PULL_REQUEST, DISPLAY_USER, emptySet()));
      verify(template).execute(any(), argThat(
        model -> {
          assertThat(model)
            .isInstanceOf(Map.class)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .contains(
              entry("author", "Arthur Dent"),
              entry("keyword", "created"),
              entry("pullRequest", PULL_REQUEST),
              entry("repository", REPOSITORY),
              entry("pullRequestUrl", "http://hog/scm/repo/hitchhiker/HeartOfGold/pull-request/42")
            );
          return true;
        }
      ));
    }

    @BeforeEach
    void initTemplates() {
      when(templateEngineFactory.getDefaultEngine()).thenReturn(templateEngine);
    }
  }

  @BeforeEach
  public void init() {
    changeset = mock(Changeset.class);
    request = new IssueRequest(new Repository("id", "git", "space", "X"), changeset, null, empty());
    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setBaseUrl("http://hog/scm");
    templateHandler = new TemplateBasedHandler(templateEngineFactory, new LinkHandler(configuration)) {
      @Override
      String getSystemLineSeparator() {
        return systemLineSeparator;
      }

      @Override
      protected Template loadTemplate(TemplateEngine engine) throws IOException {
        return template;
      }
    };
  }
}
