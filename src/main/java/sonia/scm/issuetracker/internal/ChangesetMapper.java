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

package sonia.scm.issuetracker.internal;

import com.google.common.base.Strings;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Contributor;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class ChangesetMapper {

  public static final String TYPE = "changeset";

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
      mapContributors(changeset.getContributors()),
      Instant.ofEpochMilli(changeset.getDate()),
      content(changeset),
      link(repository, changeset),
      true,
      changeset
    );
  }

  private Map<String, Collection<Person>> mapContributors(Collection<Contributor> contributors) {
    if (contributors == null) {
      return emptyMap();
    }
    MultiMap map = new MultiValueMap();
    contributors.forEach(
      contributor -> map.put(contributor.getType(), contributor.getPerson())
    );
    return map;
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
