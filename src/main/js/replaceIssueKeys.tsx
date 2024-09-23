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

import React from "react";
import { HalRepresentation } from "@scm-manager/ui-types";
import { Replacement, ExternalLink } from "@scm-manager/ui-components";

type Issue = {
  name: string;
  href: string;
};

const replaceIssueKeys: (object: HalRepresentation, value: string) => Replacement[] = (
  object: HalRepresentation,
  value: string
) => {
  const issues = object._links.issues as Issue[];
  if (!value || !issues) {
    return [];
  }
  const replacements: Replacement[] = [];
  for (const issue of issues) {
    replacements.push({
      textToReplace: issue.name,
      replacement: (
        <ExternalLink key={issue.name} to={issue.href}>
          {issue.name}
        </ExternalLink>
      )
    });
  }
  return replacements;
};

export default replaceIssueKeys;
