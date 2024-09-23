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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.store.DataStore;

class ProcessedStore {

  private final DataStore<ProcessedMarks> store;

  ProcessedStore(DataStore<ProcessedMarks> store) {
    this.store = store;
  }

  public boolean isProcessed(String issueKey, IssueReferencingObject object) {
    return getMarks(issueKey).contains(mark(object));
  }

  private ProcessedMarks.Mark mark(IssueReferencingObject object) {
    return new ProcessedMarks.Mark(object.getType(), object.getId());
  }

  public boolean isProcessed(String issueKey, IssueReferencingObject object, String keyWord) {
    return getMarks(issueKey).contains(mark(object, keyWord));
  }

  private ProcessedMarks.Mark mark(IssueReferencingObject object, String keyWord) {
    return new ProcessedMarks.Mark(object.getType(), object.getId(), keyWord);
  }

  public void mark(String issueKey, IssueReferencingObject object) {
    ProcessedMarks marks = getMarks(issueKey);
    marks.add(mark(object));
    store(issueKey, marks);
  }

  private void store(String issueKey, ProcessedMarks marks) {
    store.put(Issues.normalize(issueKey), marks);
  }

  public void mark(String issueKey, IssueReferencingObject object, String keyWord) {
    ProcessedMarks marks = getMarks(issueKey);
    marks.add(mark(object, keyWord));
    store(issueKey, marks);
  }

  private ProcessedMarks getMarks(String issueKey) {
    ProcessedMarks marks = store.get(Issues.normalize(issueKey));
    if (marks == null) {
      return new ProcessedMarks();
    }
    return marks;
  }
}
