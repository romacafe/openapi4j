package org.openapi4j.schema.validator.v3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.model.v3.OAI3Context;
import org.openapi4j.core.validation.ValidationResults;
import org.openapi4j.schema.validator.BaseJsonValidator;
import org.openapi4j.schema.validator.JsonValidator;
import org.openapi4j.schema.validator.ValidationContext;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.openapi4j.core.model.v3.OAI3SchemaKeywords.ADDITIONALPROPERTIES;
import static org.openapi4j.core.model.v3.OAI3SchemaKeywords.NULLABLE;
import static org.openapi4j.schema.validator.v3.ValidationOptions.ADDITIONAL_PROPS_RESTRICT;

public class SchemaValidator extends BaseJsonValidator<OAI3> {
  private static final JsonNode FALSE_NODE = JsonNodeFactory.instance.booleanNode(false);

  private final String propertyName;
  private final Map<String, JsonValidator<OAI3>> validators;

  public SchemaValidator(final String propertyName, final JsonNode schemaNode) {
    this(null, propertyName, schemaNode, null, null);
  }

  public SchemaValidator(final ValidationContext<OAI3> context,
                         final String propertyName,
                         final JsonNode schemaNode) {

    this(context, propertyName, schemaNode, null, null);
  }

  public SchemaValidator(final ValidationContext<OAI3> context,
                         final String propertyName,
                         final JsonNode schemaNode,
                         final JsonNode schemaParentNode,
                         final SchemaValidator parentSchema) {

    super(context, schemaNode, schemaParentNode, parentSchema);

    if (context == null) {
      try {
        OAI3Context apiContext = new OAI3Context(URI.create("/"), schemaNode);
        this.context = new ValidationContext<>(apiContext);
      } catch (ResolutionException e) {
        throw new RuntimeException(e);
      }
    }

    this.propertyName = propertyName;
    validators = read(this.context, schemaNode);
  }

  public ValidationContext<OAI3> getContext() {
    return context;
  }

  SchemaValidator findParent() {
    return (getParentSchema() != null) ? getParentSchema().findParent() : this;
  }

  @Override
  public void validate(final JsonNode valueNode, final ValidationResults results) {
    results.withCrumb(propertyName, () -> {
      for (Map.Entry<String, JsonValidator<OAI3>> validatorEntry : validators.entrySet()) {
        validatorEntry.getValue().validate(valueNode, results);
      }
    });
  }

  private Map<String, JsonValidator<OAI3>> read(final ValidationContext<OAI3> context, final JsonNode schemaNode) {
    Map<String, JsonValidator<OAI3>> validators = new HashMap<>();

    Iterator<String> fieldNames = schemaNode.fieldNames();
    while (fieldNames.hasNext()) {
      final String keyword = fieldNames.next();
      final JsonNode keywordSchemaNode = schemaNode.get(keyword);

      JsonValidator<OAI3> validator = ValidatorsRegistry.instance().getValidator(context, keyword, keywordSchemaNode, schemaNode, this);
      if (validator != null) {
        validators.put(keyword, validator);
      }
    }

    if (validators.get(ADDITIONALPROPERTIES) == null && context.getOption(ADDITIONAL_PROPS_RESTRICT)) {
      validators.put(
        ADDITIONALPROPERTIES,
        ValidatorsRegistry.instance().getValidator(context, ADDITIONALPROPERTIES, FALSE_NODE, schemaNode, this));
    }

    // Setup default nullable schema to false
    // https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md#schemaNullable
    if (validators.get(NULLABLE) == null) {
      validators.put(
        NULLABLE,
        ValidatorsRegistry.instance().getValidator(context, NULLABLE, FALSE_NODE, schemaNode, this));
    }

    return validators;
  }
}
