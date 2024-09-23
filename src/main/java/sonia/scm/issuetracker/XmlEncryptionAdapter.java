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

//~--- JDK imports ------------------------------------------------------------

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.1
 * @deprecated Use {@link sonia.scm.xml.XmlEncryptionAdapter} from SCM-Manager
 *   core instead. Otherwise, other plugins like the audit log cannot
 *   mask passwords correctly.
 *   The annotation from the core is compatible to this annotation, so
 *   no data has to be migrated.
 */
@Deprecated
public class XmlEncryptionAdapter extends XmlAdapter<String, String>
{

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String marshal(String v) throws Exception
  {
    if (!EncryptionUtil.isEncrypted(v))
    {
      v = EncryptionUtil.encrypt(v);
    }

    return v;
  }

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String unmarshal(String v) throws Exception
  {
    if (EncryptionUtil.isEncrypted(v))
    {
      v = EncryptionUtil.decrypt(v);
    }

    return v;
  }
}
