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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.issuetracker.ExampleIssueMatcher;
import sonia.scm.issuetracker.api.IssueReferencingObject;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.issuetracker.IssueReferencingObjects.content;

class IssuesTest {

  @Nested
  class FindTests {

    @Test
    void shouldFindIssues() {
      IssueReferencingObject referencingObject = content("ABC-42");
      Set<String> issues = Issues.find(ExampleIssueMatcher.createJira(), referencingObject);
      assertThat(issues).contains("ABC-42");
    }

    @Test
    void shouldReturnEmptySetWithoutIssueKey() {
      IssueReferencingObject referencingObject = content("No matching key here");
      Set<String> issues = Issues.find(ExampleIssueMatcher.createJira(), referencingObject);
      assertThat(issues).isEmpty();
    }

    @Test
    void shouldFindMultipleIssues() {
      IssueReferencingObject referencingObject = content("ABC-42 and NBC-21");
      Set<String> issues = Issues.find(ExampleIssueMatcher.createJira(), referencingObject);
      assertThat(issues).contains("ABC-42", "NBC-21");
    }

    @Test
    void shouldReturnEachKeyOnlyOnce() {
      IssueReferencingObject referencingObject = content("ABC-42 and NBC-21 and again ABC-42");
      Set<String> issues = Issues.find(ExampleIssueMatcher.createJira(), referencingObject);
      assertThat(issues).contains("ABC-42", "NBC-21").hasSize(2);
    }

    @Test
    void shouldFindMultipleIssuesInMultipleContents() {
      IssueReferencingObject referencingObject = content("ABC-42 and NBC-21", "CDC-18", "AWS-11 and GKE-3");
      Set<String> issues = Issues.find(ExampleIssueMatcher.createJira(), referencingObject);
      assertThat(issues).contains("ABC-42", "NBC-21", "CDC-18", "AWS-11", "GKE-3");
    }

  }

  @Nested
  @SuppressWarnings("java:S5976") // we don't want parametrized test here, because we want to keep the description
  class DetectStateChange {

    private final Set<String> keyWords = ImmutableSet.of("fixed", "fixes", "fix", "closed", "resolved");

    @Test
    void shouldNotDetectAStateChangeWithoutIssueKey() {
      IssueReferencingObject referencingObject = content("No matching key here");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).isEmpty();
    }

    @Test
    void shouldNotDetectAStateChangeWithoutKeyWord() {
      IssueReferencingObject referencingObject = content("ABC-42 is awesome");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).isEmpty();
    }

    @Test
    void shouldNotDetectPartialIssueKeys() {
      IssueReferencingObject referencingObject = content("ABC-42 is resolved");
      Optional<String> stateChange = Issues.detectStateChange("ABC-4", keyWords, referencingObject);
      assertThat(stateChange).isEmpty();
    }

    @Test
    void shouldDetectAStateChangeInOneLine() {
      IssueReferencingObject referencingObject = content("ABC-42 is fixed");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).contains("fixed");
    }

    @Test
    void shouldDetectAStateChangeWithDifferentCase() {
      IssueReferencingObject referencingObject = content("Fixes ABC-42.");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).contains("fixes");
    }

    @Test
    void shouldReturnFirstMatch() {
      IssueReferencingObject referencingObject = content("ABC-42 is now fully closed", "Fixes ABC-42");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).contains("closed");
    }

    @Test
    void shouldNotMatchKeyWordInDifferentSentence() {
      IssueReferencingObject referencingObject = content("ABC-42 is awesome. Someone should open the closed door.");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).isEmpty();
    }

    @Test
    void shouldNotMatchKeyWordInDifferentLines() {
      IssueReferencingObject referencingObject = content("ABC-42 is awesome\nSomeone should open the closed door.");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).isEmpty();
    }

    @Test
    void shouldNotMatchKeyWordsAsPartOfAnotherWord() {
      IssueReferencingObject referencingObject = content("ABC-42 is a fixture ...");
      Optional<String> stateChange = Issues.detectStateChange("ABC-42", keyWords, referencingObject);
      assertThat(stateChange).isEmpty();
    }
  }

  @Nested
  class Normalize {

    @Test
    void shouldKeeAsIs() {
      assertThat(Issues.normalize("JIRA-123")).isEqualTo("JIRA-123");
      assertThat(Issues.normalize("42")).isEqualTo("42");
    }

    @Test
    void shouldNormalize() {
      assertThat(Issues.normalize("#42")).isEqualTo("_42");
      assertThat(Issues.normalize("/42")).isEqualTo("_42");
      assertThat(Issues.normalize("4/2")).isEqualTo("4_2");
    }

  }

}
