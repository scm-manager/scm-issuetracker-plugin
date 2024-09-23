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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

@Getter
@SuppressWarnings("java:S2160") // we don't need equals for dto
public class ResubmitDto extends HalRepresentation {

  private final String issueTracker;
  private final int queueSize;
  private final boolean inProgress;

  public ResubmitDto(Links links, String issueTracker, int queueSize, boolean inProgress) {
    super(links);
    this.issueTracker = issueTracker;
    this.queueSize = queueSize;
    this.inProgress = inProgress;
  }
}
