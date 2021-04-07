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
