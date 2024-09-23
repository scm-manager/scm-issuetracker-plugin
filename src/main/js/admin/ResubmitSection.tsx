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
import { Link } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { Resubmit, ResubmitCollection, useResubmits } from "./hooks";
import { Button, Notification, ErrorNotification, Loading } from "@scm-manager/ui-components";
import styled from "styled-components";

type ActionProps = {
  label: string;
  linkName: string;
  color: string;
  resubmit: Resubmit;
  actionDispatcher: (link: string) => void;
};

const Action: FC<ActionProps> = ({ label, linkName, color, resubmit, actionDispatcher }) => {
  const link = resubmit._links[linkName] as Link;
  if (!link) {
    return null;
  }
  return (
    <Button color={color} action={e => actionDispatcher(link.href)} disabled={resubmit.inProgress}>
      {label}
    </Button>
  );
};

type ResubmitTableProps = {
  data?: ResubmitCollection;
  actionDispatcher: (link: string) => void;
};

const TextTd = styled.td`
  vertical-align: middle !important;
`;

const ActionTd = styled.td`
  width: 5rem;
`;

const ResubmitTable: FC<ResubmitTableProps> = ({ data, actionDispatcher }) => {
  const [t] = useTranslation("plugins");
  if ((data?._embedded?.resubmit || []).length === 0) {
    return <Notification type="info">{t("scm-issuetracker-plugin.resubmit.empty")}</Notification>;
  }
  return (
    <table className="table is-fullwidth">
      <thead>
        <tr>
          <th>{t("scm-issuetracker-plugin.resubmit.issueTracker")}</th>
          <th>{t("scm-issuetracker-plugin.resubmit.queueCount")}</th>
          <th />
          <th />
        </tr>
      </thead>
      <tbody>
        {data?._embedded.resubmit?.map(resubmit => (
          <tr className="border-is-green">
            <TextTd>{resubmit.issueTracker}</TextTd>
            <TextTd>{resubmit.queueSize}</TextTd>
            <ActionTd>
              <Action
                label={t("scm-issuetracker-plugin.resubmit.resubmit")}
                linkName={"resubmit"}
                resubmit={resubmit}
                color="warning"
                actionDispatcher={actionDispatcher}
              />
            </ActionTd>
            <ActionTd>
              <Action
                label={t("scm-issuetracker-plugin.resubmit.clear")}
                linkName={"clear"}
                resubmit={resubmit}
                color="danger"
                actionDispatcher={actionDispatcher}
              />
            </ActionTd>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

type Props = {
  link: Link;
};

const ResubmitSection: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const { data, isLoading, error, actionDispatcher } = useResubmits(link.href);
  return (
    <div className="content">
      <h3>{t("scm-issuetracker-plugin.resubmit.title")}</h3>
      <p>{t("scm-issuetracker-plugin.resubmit.description")}</p>
      <ErrorNotification error={error} />
      {isLoading ? <Loading /> : <ResubmitTable data={data} actionDispatcher={actionDispatcher} />}
    </div>
  );
};

export default ResubmitSection;
