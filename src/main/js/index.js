// @flow

import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import ChangesetDescription from "./ChangesetDescription";

binder.bind("changesets.changeset.description", ChangesetDescription);
