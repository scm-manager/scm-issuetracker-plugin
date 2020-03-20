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

package sonia.scm.issuetracker;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import sonia.scm.security.CipherUtil;
import sonia.scm.util.AssertUtil;

/**
 *
 * @author Sebastian Sdorra
 */
public final class Credentials
{

  /** Field description */
  private static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param username
   * @param password
   */
  public Credentials(String username, String password)
  {
    this.username = username;
    this.password = password;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static Credentials current()
  {
    String credentialsString = getCredentialsString();

    AssertUtil.assertIsNotEmpty(credentialsString);
    credentialsString = CipherUtil.getInstance().decode(credentialsString);

    Credentials credentials = null;

    int index = credentialsString.indexOf(':');

    if ((index > 0) && (index < credentialsString.length()))
    {
      //J-
      credentials = new Credentials(
        credentialsString.substring(0, index),
        credentialsString.substring(index + 1)
      );
      //J+
    }

    return credentials;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private static String getCredentialsString()
  {
    String crendentials = null;

    Subject subject = SecurityUtils.getSubject();
    Session subjectSession = subject.getSession();

    if (subjectSession != null)
    {
      crendentials = (String) subjectSession.getAttribute(SCM_CREDENTIALS);

    }

    return crendentials;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String password;

  /** Field description */
  private final String username;
}
