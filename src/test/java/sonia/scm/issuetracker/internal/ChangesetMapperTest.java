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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Contributor;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ChangesetMapperTest {

  private ChangesetMapper mapper;

  @BeforeEach
  void setUpMapper() {
    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setBaseUrl("https://hitchhiker.com/scm");
    mapper = new ChangesetMapper(configuration);
  }

  @Test
  void shouldMapToRef() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    Instant now = Instant.now();
    Person trillian = Person.toPerson("trillian");
    Changeset changeset = new Changeset("42", now.toEpochMilli(), trillian, "ABC-42 is fixed");
    changeset.setContributors(
      Arrays.asList(
        new Contributor("captain", new Person("Zaphod")),
        new Contributor("crew", new Person("Trillian")),
        new Contributor("crew", new Person("Marvin"))
      )
    );

    IssueReferencingObject ref = mapper.ref(heartOfGold, changeset);
    assertThat(ref.getType()).isEqualTo(ChangesetMapper.TYPE);
    assertThat(ref.getId()).isEqualTo("42");
    assertThat(ref.getAuthor()).isEqualTo(trillian);
    assertThat(ref.getDate().toEpochMilli()).isEqualTo(now.toEpochMilli());
    assertThat(ref.getContributors().get("captain")).contains(new Person("Zaphod"));
    assertThat(ref.getContributors().get("crew")).contains(new Person("Trillian"), new Person("Marvin"));

    Content content = ref.getContent().get(0);
    assertThat(content.getType()).isEqualTo("description");
    assertThat(content.getValue()).isEqualTo("ABC-42 is fixed");

    assertThat(ref.getLink()).isEqualTo("https://hitchhiker.com/scm/repo/hitchhiker/HeartOfGold/code/changeset/42");

    assertThat(ref.isTriggeringStateChange()).isTrue();

    assertThat(ref.getOrigin()).isSameAs(changeset);
  }

}
