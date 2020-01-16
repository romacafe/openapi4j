package org.openapi4j.parser.refs;

import org.junit.Test;
import org.openapi4j.core.exception.DecodeException;
import org.openapi4j.core.model.OAIContext;
import org.openapi4j.core.model.reference.Reference;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.AbsRefOpenApiSchema;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Schema;

import java.net.URI;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class RefsExample {
  @Test
  public void test_refResolve_same$ref_differentContent() throws Exception {
    // What happens if 2 $refs in different files use the same $ref string but point to different content?
    // It fails.  One of the refs will point to the wrong content
    OpenApi3Parser parser = new OpenApi3Parser();
    URL url = this.getClass().getResource("/refs/v3/similar/valid/api.yaml");
    OpenApi3 oas = parser.parse(url, false);
    OAIContext context = oas.getContext();

    Schema schema1 = oas.getComponents().getSchema("Schema1");
    Schema testType1 = schema1.getProperty("testType").getReference(context).getMappedContent(Schema.class);
    Schema schema2 = oas.getComponents().getSchema("Schema2").getReference(context).getMappedContent(Schema.class);
    Schema testType2 = schema2.getProperty("testType").getReference(context).getMappedContent(Schema.class);

    assertThat(testType1.getProperty("id").getType(), is("integer"));
    assertThat(testType2.getProperty("id").getType(), is("string"));
  }

  @Test
  public void test_withDeref() throws Exception {
    OpenApi3Parser parser = new OpenApi3Parser();
    URL url = this.getClass().getResource("/refs/v3/similar/valid/api.yaml");
    OpenApi3 oas = parser.parse(url, false);
    OAIContext context = oas.getContext();

    Schema schema1 = deReference(oas.getComponents().getSchema("Schema1"), context);
    Schema testType1 = deReference(schema1.getProperty("testType"), context);
    Schema schema2 = deReference(oas.getComponents().getSchema("Schema2"), context);
    Schema testType2 = deReference(schema2.getProperty("testType"), context);

    assertThat(testType1.getProperty("id").getType(), is("integer"));
    assertThat(testType2.getProperty("id").getType(), is("string"));
  }

  @SuppressWarnings("unchecked")
  private <T extends AbsRefOpenApiSchema<T>> T deReference(T model, OAIContext context) throws DecodeException {
    if (!model.isRef()) return model;
    return (T) model.getReference(context).getMappedContent(model.getClass());
  }
}
