// @flow

import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { ChangesetDescription } from "./ChangesetDescription";

type Issue = {
  name: string,
  href: string
};

export const replaceKeysWithLinks = (value: string, issues: Issue[]) => {
  if (!value) {
    return;
  }

  let resultArray = [];
  const issueMap = createIssueMap(issues);

  const parts = value.split(" ");

  for (let i = 0; i < parts.length; i++) {
    let part = parts[i];

    const issue = issueMap[part];
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
    issueMap[issue.name] = issue;
  }
  return issueMap;
};

export const createLink = (issue: Issue) => {
  return <a href={issue.href}>{issue.name}</a>;
};

binder.bind("changesets.changeset.description", ChangesetDescription);
