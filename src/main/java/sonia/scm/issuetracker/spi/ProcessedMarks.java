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

import lombok.Data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class ProcessedMarks {

  private Set<Mark> marks = new HashSet<>();

  ProcessedMarks() {
  }

  public boolean contains(Mark mark) {
    return marks.contains(mark);
  }

  public void add(Mark mark) {
    marks.add(mark);
  }

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  static class Mark {

    private String type;
    private String id;
    private String keyword;

    Mark() {
    }

    Mark(String type, String id) {
      this.type = type;
      this.id = id;
    }

    Mark(String type, String id, String keyword) {
      this.type = type;
      this.id = id;
      this.keyword = keyword;
    }

  }
}
