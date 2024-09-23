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

import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class IndexLinkEnricherTest {

  private IndexLinkEnricher enricher;

  @Mock
  private HalEnricherContext context;

  @Mock
  private HalAppender appender;

  @Mock
  private HalAppender.LinkArrayBuilder arrayBuilder;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore store = new ScmPathInfoStore();
    store.set(() -> URI.create("/"));
    enricher = new IndexLinkEnricher(Providers.of(store));
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "issuetracker:resubmit")
  void shouldAppendResubmitLink() {
    when(appender.linkArrayBuilder("issueTracker")).thenReturn(arrayBuilder);

    enricher.enrich(context, appender);

    verify(arrayBuilder).append("resubmit", "/v2/issue-tracker/resubmits");
    verify(arrayBuilder).build();
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "issuetracker:resubmit")
  void shouldAppendResubmitConfigurationLink() {
    when(appender.linkArrayBuilder("issueTracker")).thenReturn(arrayBuilder);

    enricher.enrich(context, appender);

    verify(arrayBuilder).append("resubmitConfiguration", "/v2/issue-tracker/resubmits/config");
    verify(arrayBuilder).build();
  }

  @Test
  @SubjectAware(value = "trillian")
  void shouldNotAppendResubmitLinkWithoutPermission() {
    enricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }
}
