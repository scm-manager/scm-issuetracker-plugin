// @flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import replaceKeysWithLinks from "./replaceKeysWithLinks";

const renderSpan = (changeset: Changeset, key: string, item: string) => {
  if (changeset._links.issues) {
    return (
      <span key={key}>
        {React.createElement(
          React.Fragment,
          {},
          ...replaceKeysWithLinks(item, changeset._links.issues)
        )}
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

export default function ChangesetDescription({ changeset, description }) {
  return (
    <p>
      {description.message.split("\n").map((item, key) => {
        return renderSpan(changeset, key, item);
      })}
    </p>
  );
};
