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

import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryTestData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssueReferencingObjects {

  private IssueReferencingObjects() {
  }

  public static IssueReferencingObject ref(String type, String id) {
    return new IssueReferencingObject(
      RepositoryTestData.createHeartOfGold(),
      type,
      id,
      Person.toPerson("Trillian"),
      Collections.emptyMap(),
      Instant.now(),
      Collections.emptyList(),
      "https://hitchhiker.com/scm",
      true,
      type + "/" + id
    );
  }

  public static IssueReferencingObject content(String... values) {
    return content(true, values);
  }

  public static IssueReferencingObject content(boolean triggeringStateChange, String... values) {
    List<Content> content = new ArrayList<>();
    for (int i = 0; i < values.length; i++) {
      content.add(new Content("c" + i, values[i]));
    }
    return new IssueReferencingObject(
      RepositoryTestData.createHeartOfGold(),
      "unit-test",
      "42",
      Person.toPerson("Trillian"),
      Collections.emptyMap(),
      Instant.now(),
      content,
      "https://hitchhiker.com/scm",
      triggeringStateChange,
      values
    );
  }
}
