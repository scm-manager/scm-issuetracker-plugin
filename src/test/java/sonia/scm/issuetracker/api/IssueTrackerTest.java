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

package sonia.scm.issuetracker.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IssueTrackerTest {

  private final SampleIssueTracker issueTracker = new SampleIssueTracker();

  @Test
  void shouldReturnClassName() {
    assertThat(issueTracker.getName()).isEqualTo(SampleIssueTracker.class.getName());
  }

  @Test
  void shouldReturnEmptyOptionalForResubmitter() {
    assertThat(issueTracker.getResubmitter()).isEmpty();
  }

  private static class SampleIssueTracker implements IssueTracker {

    @Override
    public void process(IssueReferencingObject object) {

    }

    @Override
    public Map<String, String> findIssues(IssueReferencingObject object) {
      return null;
    }
  }

}
