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
import sonia.scm.issuetracker.internal.Permissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResubmitResource {

  @VisibleForTesting
  static final String MEDIA_TYPE_RESUBMIT = VndMediaType.PREFIX + "issueTrackerResubmit" + VndMediaType.SUFFIX;
  @VisibleForTesting
  static final String MEDIA_TYPE_RESUBMIT_CONFIG = VndMediaType.PREFIX + "issueTrackerResubmitConfig" + VndMediaType.SUFFIX;

  private final ResubmitQueue queue;
  private final ResubmitDispatcher action;
  private final ResubmitConfigurationStore resubmitConfigurationStore;

  @Inject
  public ResubmitResource(ResubmitQueue queue, ResubmitDispatcher action, ResubmitConfigurationStore resubmitConfigurationStore) {
    this.queue = queue;
    this.action = action;
    this.resubmitConfigurationStore = resubmitConfigurationStore;
  }

  @GET
  @Path("config")
  @Produces(MEDIA_TYPE_RESUBMIT_CONFIG)
  @Operation(
    summary = "Get configuration",
    description = "Returns issue tracker resubmit configuration.",
    tags = "Issue Tracker",
    operationId = "issue_tracker_resubmit_configuration"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE_RESUBMIT,
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
  public ResubmitConfigurationDto getConfiguration(@Context UriInfo info) {
    Permissions.checkResubmit();

    ResubmitConfiguration resubmitConfiguration = resubmitConfigurationStore.get();
    String requestedUri = info.getRequestUriBuilder().build().toASCIIString();
    Links links = Links.linkingTo().self(requestedUri)
      .single(Link.link("update", requestedUri))
      .build();

    return new ResubmitConfigurationDto(links, resubmitConfiguration.getAddresses());
  }

  @PUT
  @Path("config")
  @Produces(MEDIA_TYPE_RESUBMIT_CONFIG)
  @Operation(
    summary = "Update resubmit configuration",
    description = "Update issue tracker resubmit configuration.",
    tags = "Issue Tracker",
    operationId = "update_issue_tracker_resubmit_configuration"
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
  public Response setConfiguration(@Valid ResubmitConfigurationDto dto) {
    ResubmitConfiguration resubmitConfiguration = new ResubmitConfiguration();
    resubmitConfiguration.setAddresses(dto.getAddresses());
    resubmitConfigurationStore.set(resubmitConfiguration);
    return Response.noContent().build();
  }

  @GET
  @Path("")
  @Produces(MEDIA_TYPE_RESUBMIT)
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
      mediaType = MEDIA_TYPE_RESUBMIT,
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
    return createDto(uriInfo, issueTracker, comments.size());
  }

  private ResubmitDto createDto(UriInfo uriInfo, String issueTracker, int size) {
    return new ResubmitDto(createLinks(uriInfo, issueTracker), issueTracker, size, action.isInProgress());
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
    tags = "Issue Tracker",
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
    tags = "Issue Tracker",
    operationId = "issue_tracker_resubmits_clear"
  )
  @ApiResponse(responseCode = "204", description = "no content")
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
  public Response clear(@PathParam("issueTracker") String issueTracker) {
    queue.clear(issueTracker);
    return Response.accepted().build();
  }

}
