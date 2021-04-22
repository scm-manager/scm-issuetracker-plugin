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
