// @flow
import React from "react";
import replaceKeysWithLinks from './replaceKeysWithLinks';

describe("Issue tracker plugin", () => {
  it("should replace issue names with links", () => {
    const issues = [
      { name: "#123", href: "foo.bar" },
      { name: "#456", href: "h2g2.com" }
    ];

    const line = "#123 blabla #456 foofoo #123 #456";

    const result = replaceKeysWithLinks(line, issues);

    const expectedResult = [
      <a href="foo.bar">#123</a>,
      " ",
      "blabla",
      " ",
      <a href="h2g2.com">#456</a>,
      " ",
      "foofoo",
      " ",
      <a href="foo.bar">#123</a>,
      " ",
      <a href="h2g2.com">#456</a>
    ];

    expect(result).toEqual(expectedResult);
  });
});
