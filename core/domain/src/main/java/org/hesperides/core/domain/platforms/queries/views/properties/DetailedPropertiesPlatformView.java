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
package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Value
public class DetailedPropertiesPlatformView {

    String applicationName;
    String platformName;
    List<DetailedPropertyView> globalProperties;
    List<DetailedPropertiesModuleView> deployedModules;

    public DetailedPropertiesPlatformView completeWithPropertiesUsage(Map<Module.Key, List<AbstractPropertyView>> propertiesByModuleKey) {
        // Pour chaque propriété, on recherche les modules dans lesquels elle est définie
        // Attention, ce ne tient pas compte des propriétés itérables
        deployedModules.forEach(module -> module.getDetailedProperties().forEach(detailedProperty -> {
            propertiesByModuleKey.forEach((moduleKey, moduleProperties) -> {
                boolean detailedPropertyFoundInModule = moduleProperties.stream()
                        .map(AbstractPropertyView::getName)
                        .anyMatch(propertyName -> propertyName.equals(detailedProperty.getName()));
                if (detailedPropertyFoundInModule) {
                    detailedProperty.getReferencingModules().add(moduleKey);
                }
            });
        }));
        return this;
    }

    @Value
    @NonFinal
    public static class DetailedPropertyView {
        String name;
        String storedValue;
        String finalValue;
    }

    @Value
    public static class DetailedPropertiesModuleView {
        Module.Key moduleKey;
        String modulePath;
        String propertiesPath;
        List<ModuleDetailedPropertyView> detailedProperties;

        public DetailedPropertiesModuleView(DeployedModuleView deployedModule, List<ModuleDetailedPropertyView> detailedProperties) {
            this.moduleKey = deployedModule.getModuleKey();
            this.modulePath = deployedModule.getModulePath();
            this.propertiesPath = deployedModule.getPropertiesPath();
            this.detailedProperties = detailedProperties;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class ModuleDetailedPropertyView extends DetailedPropertyView {
        String defaultValue;
        boolean isRequired;
        boolean isPassword;
        String pattern;
        String comment;
        List<DetailedPropertyView> referencedGlobalProperties;
        List<Module.Key> referencingModules;
        boolean isNotUsed;

        public ModuleDetailedPropertyView(
                String name,
                String storedValue,
                String finalValue,
                String defaultValue,
                List<PropertyView> propertyModels,
                List<DetailedPropertyView> globalProperties,
                Set<String> unusedProperties) {
            super(name, storedValue, finalValue);
            this.defaultValue = defaultValue;
            this.isRequired = !isEmpty(propertyModels) && propertyModels.get(0).isRequired();
            this.isPassword = !isEmpty(propertyModels) && propertyModels.get(0).isPassword();
            this.pattern = isEmpty(propertyModels) ? null : propertyModels.get(0).getPattern();
            this.comment = isEmpty(propertyModels) ? null : propertyModels.get(0).getComment();
            this.referencedGlobalProperties = getReferencedGlobalProperties(globalProperties);
            this.referencingModules = new ArrayList<>();
            this.isNotUsed = unusedProperties.contains(name);
        }

        // Soit le nom est identique à celui d'une propriété globale,
        // soit sa valeur comtient une référence à une ou plusieurs propriétés globales
        private List<DetailedPropertyView> getReferencedGlobalProperties(List<DetailedPropertyView> globalProperties) {
            List<DetailedPropertyView> referencedGlobalProperties = new ArrayList<>();
            globalProperties.forEach(globalProperty -> {
                if (globalProperty.getName().equals(getName())) {
                    referencedGlobalProperties.add(globalProperty);
                }
                ValuedProperty.extractValuesBetweenCurlyBrackets(getStoredValue()).forEach(extractedProperty -> {
                    if (extractedProperty.equals(globalProperty.getName())) {
                        referencedGlobalProperties.add(globalProperty);
                    }
                });
            });

            return referencedGlobalProperties;
        }
    }
}
