package org.openapi4j.schema.validator.v3;

import com.fasterxml.jackson.databind.JsonNode;

import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.validation.ValidationResults;
import org.openapi4j.schema.validator.BaseJsonValidator;
import org.openapi4j.schema.validator.ValidationContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class PropertiesValidator extends BaseJsonValidator<OAI3> {
  private final Map<String, SchemaValidator> schemas;

  PropertiesValidator(final ValidationContext<OAI3> context, final JsonNode schemaNode, final JsonNode schemaParentNode, final SchemaValidator parentSchema) {
    super(context, schemaNode, schemaParentNode, parentSchema);

    schemas = new HashMap<>();
    for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
      String pname = it.next();
      schemas.put(pname, new SchemaValidator(context, pname, schemaNode.get(pname), schemaParentNode, parentSchema));
    }
  }

  @Override
  public void validate(final JsonNode valueNode, final ValidationResults results) {
    for (Map.Entry<String, SchemaValidator> entry : schemas.entrySet()) {
      SchemaValidator propertySchema = entry.getValue();
      JsonNode propertyNode = valueNode.get(entry.getKey());

      if (propertyNode != null) {
        propertySchema.validate(propertyNode, results);
      }
    }
  }
}
