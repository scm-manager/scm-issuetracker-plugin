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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryDataStore;

import java.time.Instant;
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
      Instant.now(),
      Collections.emptyList(),
      "https://hitchhiker.com",
      "KA"
    );
  }

}
