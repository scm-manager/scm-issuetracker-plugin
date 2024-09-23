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

import { AstPlugin } from "@scm-manager/ui-components";
import { HalRepresentation } from "@scm-manager/ui-types";
import { Issue } from "./types";

type IssueLinkMarkdownPluginOptions = { halObject: HalRepresentation };
type FoundIndex = {
  idx: number;
  name: string;
  href: string;
};

function findIndicesOfIssues(issues: Issue[], nodeText: string) {
  issues.sort((a, b) => b.name.length - a.name.length);
  const foundIndices: FoundIndex[] = [];
  for (const { href, name } of issues) {
    let idx = nodeText.indexOf(name, 0);

    while (idx !== -1) {
      const safeIdx = idx;
      if (!foundIndices.some(it => it.idx === safeIdx)) {
        foundIndices.push({ idx, name, href });
      }
      idx = nodeText.indexOf(name, idx + 1);
    }
  }
  foundIndices.sort((a, b) => a.idx - b.idx);
  return foundIndices;
}

function buildChildrenNodes(foundIndices: FoundIndex[], nodeText: string) {
  const children = [];
  let lastIndex = 0;
  for (const { idx, name, href } of foundIndices) {
    if (idx > 0) {
      children.push({
        type: "text",
        value: nodeText.substring(lastIndex, idx)
      });
    }

    children.push({
      type: "link",
      url: href,
      title: `Issue ${name}`,
      children: [
        {
          type: "text",
          value: name
        }
      ]
    });

    lastIndex = idx + name.length;
  }

  if (lastIndex < nodeText.length) {
    children.push({
      type: "text",
      value: nodeText.substring(lastIndex)
    });
  }
  return children;
}

export default function IssueLinkMarkdownPlugin({ halObject }: IssueLinkMarkdownPluginOptions): AstPlugin {
  const issues = halObject._links.issues as Issue[];

  if (!Array.isArray(issues) || issues.length === 0) {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    return () => {};
  }

  return ({ visit }) => {
    visit("text", (node, index, parent) => {
      if (!parent || parent.type === "link" || !node.value) {
        return;
      }
      const nodeText = node.value as string;
      if (issues.length > 0) {
        const foundIndices = findIndicesOfIssues(issues, nodeText);
        const children = buildChildrenNodes(foundIndices, nodeText);

        parent.children[index] = {
          type: "text",
          children
        };
      }
    });
  };
}
