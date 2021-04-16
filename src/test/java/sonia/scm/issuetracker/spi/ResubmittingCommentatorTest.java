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

package sonia.scm.issuetracker.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResubmittingCommentatorTest {

  @Mock
  private ResubmitRepositoryQueue queue;

  @Mock
  private Commentator commentator;

  @InjectMocks
  private ResubmittingCommentator resubmittingCommentator;

  @Test
  void shouldNotAppendOnSuccess() throws IOException {
    resubmittingCommentator.comment("#42", "Awesome");

    verify(commentator).comment("#42", "Awesome");
    verifyNoInteractions(queue);
  }

  @Test
  void shouldAppendOnException() throws IOException {
    doThrow(new IOException("failed!")).when(commentator).comment("#21", "Incredible");

    resubmittingCommentator.comment("#21", "Incredible");
    verify(queue).append("#21", "Incredible");
  }

  @Test
  void shouldDelegateResubmitToCommentator() throws IOException {
    resubmittingCommentator.resubmit("#42", "Awesome");

    verify(commentator).comment("#42", "Awesome");
  }

}
