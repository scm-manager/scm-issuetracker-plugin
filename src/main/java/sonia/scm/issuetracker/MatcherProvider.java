package sonia.scm.issuetracker;

import sonia.scm.repository.Repository;

import java.util.Optional;

public interface MatcherProvider {
  Optional<IssueMatcher> createMatcher(Repository repository);
}
