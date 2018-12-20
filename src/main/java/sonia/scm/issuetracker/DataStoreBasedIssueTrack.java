/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.issuetracker;

//~--- non-JDK imports --------------------------------------------------------

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
 */
public abstract class DataStoreBasedIssueTrack extends IssueTracker
{

  /**
   * the logger for DataStoreBasedIssueTrack
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DataStoreBasedIssueTrack.class);


  private DataStoreFactory storeFactory;

  public DataStoreBasedIssueTrack(String name, DataStoreFactory storeFactory)
  {
    super(name);
    this.storeFactory = storeFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   */
  @Override
  public void markAsHandled(Repository repository, Changeset changeset)
  {
    logger.debug("mark changeset {} of repository {} as handled",
      changeset.getId(), repository.getId());

    IssueData data = getData(repository);

    data.getHandledChangesets().add(changeset.getId());
    DataStore<IssueData> dataStore = storeFactory.withType(IssueData.class).withName(super.getName()).forRepository(repository).build();

    dataStore.put(repository.getId(), data);
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  @Override
  public void removeHandledMarks(Repository repository)
  {
    logger.info("remove handled marks from store {}", repository.getId());
    DataStore<IssueData> dataStore = storeFactory.withType(IssueData.class).withName(super.getName()).forRepository(repository).build();
    dataStore.remove(repository.getId());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   *
   * @return
   */
  @Override
  public boolean isHandled(Repository repository, Changeset changeset)
  {
    IssueData data = getData(repository);

    return data.getHandledChangesets().contains(changeset.getId());
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private IssueData getData(Repository repository)
  {
    DataStore<IssueData> dataStore = storeFactory.withType(IssueData.class).withName(super.getName()).forRepository(repository).build();

    IssueData data = dataStore.get(repository.getId());

    if (data == null)
    {
      data = new IssueData();
    }

    return data;
  }


}
