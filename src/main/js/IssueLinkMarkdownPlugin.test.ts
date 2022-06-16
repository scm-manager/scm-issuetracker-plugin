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
  const test = (issues, content, expectedChildren) => {
    const halObject = {
      _links: {}
    };
    if (issues) {
      halObject._links.issues = issues;
    }
    const node = { value: content };
    const parent = { type: "text", children: [node] };
    const plugin = IssueLinkMarkdownPlugin({ halObject });
    const visit = (type, visitor) => visitor(node, 0, parent);
    plugin({ visit });
    expect(parent.children).toHaveLength(1);
    expect(parent.children[0].children).toHaveLength(expectedChildren.length);
    expect(parent.children[0].children).toEqual(expectedChildren);
  };

  it("should replace issue ids with links #1", () => {
    const issues = [
      {
        name: "#22",
        href: "https://hitchhiker.com/issues/22"
      }
    ] as Issue[];
    const content = "I am a description of issue #22. This issue, (#22) is awesome. Lets see more #22s";
    const expected = [
      { type: "text", value: "I am a description of issue " },
      {
        children: [{ type: "text", value: "#22" }],
        title: "Issue #22",
        type: "link",
        url: "https://hitchhiker.com/issues/22"
      },
      { type: "text", value: ". This issue, (" },
      {
        children: [{ type: "text", value: "#22" }],
        title: "Issue #22",
        type: "link",
        url: "https://hitchhiker.com/issues/22"
      },
      { type: "text", value: ") is awesome. Lets see more " },
      {
        children: [{ type: "text", value: "#22" }],
        title: "Issue #22",
        type: "link",
        url: "https://hitchhiker.com/issues/22"
      },
      { type: "text", value: "s" }
    ];
    test(issues, content, expected);
  });
  it("should replace issue ids with links #2.1", () => {
    const issues = [
      {
        name: "#1",
        href: "https://hitchhiker.com/issues/1"
      },
      {
        name: "#2",
        href: "https://hitchhiker.com/issues/2"
      }
    ] as Issue[];
    const content = "Something on #1 should work as described in #2 but it does not look as int ";
    const expected = [
      { type: "text", value: "Something on " },
      {
        children: [{ type: "text", value: "#1" }],
        title: "Issue #1",
        type: "link",
        url: "https://hitchhiker.com/issues/1"
      },
      { type: "text", value: " should work as described in " },
      {
        children: [{ type: "text", value: "#2" }],
        title: "Issue #2",
        type: "link",
        url: "https://hitchhiker.com/issues/2"
      },
      { type: "text", value: " but it does not look as int " }
    ];
    test(issues, content, expected);
  });
  it("should replace issue ids with links #2.2", () => {
    const issues = [
      {
        name: "#1",
        href: "https://hitchhiker.com/issues/1"
      },
      {
        name: "#2",
        href: "https://hitchhiker.com/issues/2"
      }
    ] as Issue[];
    const content = "Something on #2 should work as described in #1 but it does not look as int ";
    const expected = [
      { type: "text", value: "Something on " },
      {
        children: [{ type: "text", value: "#2" }],
        title: "Issue #2",
        type: "link",
        url: "https://hitchhiker.com/issues/2"
      },
      { type: "text", value: " should work as described in " },
      {
        children: [{ type: "text", value: "#1" }],
        title: "Issue #1",
        type: "link",
        url: "https://hitchhiker.com/issues/1"
      },
      { type: "text", value: " but it does not look as int " }
    ];
    test(issues, content, expected);
  });
  it("should replace issue ids with links #3", () => {
    const issues = [
      {
        name: "#1",
        href: "https://hitchhiker.com/issues/1"
      },
      {
        name: "#2",
        href: "https://hitchhiker.com/issues/2"
      }
    ] as Issue[];
    const content = "More luck in a heading? #1";
    const expected = [
      { type: "text", value: "More luck in a heading? " },
      {
        children: [{ type: "text", value: "#1" }],
        title: "Issue #1",
        type: "link",
        url: "https://hitchhiker.com/issues/1"
      }
    ];
    test(issues, content, expected);
  });
  it("should replace issue ids with links #4.1", () => {
    const issues = [
      {
        name: "#2",
        href: "https://hitchhiker.com/issues/2"
      },
      {
        name: "#21",
        href: "https://hitchhiker.com/issues/21"
      }
    ] as Issue[];
    const content = "Lets try #2 and #21 together";
    const expected = [
      { type: "text", value: "Lets try " },
      {
        children: [{ type: "text", value: "#2" }],
        title: "Issue #2",
        type: "link",
        url: "https://hitchhiker.com/issues/2"
      },
      { type: "text", value: " and " },
      {
        children: [{ type: "text", value: "#21" }],
        title: "Issue #21",
        type: "link",
        url: "https://hitchhiker.com/issues/21"
      },
      { type: "text", value: " together" }
    ];
    test(issues, content, expected);
  });
  it("should replace issue ids with links #4.2", () => {
    const issues = [
      {
        name: "#2",
        href: "https://hitchhiker.com/issues/2"
      },
      {
        name: "#234",
        href: "https://hitchhiker.com/issues/234"
      },
      {
        name: "#21",
        href: "https://hitchhiker.com/issues/21"
      }
    ] as Issue[];
    const content = "What about #2, #234 and (#21) OR [#234]";
    const expected = [
      { type: "text", value: "What about " },
      {
        children: [{ type: "text", value: "#2" }],
        title: "Issue #2",
        type: "link",
        url: "https://hitchhiker.com/issues/2"
      },
      { type: "text", value: ", " },
      {
        children: [{ type: "text", value: "#234" }],
        title: "Issue #234",
        type: "link",
        url: "https://hitchhiker.com/issues/234"
      },
      { type: "text", value: " and (" },
      {
        children: [{ type: "text", value: "#21" }],
        title: "Issue #21",
        type: "link",
        url: "https://hitchhiker.com/issues/21"
      },
      { type: "text", value: ") OR [" },
      {
        children: [{ type: "text", value: "#234" }],
        title: "Issue #234",
        type: "link",
        url: "https://hitchhiker.com/issues/234"
      },
      { type: "text", value: "]" }
    ];
    test(issues, content, expected);
  });
  it("should do nothing if there are no issue links", () => {
    const halObject = {
      _links: {}
    };
    const content = "I am a description of issue #22. This issue, (#22) is awesome. Lets see more #22s.";
    const node = { value: content };
    const parent = { type: "text", children: [node] };
    const plugin = IssueLinkMarkdownPlugin({ halObject });
    const visit = (type, visitor) => visitor(node, 0, parent);
    plugin({ visit });
    expect(parent.children).toHaveLength(1);
    expect(parent.children[0]).toEqual(node);
  });
  it("should do nothing if the issue links are empty", () => {
    const halObject = {
      _links: {
        issues: []
      }
    };
    const content = "I am a description of issue #22. This issue, (#22) is awesome. Lets see more #22s.";
    const node = { value: content };
    const parent = { type: "text", children: [node] };
    const plugin = IssueLinkMarkdownPlugin({ halObject });
    const visit = (type, visitor) => visitor(node, 0, parent);
    plugin({ visit });
    expect(parent.children).toHaveLength(1);
    expect(parent.children[0]).toEqual(node);
  });
});
