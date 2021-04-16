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

import com.google.common.collect.Multimap;
import org.apache.shiro.authz.UnauthorizedException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShiroExtension.class)
class ResubmitQueueTest {

  private AtomicInteger counter;

  @BeforeEach
  void setUp() {
    counter = new AtomicInteger();
  }

  @Nested
  @SubjectAware(value = "trillian", permissions = "issuetracker:resubmit:*")
  class Default {

    private ResubmitQueue queue;


    @BeforeEach
    void setUp() {
      queue = new ResubmitQueue(new InMemoryDataStoreFactory());
    }


    @Test
    void shouldQueue() {
      QueuedComment comment = comment("redmine");
      queue.append(comment);
      assertThat(queue.getComments("redmine")).containsOnly(comment);
    }

    @Test
    void shouldUseDifferentQueuesForEachTracker() {
      QueuedComment redmine = comment("redmine");
      queue.append(redmine);

      QueuedComment jira = comment("jira");
      queue.append(jira);

      assertThat(queue.getComments("redmine")).containsOnly(redmine);
      assertThat(queue.getComments("jira")).containsOnly(jira);
    }

    @Test
    void shouldClearQueue() {
      queue.append(comment("redmine"));
      queue.append(comment("redmine"));
      queue.append(comment("jira"));

      queue.clear("redmine");

      assertThat(queue.getComments("redmine")).isEmpty();
      assertThat(queue.getComments("jira")).hasSize(1);
    }

    @Test
    void shouldReturnMultimap() {
      QueuedComment redmine = comment("redmine");
      queue.append(redmine);

      QueuedComment jira = comment("jira");
      queue.append(jira);

      Multimap<String, QueuedComment> comments = queue.getComments();
      assertThat(comments.get("redmine")).containsOnly(redmine);
      assertThat(comments.get("jira")).containsOnly(jira);
    }

    @Test
    void shouldSync() {
      QueuedComment one = comment("redmine");
      queue.append(one);
      QueuedComment two = comment("redmine");
      queue.append(two);
      QueuedComment three = comment("redmine");
      queue.append(three);

      queue.sync("redmine", Collections.singleton(one), Collections.singleton(three));

      List<QueuedComment> redmine = queue.getComments("redmine");
      assertThat(redmine).containsOnly(two, three);

      for (QueuedComment comment : queue.getComments("redmine")) {
        if (comment.equals(three)) {
          assertThat(three.getRetries()).isOne();
        } else {
          assertThat(two.getRetries()).isZero();
        }
      }
    }

  }

  @Nested
  @SubjectAware(value = "marvin", permissions = "issuetracker:resubmit:*")
  class Limited {

    @Test
    void shouldKeepOnlyTheLastTwoComments() {
      ResubmitQueue queue = new ResubmitQueue(new InMemoryDataStoreFactory(), 2);

      queue.append(comment("redmine"));
      queue.append(comment("redmine"));
      queue.append(comment("redmine"));

      QueuedComment one = comment("redmine");
      queue.append(one);
      QueuedComment two = comment("redmine");
      queue.append(two);

      assertThat(queue.getComments("redmine")).containsOnly(one, two);
    }

  }

  @Nested
  class Unauthorized {

    private ResubmitQueue queue;

    @BeforeEach
    void setUp() {
      queue = new ResubmitQueue(new InMemoryDataStoreFactory());
    }

    @Test
    @SubjectAware(value = "dent", permissions = "issuetracker:resubmit:jira")
    void shouldReturnOnlyAuthorizedComments() {
      queue.append(comment("redmine"));

      QueuedComment jira = comment("jira");
      queue.append(jira);

      Multimap<String, QueuedComment> comments = queue.getComments();
      assertThat(comments.size()).isEqualTo(1);
      assertThat(comments.values()).containsOnly(jira);
    }

    @Test
    @SubjectAware("ford")
    void shouldFailIfNotAuthorizedToClear() {
      assertThrows(UnauthorizedException.class, () -> queue.clear("redmine"));
    }

    @Test
    @SubjectAware("ford")
    void shouldFailIfNotAuthorizedToSync() {
      Set<QueuedComment> empty = Collections.emptySet();
      assertThrows(UnauthorizedException.class, () -> queue.sync("redmine", empty, empty));
    }

  }

  private QueuedComment comment(String issueTracker) {
    String issueKey = "#" + counter.incrementAndGet();
    return new QueuedComment("4211", issueTracker, issueKey, "Content of" + issueKey);
  }
}
