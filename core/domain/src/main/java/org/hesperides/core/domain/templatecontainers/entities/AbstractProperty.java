/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
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
package org.hesperides.core.domain.templatecontainers.entities;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.commons.spring.HasProfile;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Value
@NonFinal
public abstract class AbstractProperty {

    String name;

    public static List<AbstractProperty> extractPropertiesFromTemplates(Collection<Template> templates, List<String> updatedTemplatesName, boolean isFirstEvent) {
        Set<AbstractProperty> properties = new HashSet<>();

        if (HasProfile.dataMigration()) {

            Set<AbstractProperty> updatedTemplatesProperties = Optional.ofNullable(templates)
                    .orElse(Collections.emptyList())
                    .stream()
                    .sorted((template1, template2) -> isFirstEvent ? template2.getName().compareTo(template1.getName()) : template1.getName().compareTo(template2.getName()))
                    .filter(template -> updatedTemplatesName.stream().anyMatch(updatedTemplateName -> updatedTemplateName.equalsIgnoreCase(template.getName())))
                    .map(Template::extractProperties)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            Set<AbstractProperty> otherTemplatesProperties = Optional.ofNullable(templates)
                    .orElse(Collections.emptyList())
                    .stream()
                    .sorted((template1, template2) -> template2.getName().compareTo(template1.getName()))
                    .filter(template -> updatedTemplatesName.stream().noneMatch(updatedTemplate -> updatedTemplate.equalsIgnoreCase(template.getName())))
                    .map(Template::extractProperties)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            properties.addAll(updatedTemplatesProperties);
            properties.addAll(otherTemplatesProperties);

        } else {
            properties.addAll(Optional.ofNullable(templates)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Template::extractProperties)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet()));
        }

        return new ArrayList<>(properties);
    }

    public static List<AbstractProperty> extractPropertiesFromStringContent(String content) {
        List<AbstractProperty> properties = new ArrayList<>();
        Mustache mustache = getMustacheInstanceFromStringContent(content);
        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {
                String propertyDefinition = code.getName();
                Property property = Property.extractProperty(propertyDefinition);
                if (property != null) {
                    properties.add(property);
                }
            } else if (code instanceof IterableCode) {
                IterableProperty iterableProperty = IterableProperty.extractIterablePropertyFromMustacheCode((IterableCode) code);
                properties.add(iterableProperty);
            }
        }
        return properties;
    }

    public static Mustache getMustacheInstanceFromStringContent(String content) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory.compile(new StringReader(content), "anything");
    }
}