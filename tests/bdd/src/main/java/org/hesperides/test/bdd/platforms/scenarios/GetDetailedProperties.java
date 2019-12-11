package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import lombok.Value;
import org.hesperides.core.presentation.io.platforms.properties.DetailedPropertiesPlatformOutput;
import org.hesperides.core.presentation.io.platforms.properties.DetailedPropertiesPlatformOutput.DetailedPropertiesModuleOutput;
import org.hesperides.core.presentation.io.platforms.properties.DetailedPropertiesPlatformOutput.DetailedPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.DetailedPropertiesPlatformOutput.ModuleDetailedPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.DetailedPropertiesPlatformOutput.ModuleOutput;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.assertEquals;

public class GetDetailedProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;

    public GetDetailedProperties() {

        When("^I get the detailed properties of this (module|platform)?$", (String moduleOrPlatform) -> {
            String propertiesPath = "module".equals(moduleOrPlatform) ? deployedModuleBuilder.buildPropertiesPath() : null;
            platformClient.getDetailedProperties(platformBuilder.buildInput(), propertiesPath);
        });

        Then("^the detailed properties of(?: this)? module(?: \"([^\"]+)\")? are$", (String moduleName, DataTable data) -> {
            assertOK();
            List<ModuleDetailedProperty> moduleDetailedProperties = data.asList(ModuleDetailedProperty.class);
            List<ModuleDetailedPropertyOutput> expectedProperties = ModuleDetailedProperty.toModuleDetailedPropertyOutputs(moduleDetailedProperties, platformBuilder);
            List<ModuleDetailedPropertyOutput> actualProperties = findActualModule(testContext.getResponseBody(), moduleName).getDetailedProperties();
            assertEquals(expectedProperties, actualProperties);
        });

        Then("^the detailed global properties of this platform are$", (DataTable data) -> {
            assertOK();
            List<DetailedPropertyOutput> expectedProperties = data.asList(DetailedPropertyOutput.class);
            DetailedPropertiesPlatformOutput detailedPropertiesPlatformOutput = testContext.getResponseBody();
            List<DetailedPropertyOutput> actualProperties = detailedPropertiesPlatformOutput.getGlobalProperties();
            assertEquals(expectedProperties, actualProperties);
        });
    }

    private DetailedPropertiesModuleOutput findActualModule(DetailedPropertiesPlatformOutput detailedProperties, String moduleName) {
        return isEmpty(moduleName) ? detailedProperties.getDeployedModules().get(0) : detailedProperties.getDeployedModules().stream()
                .filter(module -> moduleName.equals(module.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't find module \"" + moduleName + "\""));
    }

    @Value
    private static class ModuleDetailedProperty {
        String name;
        String storedValue;
        String finalValue;
        String defaultValue;
        boolean isRequired;
        boolean isPassword;
        String pattern;
        String comment;
        String referencedGlobalProperties;
        String referencingModules;
        boolean isNotUsed;

        public static List<ModuleDetailedPropertyOutput> toModuleDetailedPropertyOutputs(List<ModuleDetailedProperty> moduleDetailedProperties, PlatformBuilder platformBuilder) {
            return moduleDetailedProperties.stream()
                    .map(moduleDetailedProperty -> moduleDetailedProperty.toModuleDetailedPropertyOutput(platformBuilder))
                    .collect(toList());
        }

        public ModuleDetailedPropertyOutput toModuleDetailedPropertyOutput(PlatformBuilder platformBuilder) {

            return new ModuleDetailedPropertyOutput(
                    name,
                    defaultIfEmpty(storedValue, null),
                    finalValue,
                    defaultIfEmpty(defaultValue, null),
                    isRequired,
                    isPassword,
                    defaultIfEmpty(pattern, null),
                    defaultIfEmpty(comment, null),
                    buildReferencedGlobalPropertiesOutput(platformBuilder),
                    buildReferencingModulesOutput(platformBuilder),
                    isNotUsed);
        }

        private List<DetailedPropertyOutput> buildReferencedGlobalPropertiesOutput(PlatformBuilder platformBuilder) {
            List<DetailedPropertyOutput> referencedGlobalPropertiesOutput = new ArrayList<>();
            if (isNotEmpty(referencedGlobalProperties)) {
                referencedGlobalPropertiesOutput = Arrays.stream(referencedGlobalProperties.split(","))
                        .map(String::trim)
                        .map(referencedGlobalProperty -> {
                            ValuedPropertyIO globalProperty = platformBuilder.getGlobalProperties().stream()
                                    .filter(platformProperty -> referencedGlobalProperty.equals(platformProperty.getName()))
                                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't find referenced global property \"" + referencedGlobalProperty + "\""));

                            return new DetailedPropertyOutput(globalProperty.getName(), globalProperty.getValue(), globalProperty.getValue());
                        })
                        .collect(toList());
            }
            return referencedGlobalPropertiesOutput;
        }

        private List<ModuleOutput> buildReferencingModulesOutput(PlatformBuilder platformBuilder) {
            List<ModuleOutput> referencingModulesOutput = new ArrayList<>();
            if (isNotEmpty(referencingModules)) {
                referencingModulesOutput = Arrays.stream(referencingModules.split(","))
                        .map(String::trim)
                        .map(referencingModule -> {
                            DeployedModuleBuilder deployedModuleBuilder = platformBuilder.getDeployedModuleBuilders().stream()
                                    .filter(platformModule -> referencingModule.equals(platformModule.getName()))
                                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't find referencing module \"" + referencingModule + "\""));

                            return new ModuleOutput(deployedModuleBuilder.buildModuleKey());
                        })
                        .collect(toList());
            }
            return referencingModulesOutput;
        }
    }
}
