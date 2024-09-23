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

import java.io.IOException;

public class TemplateNotFoundException extends IOException {

  private final String templatePath;

  public TemplateNotFoundException(String templatePath) {
    super("Could not find template at " + templatePath);
    this.templatePath = templatePath;
  }

  public String getTemplatePath() {
    return templatePath;
  }
}
