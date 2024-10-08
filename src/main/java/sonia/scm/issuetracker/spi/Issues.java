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

import com.google.common.base.Strings;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.api.Content;
import sonia.scm.issuetracker.api.IssueReferencingObject;

import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Issues {

  private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^\\w-]");

  private Issues() {
  }

  static Set<String> find(IssueMatcher matcher, IssueReferencingObject object) {
    return find(matcher, object.getContent());
  }

  static Set<String> find(IssueMatcher matcher, List<Content> content) {
    Set<String> issues = new LinkedHashSet<>();
    Pattern pattern = matcher.getKeyPattern();
    for (Content c : content) {
      Matcher m = pattern.matcher(c.getValue());
      while (m.find()) {
        String key = matcher.getKey(m);
        issues.add(key);
      }
    }
    return issues;
  }

  static Optional<String> detectStateChange(String issueKey, Iterable<String> keyWords, IssueReferencingObject object) {
    StateChangeDetector detector = new StateChangeDetector(Locale.ENGLISH, issueKey, keyWords);
    return Optional.ofNullable(detector.detect(object));
  }

  static String normalize(String issueKey) {
    Matcher matcher = NORMALIZE_PATTERN.matcher(issueKey);
    return matcher.replaceAll("_");
  }

  private static class StateChangeDetector {

    private final Locale locale;
    private final String issueKey;
    private final Iterable<String> keyWords;

    private StateChangeDetector(Locale locale, String issueKey, Iterable<String> keyWords) {
      this.locale = locale;
      this.issueKey = issueKey;
      this.keyWords = keyWords;
    }

    String detect(IssueReferencingObject object) {
      for (Content content : object.getContent()) {
        String keyWord = detect(Strings.nullToEmpty(content.getValue()));
        if (keyWord != null) {
          return keyWord;
        }
      }
      return null;
    }

    private String detect(String content) {
      for (String line : lines(content)) {
        String keyWord = detectInLine(line);
        if (keyWord != null) {
          return keyWord;
        }
      }
      return null;
    }

    private String detectInLine(String line) {
      for (String sentence : sentences(line)) {
        String keyWord = detectInSentence(sentence);
        if (keyWord != null) {
          return keyWord;
        }
      }
      return null;
    }

    private String detectInSentence(String sentence) {
      List<String> words = words(sentence);
      if (containsIssueKey(words)) {
        List<String> lowerCaseWords = toLowerCase(words);
        for (String keyWord : keyWords) {
          if (lowerCaseWords.contains(keyWord.toLowerCase(locale))) {
            return keyWord;
          }
        }
      }
      return null;
    }

    private boolean containsIssueKey(List<String> words) {
      List<String> issueKeyParts = words(issueKey);
      return Collections.indexOfSubList(words, issueKeyParts) >= 0;
    }

    private List<String> toLowerCase(Collection<String> words) {
      return words.stream().map(word -> word.toLowerCase(locale)).collect(Collectors.toList());
    }

    private String[] lines(String value) {
      return value.split("\n");
    }

    private List<String> words(String sentence) {
      BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
      return collect(breakIterator, sentence);
    }

    private List<String> sentences(String value) {
      BreakIterator breakIterator = BreakIterator.getSentenceInstance(locale);
      return collect(breakIterator, value);
    }

    private List<String> collect(BreakIterator breakIterator, String value) {
      List<String> items = new ArrayList<>();
      breakIterator.setText(value);

      int prevIndex = 0;
      int boundaryIndex = breakIterator.first();
      while (boundaryIndex != BreakIterator.DONE) {
        String item = value.substring(prevIndex, boundaryIndex);
        // break iterator starts with an empty string
        if (!item.isEmpty()) {
          items.add(item);
        }
        prevIndex = boundaryIndex;
        boundaryIndex = breakIterator.next();
      }
      return items;
    }

  }
}
