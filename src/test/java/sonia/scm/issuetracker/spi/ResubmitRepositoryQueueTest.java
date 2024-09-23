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

package sonia.scm.issuetracker.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.internal.resubmit.QueuedComment;
import sonia.scm.issuetracker.internal.resubmit.ResubmitQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResubmitRepositoryQueueTest {

  @Mock
  private ResubmitQueue resubmitQueue;

  @Captor
  private ArgumentCaptor<QueuedComment> commentArgumentCaptor;

  @Test
  void shouldAppendComment() {
    ResubmitRepositoryQueue queue = new ResubmitRepositoryQueue(resubmitQueue, "hog", "redmine");
    queue.append("#42", "Awesome");

    verify(resubmitQueue).append(commentArgumentCaptor.capture());

    QueuedComment comment = commentArgumentCaptor.getValue();
    assertThat(comment.getRepository()).isEqualTo("hog");
    assertThat(comment.getIssueTracker()).isEqualTo("redmine");
    assertThat(comment.getIssueKey()).isEqualTo("#42");
    assertThat(comment.getComment()).isEqualTo("Awesome");
    assertThat(comment.getRetries()).isZero();
  }

}
