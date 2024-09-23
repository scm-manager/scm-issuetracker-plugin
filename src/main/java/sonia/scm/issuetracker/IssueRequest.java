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

package sonia.scm.issuetracker;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import java.util.List;
import java.util.Optional;

/**
 *
 * The detected issue keys related to a repository and a changeset
 *
 * @author Sebastian Sdorra
 * @deprecated
 */
@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@Deprecated
public final class IssueRequest {
    private Repository repository;
    private Changeset changeset;
    private List<String> issueKeys;
    private Optional<User> committer;
}
