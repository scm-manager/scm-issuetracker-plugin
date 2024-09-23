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
