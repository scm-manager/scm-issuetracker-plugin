/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, { FC } from "react";
import { Link } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { Resubmit, ResubmitCollection, useResubmits } from "./hooks";
import { Button, Notification, ErrorNotification, Loading } from "@scm-manager/ui-components";

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
            <td>{resubmit.issueTracker}</td>
            <td>{resubmit.queueSize}</td>
            <td>
              <Action
                label={t("scm-issuetracker-plugin.resubmit.resubmit")}
                linkName={"resubmit"}
                resubmit={resubmit}
                color="warning"
                actionDispatcher={actionDispatcher}
              />
            </td>
            <td>
              <Action
                label={t("scm-issuetracker-plugin.resubmit.clear")}
                linkName={"clear"}
                resubmit={resubmit}
                color="danger"
                actionDispatcher={actionDispatcher}
              />
            </td>
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
      <h2>{t("scm-issuetracker-plugin.resubmit.title")}</h2>
      <p>{t("scm-issuetracker-plugin.resubmit.description")}</p>
      <ErrorNotification error={error} />
      {isLoading ? <Loading /> : <ResubmitTable data={data} actionDispatcher={actionDispatcher} />}
    </div>
  );
};

export default ResubmitSection;
