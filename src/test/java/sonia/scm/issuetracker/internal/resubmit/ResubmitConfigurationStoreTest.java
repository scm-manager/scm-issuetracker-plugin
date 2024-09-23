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

package sonia.scm.issuetracker.internal.resubmit;

import com.google.common.collect.ImmutableList;
import org.apache.shiro.authz.UnauthorizedException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShiroExtension.class)
class ResubmitConfigurationStoreTest {

  private ResubmitConfigurationStore store;

  @BeforeEach
  void setUpStore() {
    store = new ResubmitConfigurationStore(new InMemoryConfigurationStoreFactory());
  }

  @Test
  @SubjectAware("trillian")
  void shouldFailWithoutPermission() {
    ResubmitConfiguration configuration = new ResubmitConfiguration();
    assertThrows(UnauthorizedException.class, () -> store.set(configuration));
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "issuetracker:resubmit:*")
  void shouldReturnEmptyConfigurationOnFirstGet() {
    ResubmitConfiguration resubmitConfiguration = store.get();

    assertThat(resubmitConfiguration).isNotNull();
    assertThat(resubmitConfiguration.getAddresses()).isEmpty();
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "issuetracker:resubmit:*")
  void shouldSetAndGet() {
    ResubmitConfiguration configuration = new ResubmitConfiguration();
    configuration.setAddresses(ImmutableList.of("trillian@hitchhiker.com"));

    store.set(configuration);

    assertThat(store.get().getAddresses()).containsOnly("trillian@hitchhiker.com");
  }

}
