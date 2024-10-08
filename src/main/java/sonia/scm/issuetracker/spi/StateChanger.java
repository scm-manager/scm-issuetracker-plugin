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
   * The default implementation delegates to {@link #isStateChangeActivatedForCommits()} for commits and
   * {@link #isStateChangeActivatedForPullRequests()} for pull requests and returns <code>true</code> for
   * all other types.
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
