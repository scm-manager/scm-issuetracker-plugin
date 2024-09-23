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
import { Link } from "@scm-manager/ui-types";
import { useResubmitConfiguration } from "./hooks";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import ResubmitConfigurationForm from "./ResubmitConfigurationForm";

type Props = {
  link: Link;
};

const ResubmitConfigurationSection: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const { configuration, isLoading, error } = useResubmitConfiguration(link.href);

  return (
    <>
      <div className="content">
        <h3>{t("scm-issuetracker-plugin.resubmit.config.title")}</h3>
        <p>{t("scm-issuetracker-plugin.resubmit.config.description")}</p>
      </div>
      <ErrorNotification error={error} />
      {isLoading ? <Loading /> : null}
      {configuration ? <ResubmitConfigurationForm configuration={configuration} /> : null}
    </>
  );
};

export default ResubmitConfigurationSection;
