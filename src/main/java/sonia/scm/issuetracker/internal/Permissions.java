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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class Permissions {

  private static final String RESOURCE = "issuetracker";
  private static final String ACTION_RESUBMIT = "resubmit";

  private static final String PERMISSION_RESUBMIT = RESOURCE + ":" + ACTION_RESUBMIT;

  private Permissions() {
  }

  public static void checkResubmit(String issueTrackerName) {
    SecurityUtils.getSubject().checkPermission(PERMISSION_RESUBMIT + ":" + issueTrackerName);
  }

  public static void checkResubmit() {
    SecurityUtils.getSubject().checkPermission(PERMISSION_RESUBMIT);
  }

  public static boolean isResubmitPermitted() {
    return SecurityUtils.getSubject().isPermitted(PERMISSION_RESUBMIT);
  }

  public static Checker resubmitChecker() {
    return new Checker(SecurityUtils.getSubject(), PERMISSION_RESUBMIT + ":");
  }

  public static class Checker {

    private final Subject subject;
    private final String basePermission;

    private Checker(Subject subject, String basePermission) {
      this.subject = subject;
      this.basePermission = basePermission;
    }

    public boolean isPermitted(String id) {
      return subject.isPermitted(basePermission + id);
    }
  }

}
