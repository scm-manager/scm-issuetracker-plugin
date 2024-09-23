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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.internal.PersonMapper;
import sonia.scm.repository.Person;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserTestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

  @Mock
  private UserDisplayManager userDisplayManager;

  @InjectMocks
  private PersonMapper personMapper;

  @Test
  void shouldReturnNameWithoutDisplayUser() {
    Person tricia = personMapper.person("tricia");
    assertThat(tricia).satisfies(person -> {
      assertThat(person.getName()).isEqualTo("tricia");
      assertThat(person.getMail()).isNull();
    });
  }

  @Test
  void shouldReturnDisplayUser() {
    when(userDisplayManager.get("tricia")).thenReturn(
      Optional.of(DisplayUser.from(UserTestData.createTrillian()))
    );

    Person tricia = personMapper.person("tricia");
    assertThat(tricia).satisfies(person -> {
      assertThat(person.getName()).isEqualTo("Tricia McMillan");
      assertThat(person.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
    });
  }


}
