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

  public static boolean isResubmitPermitted() {
    return SecurityUtils.getSubject().isPermitted(PERMISSION_RESUBMIT);
  }

  public static Checker checkResubmit() {
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
