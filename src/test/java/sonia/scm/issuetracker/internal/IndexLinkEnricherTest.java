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
  @SubjectAware(value = "trillian")
  void shouldNotAppendResubmitLinkWithoutPermission() {
    enricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }
}
