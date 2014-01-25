/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
