import React from "react";
import { mount } from "enzyme";
import "./testing/enzyme";
import { replaceKeysWithLinks } from "./ChangesetDescription";
import ChangesetDescription from "./ChangesetDescription";

describe("Changeset description", () => {
  it("should replace issue names with links", () => {
    const issues = [
      { name: "#123", href: "http://foo.bar" },
      { name: "#456", href: "http://h2g2.com" }
    ];

    const line = "get #123 blabla #456 foofoo #123 #456";

    const result = replaceKeysWithLinks(line, issues);

    const expectedResult = [
      "get",
      " ",
      <a href="http://foo.bar" target="_blank">
        #123
      </a>,
      " ",
      "blabla",
      " ",
      <a href="http://h2g2.com" target="_blank">
        #456
      </a>,
      " ",
      "foofoo",
      " ",
      <a href="http://foo.bar" target="_blank">
        #123
      </a>,
      " ",
      <a href="http://h2g2.com" target="_blank">
        #456
      </a>
    ];

    expect(result).toEqual(expectedResult);
  });

  it("should render replaced issue links", () => {
    const changeset = {
      _links: {
        issues: [
          { name: "#123", href: "http://abc.de" },
          { name: "#456", href: "http://fgh.ij" },
          { name: "#789", href: "http://klm.no" }
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
        <ChangesetDescription
          changeset={changeset}
          value={"get #123 blabla #456 foo #789"}
        />
      </div>
    );
    console.log(rendered.html());
    for (let l of links) {
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
        <ChangesetDescription
          changeset={changeset}
          value={"#123 blabla #456 foo #789"}
        />
      </div>
    );

    expect(rendered.html()).not.toContain("<a ");
  });
});
