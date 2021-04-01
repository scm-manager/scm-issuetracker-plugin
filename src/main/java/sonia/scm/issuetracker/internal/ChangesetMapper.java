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

package sonia.scm.issuetracker.internal;

import com.google.common.base.Strings;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class ChangesetMapper {

  static final String TYPE = "changeset";

  private final ScmConfiguration configuration;

  @Inject
  public ChangesetMapper(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  public IssueReferencingObject ref(Repository repository, Changeset changeset) {
    return new IssueReferencingObject(
      repository,
      TYPE,
      changeset.getId(),
      changeset.getAuthor(),
      Instant.ofEpochMilli(changeset.getDate()),
      content(changeset),
      link(repository, changeset),
      changeset
    );
  }

  @SuppressWarnings("java:S1192") // constant does not mean the same
  private String link(Repository repository, Changeset changeset) {
    return HttpUtil.concatenate(
      configuration.getBaseUrl(),
      "repo", repository.getNamespace(), repository.getName(), "code", "changeset", changeset.getId()
    );
  }

  private List<Content> content(Changeset changeset) {
    if (Strings.isNullOrEmpty(changeset.getDescription())) {
      return Collections.emptyList();
    }
    return Collections.singletonList(
      new Content("description", changeset.getDescription())
    );
  }
}
