/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Subtitle, Title } from "@scm-manager/ui-components";
import { Link } from "@scm-manager/ui-types";
import ResubmitSection from "./ResubmitSection";
import ResubmitConfigurationSection from "./ResubmitConfigurationSection";

type Props = {
  links: Link[];
};

const AdminPage: FC<Props> = ({ links }) => {
  const [t] = useTranslation("plugins");
  const resubmitConfigurationLink = links.find(l => l.name === "resubmitConfiguration");
  const resubmitLink = links.find(l => l.name === "resubmit");
  return (
    <>
      <Title title={t("scm-issuetracker-plugin.title")} />
      <Subtitle subtitle={t("scm-issuetracker-plugin.subtitle")} />
      {resubmitConfigurationLink ? <ResubmitConfigurationSection link={resubmitConfigurationLink} /> : null}
      {resubmitLink ? <ResubmitSection link={resubmitLink} /> : null}
    </>
  );
};

export default AdminPage;
