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

package sonia.scm.issuetracker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.issuetracker.internal.IssueData;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

/**
 *
 * @author Sebastian Sdorra
 * @deprecated use {@link sonia.scm.issuetracker.spi.IssueTrackerProvider} instead.
 */
@Deprecated
public abstract class DataStoreBasedIssueTracker extends IssueTracker {

  private static final Logger logger =
    LoggerFactory.getLogger(DataStoreBasedIssueTracker.class);


  private DataStoreFactory storeFactory;

  public DataStoreBasedIssueTracker(String name, DataStoreFactory storeFactory) {
    super(name);
    this.storeFactory = storeFactory;
  }

  @Override
  public void markAsHandled(Repository repository, Changeset changeset) {
    logger.debug("mark changeset {} of repository {} as handled",
      changeset.getId(), repository.getId());

    IssueData data = getData(repository);

    data.getHandledChangesets().add(changeset.getId());
    DataStore<IssueData> dataStore = createDatastore(repository);

    dataStore.put(repository.getId(), data);
  }

  private DataStore<IssueData> createDatastore(Repository repository) {
    return storeFactory.withType(IssueData.class)
      .withName(super.getName())
      .forRepository(repository).build();
  }

  @Override
  public void removeHandledMarks(Repository repository) {
    logger.info("remove handled marks from store {}", repository.getId());
    DataStore<IssueData> dataStore = createDatastore(repository);
    dataStore.remove(repository.getId());
  }

  @Override
  public boolean isHandled(Repository repository, Changeset changeset) {
    IssueData data = getData(repository);

    return data.getHandledChangesets().contains(changeset.getId());
  }


  private IssueData getData(Repository repository) {
    DataStore<IssueData> dataStore = createDatastore(repository);

    IssueData data = dataStore.get(repository.getId());

    if (data == null) {
      data = new IssueData();
    }

    return data;
  }


}
