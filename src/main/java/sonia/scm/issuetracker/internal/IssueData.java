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
package sonia.scm.issuetracker.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "issue-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueData {

  @XmlElement(name = "changeset")
  @XmlElementWrapper(name = "handled-changesets")
  private Set<String> handledChangesets;
  @XmlElement(name = "key")
  @XmlElementWrapper(name = "handled-keys")
  private Set<String> handledKeys;

  public Set<String> getHandledChangesets() {
    if (handledChangesets == null)
      handledChangesets = Collections.emptySet();

    return unmodifiableSet(handledChangesets);
  }

  public void addHandledChangeset(String changesetId) {
    if (handledChangesets == null) {
      handledChangesets = new HashSet<>(asList(changesetId));
    } else {
      this.handledChangesets.add(changesetId);
    }
  }

  public Set<String> getHandledKeys() {
    if (handledKeys == null)
      return Collections.emptySet();

    return unmodifiableSet(handledKeys);
  }

  public void addHandledKey(String key) {
    if (handledKeys == null) {
      handledKeys = new HashSet<>(asList(key));
    } else {
      this.handledKeys.add(key);
    }
  }
}
