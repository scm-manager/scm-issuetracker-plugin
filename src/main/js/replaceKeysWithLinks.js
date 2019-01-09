// @flow
import React from "react";
import type {Issue} from './types';

export default function replaceKeysWithLinks(line: string, issues: Issue[]) {
  let resultArray = [];
  const issueMap = createIssueMap(issues);


  const parts = line.split(" ");
  for (let i = 0; i < parts.length; i++) {
    let part = parts[i];

    const issue = issueMap[part];
    if (issue) {
      resultArray.push(createLink(issue))
    } else {
      resultArray.push(part)
    }
    if (i < parts.length - 1) {
      resultArray.push(" ")
    }
  }
  return resultArray;
};

const createIssueMap = (issues: Issue[]) => {
  const issueMap = new Map();
  for (const issue of issues) {
    issueMap[issue.name] = issue
  }
  return issueMap;
};

const createLink = (issue: Issue) => {
  return <a href={issue.href}>{issue.name}</a>;
};
