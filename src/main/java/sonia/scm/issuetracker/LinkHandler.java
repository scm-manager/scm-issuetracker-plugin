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

package sonia.scm.issuetracker;

import com.google.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;


/**
 *
 * @author Sebastian Sdorra
 * @deprecated
 */
@Deprecated
public final class LinkHandler {

  private ScmConfiguration configuration;

  @Inject
  public LinkHandler(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  public String getDiffUrl(IssueRequest request) {
    Changeset changeset = request.getChangeset();
    return getRepositoryUrl(request) + "/changeset/" + changeset.getId();
  }

  public String getRepositoryUrl(IssueRequest request) {
    Repository repo = request.getRepository();
    return String.format("%s/repo/%s/%s", configuration.getBaseUrl(), repo.getNamespace(), repo.getName());
  }
}
