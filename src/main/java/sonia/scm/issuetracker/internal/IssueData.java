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

package sonia.scm.issuetracker.internal;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "issue-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueData
{

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<String> getHandledChangesets()
  {
    if (handledChangesets == null)
    {
      handledChangesets = Sets.newHashSet();
    }

    return handledChangesets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "changeset")
  @XmlElementWrapper(name = "handled-changesets")
  private Set<String> handledChangesets;
}
