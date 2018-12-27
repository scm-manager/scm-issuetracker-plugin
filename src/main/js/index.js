// @flow

import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import type { Changeset } from "@scm-manager/ui-types";

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
        dangerouslySetInnerHTML={{
          __html: replaceKeysWithLinks(item, changeset._links.issues) + "<br />" // TODO: Check whether this is really necessary
        }}
      />
    );
  }

  return (
    <span key={key}>
      {item}
      <br />
    </span>
  );
};

const replaceKeysWithLinks = (line: string, issues) => {
  let replacedString = line;
  for (let issue of issues) {
    const link = createLink(issue.name, issue.href);
    replacedString = replacedString.replace(issue.name, link);
  }
  return replacedString;
};

const createLink = (name: string, href: string) => {
  return `<a href=${href}>${name}</a>`;
};

binder.bind("changesets.changeset.description", ChangesetDescription);
