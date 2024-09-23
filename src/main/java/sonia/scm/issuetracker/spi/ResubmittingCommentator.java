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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.issuetracker.api.Resubmitter;

import java.io.IOException;

class ResubmittingCommentator implements Commentator, Resubmitter {

  private static final Logger LOG = LoggerFactory.getLogger(ResubmittingCommentator.class);

  private final Commentator commentator;
  private final ResubmitRepositoryQueue queue;

  ResubmittingCommentator(ResubmitRepositoryQueue queue, Commentator commentator) {
    this.queue = queue;
    this.commentator = commentator;
  }

  @Override
  public void comment(String issueKey, String comment) throws IOException {
    try {
      commentator.comment(issueKey, comment);
    } catch (IOException ex) {
      LOG.warn("failed to append comment for issue {}, queue for resubmit", issueKey, ex);
      queue.append(issueKey, comment);
    }
  }

  @Override
  public void resubmit(String issueKey, String comment) throws IOException {
    commentator.comment(issueKey, comment);
  }
}
