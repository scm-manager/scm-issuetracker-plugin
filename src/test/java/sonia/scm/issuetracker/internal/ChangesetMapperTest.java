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
