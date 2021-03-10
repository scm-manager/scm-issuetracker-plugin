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

import { AstPlugin } from "@scm-manager/ui-components";
import { HalRepresentation } from "@scm-manager/ui-types";
import { Issue } from "./types";

type IssueLinkMarkdownPluginOptions = { halObject: HalRepresentation };
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
      let nodeText = node.value as string;
      if (issues.length > 0) {
        const children = [];
        for (const { href, name } of issues) {
          let idx: number;
          while ((idx = nodeText.indexOf(name)) !== -1) {
            if (idx > 0) {
              children.push({
                type: "text",
                value: nodeText.substring(0, idx)
              });
            }

            children.push({
              type: "link",
              url: href,
              title: name,
              children: [
                {
                  type: "text",
                  value: name
                }
              ]
            });

            nodeText = nodeText.substring(idx + name.length);
          }

          if (nodeText.length > 0) {
            children.push({
              type: "text",
              value: nodeText
            });
          }

          parent.children[index] = {
            type: "text",
            children
          };
        }
      }
    });
  };
}
