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

import { useEffect, useState } from "react";
import { HalRepresentation } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";

export type ResubmitCollection = HalRepresentation & {
  _embedded: {
    resubmit?: Resubmit[];
  };
};

export type Resubmit = HalRepresentation & {
  issueTracker: string;
  queueSize: number;
  inProgress: boolean;
};

export const useResubmits = (link: string) => {
  const [counter, setCounter] = useState(0);
  const [data, setData] = useState<ResubmitCollection>();
  const [isLoading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error>();

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(link)
      .then(response => response.json())
      .then(collection => {
        setData(collection);
        setError(undefined);
      })
      .catch(e => {
        setData(undefined);
        setError(e);
      })
      .finally(() => setLoading(false));
  }, [link, counter]);

  const actionDispatcher = (actionLink: string) => {
    setLoading(true);
    apiClient
      .post(actionLink)
      .then(() => {
        setCounter(counter + 1);
      })
      .catch(e => {
        setError(e);
        setLoading(false);
      });
  };

  return {
    data,
    isLoading,
    error,
    actionDispatcher
  };
};

export type ResubmitConfiguration = HalRepresentation & {
  addresses: string[];
};

export const useResubmitConfiguration = (link: string) => {
  const [configuration, setConfiguration] = useState<ResubmitConfiguration>();
  const [isLoading, setLoading] = useState(true);
  const [error, setError] = useState<Error>();

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(link)
      .then(response => response.json())
      .then(cfg => {
        setConfiguration(cfg);
        setError(undefined);
      })
      .catch(e => {
        setConfiguration(undefined);
        setError(e);
      })
      .finally(() => setLoading(false));
  }, [link]);

  return {
    configuration,
    isLoading,
    error
  };
};

const MEDIA_TYPE_CONFIG = "application/vnd.scmm-issueTrackerResubmitConfig+json;v=2";

export const useResubmitConfigurationMutation = (link?: string) => {
  const [isLoading, setLoading] = useState(false);
  const [error, setError] = useState<Error>();
  const [updated, setUpdated] = useState(false);

  const mutate = link
    ? (config: ResubmitConfiguration) => {
        setLoading(true);
        setUpdated(false);
        apiClient
          .put(link, config, MEDIA_TYPE_CONFIG)
          .then(() => {
            setUpdated(true);
            setError(undefined);
          })
          .catch(setError)
          .finally(() => setLoading(false));
      }
    : undefined;

  return {
    isLoading,
    error,
    mutate,
    updated
  };
};
