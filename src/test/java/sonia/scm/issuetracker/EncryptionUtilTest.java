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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class EncryptionUtilTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testEncryptionUtil()
  {
    String encrypted = EncryptionUtil.encrypt("hansolo");

    assertNotEquals("hansolo", encrypted);
    assertTrue(EncryptionUtil.isEncrypted(encrypted));

    String plain = EncryptionUtil.decrypt(encrypted);

    assertEquals("hansolo", plain);
    assertFalse(EncryptionUtil.isEncrypted(plain));
  }
}
