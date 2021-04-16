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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResubmitResource {

  @VisibleForTesting
  static final String MEDIA_TYPE = VndMediaType.PREFIX + "issueTrackerResubmit" + VndMediaType.SUFFIX;

  private final ResubmitQueue queue;
  private final ResubmitDispatcher action;

  @Inject
  public ResubmitResource(ResubmitQueue queue, ResubmitDispatcher action) {
    this.queue = queue;
    this.action = action;
  }

  @GET
  @Path("")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get issue tracker resubmits",
    description = "Returns a collection of issue trackers which have queued comments.",
    tags = "Issue Tracker",
    operationId = "issue_tracker_resubmits"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE,
      schema = @Schema(implementation = ResubmitDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"resubmit\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation resubmits(@Context UriInfo uriInfo) {
    List<ResubmitDto> embedded = new ArrayList<>();
    Multimap<String, QueuedComment> comments = queue.getComments();
    for (String issueTracker : comments.keySet()) {
      embedded.add(createDto(uriInfo, issueTracker, comments.get(issueTracker)));
    }
    Links links = Links.linkingTo().self(uriInfo.getAbsolutePath().toASCIIString()).build();
    return new HalRepresentation(links, Embedded.embedded("resubmit", embedded));
  }

  private ResubmitDto createDto(UriInfo uriInfo, String issueTracker, Collection<QueuedComment> comments) {
    return new ResubmitDto(createLinks(uriInfo, issueTracker), issueTracker, comments.size(), action.isInProgress());
  }

  private Links createLinks(UriInfo uriInfo, String issueTracker) {
    return Links.linkingTo()
      .single(link(uriInfo, "resubmit", issueTracker))
      .single(link(uriInfo, "clear", issueTracker))
      .build();
  }

  private Link link(UriInfo uriInfo, String method, String issueTracker) {
    URI uri = uriInfo.getRequestUriBuilder().path(ResubmitResource.class, method).build(issueTracker);
    return Link.link(method, uri.toASCIIString());
  }

  @POST
  @Path("{issueTracker}/resubmit")
  @Operation(
    summary = "Resubmit pending comments",
    description = "Resubmit all pending comments for the given issue tracker.",
    tags = "Pull Request",
    operationId = "issue_tracker_resubmits_resubmit"
  )
  @ApiResponse(responseCode = "202", description = "accepted")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"resubmit\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response resubmit(@PathParam("issueTracker") String issueTracker) {
    action.resubmitAsync(issueTracker);
    return Response.accepted().build();
  }

  @POST
  @Path("{issueTracker}/clear")
  @Operation(
    summary = "Clear pending comments",
    description = "Clear all pending comments for the given issue tracker.",
    tags = "Pull Request",
    operationId = "issue_tracker_resubmits_clear"
  )
  @ApiResponse(responseCode = "204", description = "no content")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"resubmit\" privilege")
  @ApiResponse(responseCode = "404", description = "not found, no pull request with the specified id is available")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response clear(@PathParam("issueTracker") String issueTracker) {
    queue.clear(issueTracker);
    return Response.accepted().build();
  }

}
