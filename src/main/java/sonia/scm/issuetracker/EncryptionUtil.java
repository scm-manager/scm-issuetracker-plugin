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

import com.google.common.base.Strings;

import sonia.scm.security.CipherUtil;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.1
 */
public class EncryptionUtil
{

  /** Field description */
  private static final String PREFIX = "{enc}";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static String decrypt(String value)
  {
    if (!value.startsWith(PREFIX))
    {
      throw new IllegalArgumentException("value is not encrypted");
    }

    return CipherUtil.getInstance().decode(value.substring(PREFIX.length()));
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static String encrypt(String value)
  {
    return PREFIX.concat(CipherUtil.getInstance().encode(value));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isEncrypted(String value)
  {
    return !Strings.isNullOrEmpty(value) && value.startsWith(PREFIX);
  }
}
