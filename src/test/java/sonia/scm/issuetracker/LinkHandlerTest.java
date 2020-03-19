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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;

import java.util.Collections;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkHandlerTest {

  @Mock
  private ScmConfiguration configuration;

  @InjectMocks
  private LinkHandler linkHandler;

  @BeforeEach
  void setup() {
    when(configuration.getBaseUrl()).thenReturn("https://hitchhiker.com/scm");
  }

  @Test
  void getDiffUrl() {
    Changeset changeset = new Changeset();
    changeset.setId("1234567890");

    IssueRequest issueRequest = new IssueRequest(RepositoryTestData.createHeartOfGold(), changeset, Collections.emptyList(), of(new User()));
    String url = linkHandler.getDiffUrl(issueRequest);
    assertThat(url).isEqualTo("https://hitchhiker.com/scm/repo/hitchhiker/HeartOfGold/changeset/1234567890");

  }

  @Test
  void getRepositoryUrl() {
    Changeset changeset = new Changeset();
    changeset.setId("1234567890");

    IssueRequest issueRequest = new IssueRequest(RepositoryTestData.createHeartOfGold(), changeset, Collections.emptyList(), of(new User()));
    String url = linkHandler.getRepositoryUrl(issueRequest);
    assertThat(url).isEqualTo("https://hitchhiker.com/scm/repo/hitchhiker/HeartOfGold");
  }
}
