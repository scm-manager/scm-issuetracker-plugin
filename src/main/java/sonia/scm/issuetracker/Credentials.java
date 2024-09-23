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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import sonia.scm.security.CipherUtil;
import sonia.scm.util.AssertUtil;

/**
 *
 * @author Sebastian Sdorra
 * @deprecated in scm-manager v2 there is no longer a way to access the credentials
 */
@Deprecated
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
