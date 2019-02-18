package org.hesperides.core.domain.templatecontainers.entities;

import com.github.mustachejava.MustacheException;
import lombok.Value;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.templatecontainers.exceptions.InvalidTemplateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Value
public class Template {
    String name;
    String filename;
    String location;
    String content;
    Rights rights;
    Long versionId;
    TemplateContainer.Key templateContainerKey;

    public Template validateProperties() {
        extractProperties();
        return this;
    }

    public List<AbstractProperty> extractProperties() {
        List<AbstractProperty> properties = new ArrayList<>();
        properties.addAll(extractPropertiesFromStringContent("filename", filename));
        properties.addAll(extractPropertiesFromStringContent("location", location));
        properties.addAll(extractPropertiesFromStringContent("content", content));
        return properties;
    }

    private List<AbstractProperty> extractPropertiesFromStringContent(String fieldName, String string) {
        try {
            return AbstractProperty.extractPropertiesFromStringContent(string);
        } catch (MustacheException mustacheException) {
            throw new InvalidTemplateException(templateContainerKey.toString(), fieldName, mustacheException);
        }
    }

    public Template validateNameNotTaken(Map<String, Template> templates, TemplateContainer.Key key) {
        if (templates.containsKey(name)) {
            throw new DuplicateTemplateCreationException(key, name);
        }
        return this;
    }

    public Template initializeVersionId() {
        return new Template(
                name,
                filename,
                location,
                content,
                rights,
                1L,
                templateContainerKey
        );
    }

    public Template validateExistingName(Map<String, Template> templates, TemplateContainer.Key key) {
        if (!templates.containsKey(name)) {
            throw new TemplateNotFoundException(key, name);
        }
        return this;
    }

    public Template validateVersionId(Long expectedVersionId) {
        if (!versionId.equals(expectedVersionId)) {
            throw new OutOfDateVersionException(expectedVersionId, versionId);
        }
        return this;
    }

    public Template incrementVersionId() {
        return new Template(
                name,
                filename,
                location,
                content,
                rights,
                versionId + 1,
                templateContainerKey
        );
    }

    @Value
    public static class Rights {
        FileRights user;
        FileRights group;
        FileRights other;
    }

    @Value
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}