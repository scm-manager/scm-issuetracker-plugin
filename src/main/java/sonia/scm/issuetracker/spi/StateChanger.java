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

}
