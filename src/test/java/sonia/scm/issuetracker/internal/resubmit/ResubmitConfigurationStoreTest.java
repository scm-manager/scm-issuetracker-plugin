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
