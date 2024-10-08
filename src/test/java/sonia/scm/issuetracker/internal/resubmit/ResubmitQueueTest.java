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

import com.google.common.collect.Multimap;
import org.apache.shiro.authz.UnauthorizedException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith({ShiroExtension.class, MockitoExtension.class})
class ResubmitQueueTest {

  private AtomicInteger counter;

  @Mock
  private NotificationService notificationService;

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
      queue = new ResubmitQueue(new InMemoryDataStoreFactory(), notificationService);
    }


    @Test
    void shouldQueue() {
      QueuedComment comment = comment("redmine");
      queue.append(comment);
      assertThat(queue.getComments("redmine")).containsOnly(comment);
    }

    @Test
    void shouldSendCommentNotification() {
      QueuedComment comment = comment("redmine");
      queue.append(comment);

      verify(notificationService).notifyComment(comment);
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

    @Test
    void shouldSendResubmitNotication() {
      Set<QueuedComment> one = Collections.singleton(comment("redmine"));
      Set<QueuedComment> two = Collections.singleton(comment("redmine"));
      queue.sync("redmine",one, two);

      verify(notificationService).notifyResubmit("redmine", one, two);
    }

  }

  @Nested
  @SubjectAware(value = "marvin", permissions = "issuetracker:resubmit:*")
  class Limited {

    @Test
    void shouldKeepOnlyTheLastTwoComments() {
      ResubmitQueue queue = new ResubmitQueue(new InMemoryDataStoreFactory(), notificationService, 2);

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
      queue = new ResubmitQueue(new InMemoryDataStoreFactory(), notificationService);
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
