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
import { Changeset } from "@scm-manager/ui-types";

type Issue = {
  name: string;
  href: string;
};

export const replaceKeysWithLinks = (value: string, issues: Issue[]) => {
  if (!value || !issues) {
    return value;
  }

  const resultArray = [];
  const issueMap = createIssueMap(issues);

  const parts = value.split(" ");

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i];

    const issue = issueMap.get(part);
    if (issue) {
      resultArray.push(createLink(issue));
    } else {
      resultArray.push(part);
    }
    if (i < parts.length - 1) {
      resultArray.push(" ");
    }
  }
  return resultArray;
};

const createIssueMap = (issues: Issue[]) => {
  const issueMap = new Map();
  for (const issue of issues) {
    issueMap.set(issue.name, issue);
  }
  return issueMap;
};

export const createLink = (issue: Issue) => {
  return (
    <a href={issue.href} target={"_blank"} key={issue.name}>
      {issue.name}
    </a>
  );
};

type Props = {
  changeset: Changeset;
  value: string;
};

export default class ChangesetDescription extends React.Component<Props> {
  render() {
    const { value, changeset } = this.props;
    return <>{replaceKeysWithLinks(value, changeset._links.issues)}</>;
  }
}
