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

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryDataStore;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedStoreTest {

  private ProcessedStore store;

  @BeforeEach
  void setUpStore() {
    store = new ProcessedStore(new InMemoryDataStore<>());
  }

  @Test
  void shouldStoreMarksWithoutKeyWord() {
    store.mark("42", ref("changeset", "4211"));
    assertThat(store.isProcessed("42", ref("changeset", "4211"))).isTrue();
  }

  @Test
  void shouldRespectType() {
    store.mark("21", ref("pull-request", "12"));
    assertThat(store.isProcessed("21", ref("comment", "12"))).isFalse();
  }

  @Test
  void shouldRespectId() {
    store.mark("21", ref("pull-request", "12"));
    assertThat(store.isProcessed("21", ref("pull-request", "21"))).isFalse();
  }

  @Test
  void shouldStoreMarksWithKeyWord() {
    store.mark("21", ref("pull-request", "21"), "closed");
    assertThat(store.isProcessed("21", ref("pull-request", "21"), "closed")).isTrue();
  }

  @Test
  void shouldRespectKeyWord() {
    store.mark("21", ref("pull-request", "12"), "closed");
    assertThat(store.isProcessed("21", ref("pull-request", "21"))).isFalse();
    assertThat(store.isProcessed("21", ref("pull-request", "21"), "fixed")).isFalse();
  }

  private IssueReferencingObject ref(String type, String id) {
    return new IssueReferencingObject(
      RepositoryTestData.createHeartOfGold(),
      type,
      id,
      Person.toPerson("Trillian"),
      Maps.newHashMap("bystanders", Arrays.asList(new Person("Arthur"), new Person("Ford"))),
      Instant.now(),
      Collections.emptyList(),
      "https://hitchhiker.com",
      true,
      "KA"
    );
  }

}
