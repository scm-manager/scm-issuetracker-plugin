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
package sonia.scm.issuetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.issuetracker.internal.IssueData;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

public class DataStoreBasedIssueHandledTracker implements IssueHandledTracker {

  private static final Logger LOG = LoggerFactory.getLogger(DataStoreBasedIssueHandledTracker.class);

  private final String name;
  private final DataStoreFactory storeFactory;

  public DataStoreBasedIssueHandledTracker(String name, DataStoreFactory storeFactory) {
    this.name = name;
    this.storeFactory = storeFactory;
  }

  @Override
  public void markAsHandled(Repository repository, Changeset changeset) {
    LOG.debug("mark changeset {} of repository {} as handled", changeset.getId(), repository);

    IssueData data = getData(repository);

    data.addHandledChangeset(changeset.getId());
    DataStore<IssueData> dataStore = createDatastore(repository);

    dataStore.put(repository.getId(), data);
  }

  @Override
  public boolean isHandled(Repository repository, Changeset changeset) {
    IssueData data = getData(repository);

    return data.getHandledChangesets().contains(changeset.getId());
  }

  @Override
  public void markAsHandled(Repository repository, String dataKey) {
    LOG.debug("mark key {} of repository {} as handled", dataKey, repository);

    IssueData data = getData(repository);

    data.addHandledKey(dataKey);
    DataStore<IssueData> dataStore = createDatastore(repository);

    dataStore.put(repository.getId(), data);
  }

  @Override
  public void removeHandledMarks(Repository repository) {
    LOG.info("remove handled marks from store {}", repository);
    DataStore<IssueData> dataStore = createDatastore(repository);
    dataStore.remove(repository.getId());
  }

  private DataStore<IssueData> createDatastore(Repository repository) {
    return storeFactory.withType(IssueData.class)
      .withName(name)
      .forRepository(repository).build();
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
