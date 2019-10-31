package org.openapi4j.schema.validator.v3;

import com.fasterxml.jackson.databind.JsonNode;

import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.validation.ValidationResults;
import org.openapi4j.schema.validator.BaseJsonValidator;
import org.openapi4j.schema.validator.ValidationContext;

import java.math.BigDecimal;

import static org.openapi4j.core.model.v3.OAI3SchemaKeywords.MULTIPLEOF;

/**
 * The value of "multipleOf" MUST be a number, strictly greater than 0.
 * <p>
 * A numeric instance is only valid if division by this keyword's value
 * results in an integer.
 */
class MultipleOfValidator extends BaseJsonValidator<OAI3> {
  private static final String DEFINITION_ERR_MSG = "MultipleOf definition must be strictly greater than 0.";
  private static final String ERR_MSG = "Value '%s' is not a multiple of '%s'.";

  private final BigDecimal multiple;
  private final BigDecimal divisible = BigDecimal.valueOf(0.0);

  MultipleOfValidator(final ValidationContext<OAI3> context, final JsonNode schemaNode, final JsonNode schemaParentNode, final SchemaValidator parentSchema) {
    super(context, schemaNode, schemaParentNode, parentSchema);

    multiple = schemaNode.isNumber()
      ? schemaNode.decimalValue()
      : BigDecimal.valueOf(0.0);
  }

  @Override
  public void validate(final JsonNode valueNode, final ValidationResults results) {
    if (multiple.compareTo(divisible) <= 0) {
      results.addError(DEFINITION_ERR_MSG);
      return;
    }

    if (!valueNode.isNumber()) {
      return;
    }

    BigDecimal value = valueNode.decimalValue();
    BigDecimal remainder = value.remainder(multiple);
    if (remainder.compareTo(divisible) != 0) {
      results.addError(String.format(ERR_MSG, value, multiple), MULTIPLEOF);
    }
  }
}
