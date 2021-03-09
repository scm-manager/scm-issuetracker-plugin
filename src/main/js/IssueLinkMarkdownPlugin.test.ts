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

import { Issue } from "./types";
import IssueLinkMarkdownPlugin from "./IssueLinkMarkdownPlugin";

describe("IssueLinkMarkdownPlugin tests", () => {
  it("should replace issue ids with links", () => {
    const halObject = {
      _links: {
        issues: [
          {
            name: "#22",
            href: "https://hitchhiker.com/issues/22"
          }
        ] as Issue[]
      }
    };
    const content = "I am a description of issue #22. This issue, (#22) is awesome. Lets see more #22s.";
    const node = { value: content };
    const parent = { type: "text", children: [] as Node[] };
    const plugin = IssueLinkMarkdownPlugin({ halObject });
    const visit = (type, visitor) => visitor(node, 0, parent);
    plugin({ visit });
    expect(parent.children).toHaveLength(1);
    expect(parent.children[0].children).toHaveLength(7);
    expect(parent.children[0].children).toEqual([
      { type: "text", value: "I am a description of issue " },
      {
        children: [{ type: "text", value: "#22" }],
        title: "#22",
        type: "link",
        url: "https://hitchhiker.com/issues/22"
      },
      { type: "text", value: ". This issue, (" },
      {
        children: [{ type: "text", value: "#22" }],
        title: "#22",
        type: "link",
        url: "https://hitchhiker.com/issues/22"
      },
      { type: "text", value: ") is awesome. Lets see more " },
      {
        children: [{ type: "text", value: "#22" }],
        title: "#22",
        type: "link",
        url: "https://hitchhiker.com/issues/22"
      },
      { type: "text", value: "s." }
    ]);
  });
});
