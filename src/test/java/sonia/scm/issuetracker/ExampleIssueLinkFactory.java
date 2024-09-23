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

package sonia.scm.issuetracker;

public class ExampleIssueLinkFactory implements IssueLinkFactory {

  private static final String JIRA_URL = "https://jira.hitchhiker.com/issues/";
  private static final String REDMINE_URL = "https://redmine.hitchhiker.com/issues/";

  private String trackerUrl;

  public ExampleIssueLinkFactory(String trackerUrl) {
    this.trackerUrl = trackerUrl;
  }

  @Override
  public String createLink(String key) {
    if (key.startsWith("#")) {
      return trackerUrl + key.substring(1);
    }
    return trackerUrl + key;
  }

  public static ExampleIssueLinkFactory createJira() {
    return new ExampleIssueLinkFactory(JIRA_URL);
  }

  public static ExampleIssueLinkFactory createRedmine() {
    return new ExampleIssueLinkFactory(REDMINE_URL);
  }
}
