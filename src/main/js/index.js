// @flow

import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import type { Changeset } from "@scm-manager/ui-types";
import { escape } from "lodash";


type Issue = {
  name: string,
  href: string
}

const ChangesetDescription = ({ changeset, description }) => {

  return (
    <p>
      {description.message.split("\n").map((item, key) => {
        return renderSpan(changeset, key, item);
      })}
    </p>
  );
};

const renderSpan = (changeset: Changeset, key: string, item: string) => {
  if (changeset._links.issues) {
    return (
      <span
        key={key}
      >
        {React.createElement(React.Fragment, {}, ...replaceKeysWithLinks(item, changeset._links.issues))}
      </span>
    );
  }

  return (
    <span key={key}>
      {item}
      <br />
    </span>
  );
};

export const replaceKeysWithLinks = (line: string, issues: Issue[]) => {
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

export const createLink = (issue: Issue) => {
  return <a href={issue.href}>{issue.name}</a>;
};

binder.bind("changesets.changeset.description", ChangesetDescription);
