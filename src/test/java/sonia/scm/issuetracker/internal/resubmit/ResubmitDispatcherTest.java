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

import org.apache.shiro.authz.UnauthorizedException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class ResubmitDispatcherTest {

  @Mock
  private ResubmitProcessorFactory processorFactory;

  @Mock
  private ResubmitProcessor processor;

  @Mock
  private ResubmitQueue queue;

  @InjectMocks
  private ResubmitDispatcher dispatcher;

  @Test
  @SubjectAware("dent")
  void shouldFailWithoutPermission() {
    assertThrows(UnauthorizedException.class, () -> dispatcher.resubmit("test"));
    assertThrows(UnauthorizedException.class, () -> dispatcher.resubmitAsync("test"));
  }

  @Nested
  @SubjectAware(value = "trillian", permissions = "issuetracker:resubmit")
  class Permitted {

    @Test
    void shouldResubmitAndSync() {
      when(processorFactory.create()).thenReturn(processor);

      Set<QueuedComment> remove = Collections.singleton(comment("redmine"));
      when(processor.getRemove()).thenReturn(remove);
      Set<QueuedComment> requeue = Collections.singleton(comment("redmine"));
      when(processor.getRequeue()).thenReturn(requeue);

      dispatcher.resubmit("redmine");

      verify(processor).resubmit("redmine");
      verify(queue).sync("redmine", remove, requeue);
    }

    @Test
    void shouldSetInProgressFlag() {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      when(processorFactory.create()).thenAnswer(ic -> {
        countDownLatch.await(1, TimeUnit.SECONDS);
        return processor;
      });

      dispatcher.resubmitAsync("jira");

      await()
        .atMost(500, TimeUnit.MILLISECONDS)
        .until(dispatcher::isInProgress);

      countDownLatch.countDown();

      await()
        .atMost(500, TimeUnit.MILLISECONDS)
        .until(() -> !dispatcher.isInProgress());
    }

  }

  @AfterEach
  void tearDown() {
    dispatcher.close();
  }

  private QueuedComment comment(String issueTracker) {
    return new QueuedComment("21", issueTracker, "#42", "Incredible");
  }
}
