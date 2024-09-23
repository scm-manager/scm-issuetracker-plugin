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

package sonia.scm.issuetracker.api;

import lombok.Value;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract model for handling issue references. An {@link IssueReferencingObject} can be build for a changeset,
 * a pull request comment or what ever fits into the model. You can get this object with {@link #getOrigin()}.
 *
 * @since 3.0.0
 */
@Value
public class IssueReferencingObject {

  Repository repository;
  String type;
  String id;
  Person author;
  Map<String, Collection<Person>> contributors;
  Instant date;
  List<Content> content;
  String link;
  boolean triggeringStateChange;
  Object origin;

}
