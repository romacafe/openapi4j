package org.openapi4j.parser.refs;

import org.junit.Test;
import org.openapi4j.core.model.OAIContext;
import org.openapi4j.core.model.reference.Reference;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Schema;

import java.net.URI;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RefsExample {
  @Test
  public void test_refResolve_same$ref_differentContent() throws Exception {
    // What happens if 2 $refs in different files use the same $ref string but point to different content?
    // It fails.  One of the refs will point to the wrong content
    OpenApi3Parser parser = new OpenApi3Parser();
    URL url = this.getClass().getResource("/api.yaml");
    OpenApi3 oas = parser.parse(url, false);
    OAIContext context = oas.getContext();
    URI baseUri = context.getBaseUri();

    Schema schema1 = oas.getComponents().getSchema("Schema1");
    String testType1RefString = schema1.getProperty("testType").getRef();
    Reference testType1Ref = context.getReferenceRegistry().getRef(baseUri, testType1RefString);
    Schema testType1 = testType1Ref.getMappedContent(Schema.class);

    String schema2RefString = oas.getComponents().getSchema("Schema2").getRef();
    Reference schema2Ref = context.getReferenceRegistry().getRef(baseUri, schema2RefString);
    Schema schema2 = schema2Ref.getMappedContent(Schema.class);
    String testType2RefString = schema2.getProperty("testType").getRef();
    Reference testType2Ref = context.getReferenceRegistry().getRef(testType2RefString);
    Schema testType2 = testType2Ref.getMappedContent(Schema.class);

    assertThat(testType1.getProperty("id").getType(), is("integer")); // fail
    assertThat(testType2.getProperty("id").getType(), is("string"));
  }
}

/*

    /
    +- api.yaml
    |  +- $ref testType.yaml#/TestType
    |  \- $ref schema2/schema2.yaml#/Schema2
    +- testType.yaml
    |  \- TestType (definition 1)
    \- schema2
       +- schema2.yaml
       |  \- $ref testType.yaml#/TestType
       \- testType.yaml
          \- TestType (definition 2)

 */
