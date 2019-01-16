// @flow
import React from "react";
import { replaceKeysWithLinks } from "./index";

export const ChangesetDescription = ({ changeset, value }) => {

  return <>{replaceKeysWithLinks(value, changeset._links.issues)}</>;
};
