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

import sonia.scm.issuetracker.internal.ChangesetMapper;
import sonia.scm.issuetracker.internal.review.PullRequestCommentMapper;

import java.io.IOException;

/**
 * Changes states of issues.
 *
 * @since 3.0.0
 */
public interface StateChanger {

  /**
   * Change the state of the issue with the given issue key to the one which matches the given key word.
   *
   * @param issueKey key of the issue
   * @param keyWord matched key word that triggers the state change.
   *
   * @throws IOException if state could not be changed
   */
  void changeState(String issueKey, String keyWord) throws IOException;

  /**
   * Returns a collection of key words representing possible state transitions in the issue tracker system.
   *
   * @param issueKey key of the issue
   *
   * @return collection of key words
   *
   * @throws IOException if the list of keywords could not be fetched
   */
  Iterable<String> getKeyWords(String issueKey) throws IOException;

  /**
   * This will be called before a state change is performed for the given type. The type might be
   * <ul>
   *   <li>{@link ChangesetMapper#TYPE} (<code>{@value ChangesetMapper#TYPE}</code>),</li>
   *   <li>{@link PullRequestCommentMapper#TYPE} (<code>{@value PullRequestCommentMapper#TYPE}</code>), or</li>
   *   <li>another value if this is used by further plugins.</li>
   * </ul>
   * @param type The type of the event that triggers the state change.
   * @return <code>true</code>, if the state change should be performed, <code>false</code> otherwise.
   */
  default boolean isStateChangeActivatedFor(String type) {
    switch (type) {
      case ChangesetMapper.TYPE:
        return isStateChangeActivatedForCommits();
      case PullRequestCommentMapper.TYPE:
        return isStateChangeActivatedForPullRequests();
      default:
        return true;
    }
  }

  /**
   * This is used by {@link #isStateChangeActivatedFor(String)} and therefore will not be called, if the
   * default implementation of {@link #isStateChangeActivatedFor(String)} is not used by the implementation of this
   * interface.
   * If this returns <code>false</code>, state changes will be performed for commits. The default implementation
   * returns <code>true</code>.
   * @return <code>true</code> by default.
   */
  default boolean isStateChangeActivatedForCommits() {
    return true;
  }

  /**
   * This is used by {@link #isStateChangeActivatedFor(String)} and therefore will not be called, if the
   * default implementation of {@link #isStateChangeActivatedFor(String)} is not used by the implementation of this
   * interface.
   * If this returns <code>false</code>, state changes will be performed for pull requests. The default implementation
   * returns <code>true</code>.
   * @return <code>true</code> by default.
   */
  default boolean isStateChangeActivatedForPullRequests() {
    return true;
  }
}
