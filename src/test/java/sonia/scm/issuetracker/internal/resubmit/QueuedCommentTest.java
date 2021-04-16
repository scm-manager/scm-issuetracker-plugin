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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueuedCommentTest {

  @Test
  void shouldIgnoreRetryCountForEquals() {
    QueuedComment one = new QueuedComment("42", "redmine", "#42", "Jo", 0);
    QueuedComment two = new QueuedComment("42", "redmine", "#42", "Jo", 0);
    assertThat(one).isEqualTo(two);

    QueuedComment three = new QueuedComment("42", "redmine", "#42", "Jo", 1);
    assertThat(one).isEqualTo(three);
  }

  @Test
  void shouldIncreaseReties() {
    QueuedComment comment = new QueuedComment("21", "jira", "ISD-4", "No", 0);
    assertThat(comment.getRetries()).isZero();
    comment.retried();
    assertThat(comment.getRetries()).isOne();
    comment.retried();
    assertThat(comment.getRetries()).isEqualTo(2);
  }

}
