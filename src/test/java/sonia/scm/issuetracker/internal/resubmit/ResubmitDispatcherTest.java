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

import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

  @AfterEach
  void tearDown() {
    dispatcher.close();
  }

  private QueuedComment comment(String issueTracker) {
    return new QueuedComment("21", issueTracker,"#42", "Incredible", 0);
  }
}
