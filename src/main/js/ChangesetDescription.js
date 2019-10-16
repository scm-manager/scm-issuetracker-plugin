// @flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";

type Issue = {
  name: string,
  href: string
};

export const replaceKeysWithLinks = (value: string, issues: Issue[]) => {
  if (!value || !issues) {
    return value;
  }

  let resultArray = [];
  const issueMap = createIssueMap(issues);

  const parts = value.split(" ");

  for (let i = 0; i < parts.length; i++) {
    let part = parts[i];

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
  return <a href={issue.href} target={"_blank"} key={issue.name}>{issue.name}</a>;
};

type Props = {
  changeset: Changeset,
  value: string
};

export default class ChangesetDescription extends React.Component<Props> {
  render() {
    const { value, changeset } = this.props;
    return <>{replaceKeysWithLinks(value, changeset._links.issues)}</>;
  }
}
