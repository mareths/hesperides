/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.templatecontainers.scenarios;

import io.cucumber.java8.En;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdateTemplates implements En {

    @Autowired
    private TemplateBuilder templateBuilder;

    public UpdateTemplates() {

        Given("^a template to update$", () -> {
            templateBuilder.withContent("this content will be added to the existing content");
        });

        Given("^the template is outdated", () -> {
            templateBuilder.withVersionId(2049);
        });

        Given("^the template has an invalid property", () -> {
            templateBuilder.withContent("username = {{{ mysql.user.name }}");
        });
    }
}
