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

import { binder } from "@scm-manager/ui-extensions";
import replaceIssueKeys from "./replaceIssueKeys";
import IssueLinkMarkdownPlugin from "./IssueLinkMarkdownPlugin";
import IssueTrackerRoute from "./admin/IssueTrackerRoute";
import { Links } from "@scm-manager/ui-types";
import IssueTrackerNavLink from "./admin/IssueTrackerNavLink";

type PredicateProps = {
  links: Links;
};

export const predicate = ({ links }: PredicateProps) => {
  return !!(links && links.issueTracker);
};

binder.bind("changeset.description.tokens", replaceIssueKeys);
binder.bind("reviewPlugin.pullrequest.title.tokens", replaceIssueKeys);
binder.bind("pullrequest.comment.plugins", IssueLinkMarkdownPlugin);
binder.bind("pullrequest.description.plugins", IssueLinkMarkdownPlugin);

binder.bind("admin.route", IssueTrackerRoute, predicate);
binder.bind("admin.navigation", IssueTrackerNavLink, predicate);
