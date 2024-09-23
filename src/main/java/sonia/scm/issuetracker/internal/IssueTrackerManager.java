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

package sonia.scm.issuetracker.internal;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.issuetracker.IssueTracker;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @deprecated use {@link sonia.scm.issuetracker.api.IssueTracker} instead.
 */
@Singleton
@Deprecated
public class IssueTrackerManager
{

  /**
   * Constructs ...
   *
   *
   * @param issueTrackers
   */
  @Inject
  public IssueTrackerManager(Set<IssueTracker> issueTrackers)
  {
    if (issueTrackers == null)
    {
      this.issueTrackers = ImmutableSet.of();
    }
    else
    {
      this.issueTrackers = issueTrackers;
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<IssueTracker> getIssueTrackers()
  {
    return issueTrackers;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Set<IssueTracker> issueTrackers;
}
