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

import sonia.scm.repository.Person;
import sonia.scm.user.UserDisplayManager;

import jakarta.inject.Inject;

public class PersonMapper {

  private final UserDisplayManager userDisplayManager;

  @Inject
  public PersonMapper(UserDisplayManager userDisplayManager) {
    this.userDisplayManager = userDisplayManager;
  }

  public Person person(String name) {
    return userDisplayManager.get(name)
      .map(displayUser -> new Person(displayUser.getDisplayName(), displayUser.getMail()))
      .orElse(Person.toPerson(name));
  }
}
