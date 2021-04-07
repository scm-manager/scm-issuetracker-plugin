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
