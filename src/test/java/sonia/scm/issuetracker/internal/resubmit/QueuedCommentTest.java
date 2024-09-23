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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueuedCommentTest {

  @Test
  void shouldIgnoreRetryCountForEquals() {
    QueuedComment one = new QueuedComment("42", "redmine", "#42", "Jo");
    QueuedComment two = new QueuedComment("42", "redmine", "#42", "Jo");
    assertThat(one).isEqualTo(two);

    QueuedComment three = new QueuedComment("42", "redmine", "#42", "Jo");
    three.retried();
    assertThat(one).isEqualTo(three);
  }

  @Test
  void shouldIncreaseReties() {
    QueuedComment comment = new QueuedComment("21", "jira", "ISD-4", "No");
    assertThat(comment.getRetries()).isZero();
    comment.retried();
    assertThat(comment.getRetries()).isOne();
    comment.retried();
    assertThat(comment.getRetries()).isEqualTo(2);
  }

}
