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
import { mount } from "enzyme";
import "@scm-manager/ui-tests/enzyme";
import { replaceKeysWithLinks } from "./ChangesetDescription";
import ChangesetDescription from "./ChangesetDescription";

describe("Changeset description", () => {
  it("should replace issue names with links", () => {
    const issues = [
      {
        name: "#123",
        href: "http://foo.bar"
      },
      {
        name: "#456",
        href: "http://h2g2.com"
      }
    ];

    const line = "get #123 blabla #456 foofoo #123 #456";

    const result = replaceKeysWithLinks(line, issues);

    const expectedResult = [
      "get",
      " ",
      <a href="http://foo.bar" target="_blank" key="#123">
        #123
      </a>,
      " ",
      "blabla",
      " ",
      <a href="http://h2g2.com" target="_blank" key="#456">
        #456
      </a>,
      " ",
      "foofoo",
      " ",
      <a href="http://foo.bar" target="_blank" key="#123">
        #123
      </a>,
      " ",
      <a href="http://h2g2.com" target="_blank" key="#456">
        #456
      </a>
    ];

    expect(result).toEqual(expectedResult);
  });

  it("should render replaced issue links", () => {
    const changeset = {
      _links: {
        issues: [
          {
            name: "#123",
            href: "http://abc.de"
          },
          {
            name: "#456",
            href: "http://fgh.ij"
          },
          {
            name: "#789",
            href: "http://klm.no"
          }
        ]
      }
    };

    const links = [
      '<a href="http://abc.de" target="_blank">#123</a>',
      '<a href="http://fgh.ij" target="_blank">#456</a>',
      '<a href="http://klm.no" target="_blank">#789</a>'
    ];

    const rendered = mount(
      <div>
        <ChangesetDescription changeset={changeset} value={"get #123 blabla #456 foo #789"} />
      </div>
    );
    for (const l of links) {
      expect(rendered.html()).toContain(l);
    }
  });

  it("should not replace anything if there are no issue-links", () => {
    const changeset = {
      _links: {
        foo: []
      }
    };
    const rendered = mount(
      <div>
        <ChangesetDescription changeset={changeset} value={"#123 blabla #456 foo #789"} />
      </div>
    );

    expect(rendered.html()).not.toContain("<a ");
  });
});
