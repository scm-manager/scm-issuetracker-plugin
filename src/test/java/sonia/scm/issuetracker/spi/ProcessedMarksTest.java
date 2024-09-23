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

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedMarksTest {

  @Test
  void shouldMarshalAndUnmarshal() {
    ProcessedMarks marks = new ProcessedMarks();
    ProcessedMarks.Mark changeset = new ProcessedMarks.Mark("changeset", "abc");
    marks.add(changeset);
    ProcessedMarks.Mark pr = new ProcessedMarks.Mark("pr", "42", "merged");
    marks.add(pr);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(marks, baos);
    ProcessedMarks unmarshalled = JAXB.unmarshal(new ByteArrayInputStream(baos.toByteArray()), ProcessedMarks.class);

    assertThat(unmarshalled.contains(changeset)).isTrue();
    assertThat(unmarshalled.contains(pr)).isTrue();
  }

}
