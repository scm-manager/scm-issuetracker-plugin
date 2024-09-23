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

package sonia.scm.issuetracker.internal.resubmit;

import sonia.scm.issuetracker.internal.Permissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ResubmitConfigurationStore {

  private static final String STORE_NAME = "issue-tracker-resubmit-notification";

  private final ConfigurationStore<ResubmitConfiguration> store;

  @Inject
  public ResubmitConfigurationStore(ConfigurationStoreFactory storeFactory) {
    this.store = storeFactory.withType(ResubmitConfiguration.class).withName(STORE_NAME).build();
  }

  public ResubmitConfiguration get() {
    return store.getOptional().orElse(new ResubmitConfiguration());
  }

  public void set(ResubmitConfiguration configuration) {
    Permissions.checkResubmit();
    store.set(configuration);
  }

}
