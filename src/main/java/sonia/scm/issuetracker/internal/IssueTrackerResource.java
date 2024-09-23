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

import sonia.scm.issuetracker.internal.resubmit.ResubmitResource;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

@Path("v2/issue-tracker")
public class IssueTrackerResource {

  private final Provider<ResubmitResource> resubmitResource;

  @Inject
  public IssueTrackerResource(Provider<ResubmitResource> resubmitResource) {
    this.resubmitResource = resubmitResource;
  }

  @Path("resubmits")
  public ResubmitResource resubmits() {
    return resubmitResource.get();
  }

}
