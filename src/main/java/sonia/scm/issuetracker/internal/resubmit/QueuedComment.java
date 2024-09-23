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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sonia.scm.xml.XmlInstantAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class QueuedComment {

  private String repository;
  private String issueTracker;
  private String issueKey;
  private String comment;

  @EqualsAndHashCode.Exclude
  private int retries = 0;

  @EqualsAndHashCode.Exclude
  @XmlJavaTypeAdapter(XmlInstantAdapter.class)
  private Instant date;

  public QueuedComment(String repository, String issueTracker, String issueKey, String comment) {
    this.repository = repository;
    this.issueTracker = issueTracker;
    this.issueKey = issueKey;
    this.comment = comment;
    this.retries = 0;
    this.date = Instant.now();
  }

  void retried() {
    retries = retries + 1;
  }
}
