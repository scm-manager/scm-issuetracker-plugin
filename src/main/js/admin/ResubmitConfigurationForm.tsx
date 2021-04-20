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

import React, { FC, useState, FormEvent } from "react";
import { ResubmitConfiguration, useResubmitConfigurationMutation } from "./hooks";
import { Link } from "@scm-manager/ui-types";
import {
  AddEntryToTableField,
  ErrorNotification,
  Icon,
  Notification,
  SubmitButton,
  validation
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type AddressRowProps = {
  address: string;
  readOnly: boolean;
  removeAddress: (address: string) => void;
};

const AddressRow: FC<AddressRowProps> = ({ address, readOnly, removeAddress }) => {
  const [t] = useTranslation("plugins");
  return (
    <tr>
      <td>{address}</td>
      <td className="is-darker">
        {readOnly ? null : (
          <a className="level-item" onClick={() => removeAddress(address)}>
            <span className="icon">
              <Icon name="trash" title={t("scm-issuetracker-plugin.resubmit.config.removeAddress")} color="inherit" />
            </span>
          </a>
        )}
      </td>
    </tr>
  );
};

type AddressTableProps = {
  addresses: string[];
  readOnly: boolean;
  removeAddress: (address: string) => void;
};

const AddressTable: FC<AddressTableProps> = ({ addresses, readOnly, removeAddress }) => {
  const [t] = useTranslation("plugins");
  if (!addresses || addresses.length === 0) {
    return null;
  }
  return (
    <table className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>{t("scm-issuetracker-plugin.resubmit.config.address")}</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {addresses.map(address => (
          <AddressRow address={address} readOnly={readOnly} removeAddress={removeAddress} />
        ))}
      </tbody>
    </table>
  );
};

const UpdatedNotification: FC = () => {
  const [t] = useTranslation("plugins");
  return <Notification type="success">{t("scm-issuetracker-plugin.resubmit.config.updated")}</Notification>;
};

type Props = {
  configuration: ResubmitConfiguration;
};

const ResubmitConfigurationForm: FC<Props> = ({ configuration }) => {
  const { mutate, updated, isLoading, error } = useResubmitConfigurationMutation(
    (configuration._links.update as Link)?.href
  );
  const [config, setConfig] = useState(configuration);
  const [t] = useTranslation("plugins");

  const addAddress = (address: string) => {
    // required because AddEntryToTableField does not validate on enter only onClick
    if (!validation.isMailValid(address)) {
      return;
    }
    if (config.addresses.indexOf(address) >= 0) {
      return;
    }

    setConfig({
      ...config,
      addresses: [...config.addresses, address]
    });
  };

  const removeAddress = (address: string) => {
    setConfig({
      ...config,
      addresses: [...config.addresses.filter(a => a !== address)]
    });
  };

  const doMutation = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (mutate) {
      mutate(config);
    }
  };

  return (
    <form onSubmit={doMutation} className="mb-5">
      <ErrorNotification error={error} />
      {updated ? <UpdatedNotification /> : null}
      <AddressTable addresses={config.addresses} readOnly={!mutate} removeAddress={removeAddress} />

      {mutate ? (
        <>
          {/* @ts-ignore disabled seems to be wrong implemented and it is required */}
          <AddEntryToTableField
            fieldLabel={t("scm-issuetracker-plugin.resubmit.config.address")}
            errorMessage={t("scm-issuetracker-plugin.resubmit.config.invalidMail")}
            validateEntry={validation.isMailValid}
            buttonLabel={t("scm-issuetracker-plugin.resubmit.config.addAddress")}
            addEntry={addAddress}
          />
        </>
      ) : null}
      <SubmitButton
        disabled={!mutate}
        loading={isLoading}
        label={t("scm-issuetracker-plugin.resubmit.config.submit")}
      />
    </form>
  );
};

export default ResubmitConfigurationForm;
