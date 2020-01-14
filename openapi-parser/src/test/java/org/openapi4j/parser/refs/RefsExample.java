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
    URL url = this.getClass().getResource("/refs/v3/similar/valid/api.yaml");
    String urlString = url.toString();
    OpenApi3 oas = parser.parse(url, false);
    OAIContext context = oas.getContext();

    String absoluteUriPrefix = urlString.substring(0, urlString.indexOf("api.yaml"));
    String correctRelativeUri = "schema2/schema2.yaml#/Schema2";
    String incorrectRelativeUri = "schema2/" + correctRelativeUri; // extra "schema2/" path element
    String correctAbsoluteUri = absoluteUriPrefix + correctRelativeUri;
    String incorrectAbsoluteUri = absoluteUriPrefix + incorrectRelativeUri;




    // Can't get Schema2 with the correct Absolute URI
    Reference correctAbsoluteReference = context.getReferenceRegistry().getRef(correctAbsoluteUri);
    Reference incorrectAbsoluteReference = context.getReferenceRegistry().getRef(incorrectAbsoluteUri);
//    assertThat(correctAbsoluteReference, is(notNullValue())); // fail
//    assertThat(incorrectAbsoluteReference, is(nullValue()));  // fail




    // This section works as expected
    Schema schema1 = oas.getComponents().getSchema("Schema1");
    String testType1RefString = schema1.getProperty("testType").getRef();
    Reference testType1Ref = context.getReferenceRegistry().getRef(testType1RefString);
    Schema testType1 = testType1Ref.getMappedContent(Schema.class);
    assertThat(testType1.getProperty("id").getType(), is("integer")); // pass




    // Can't get Schema2 with the correct relative URI (from the source OpenAPI spec)
    String schema2RefString = oas.getComponents().getSchema("Schema2").getRef();
    Reference correctSchema2RelativeReference = context.getReferenceRegistry().getRef(schema2RefString);
    Reference incorrectSchema2RelativeReference = context.getReferenceRegistry().getRef(incorrectRelativeUri);
//    assertThat(correctSchema2RelativeReference, is(notNullValue())); // fail
//    assertThat(incorrectSchema2RelativeReference, is(nullValue())); // fail




    // Need an API to get this value, something like Reference#getBaseUriOfTarget
    URI schema2BaseUri = URI.create(absoluteUriPrefix + "/schema2/schema2.yaml");
    // URI schema2BaseUri = correctSchema2RelativeReference.getBaseUriOfTarget();

    // Now I can walk the tree and get the correct reference and correct Schema
    Schema schema2 = incorrectSchema2RelativeReference.getMappedContent(Schema.class);
    String testType2RefString = schema2.getProperty("testType").getRef();
    Reference testType2Ref = context.getReferenceRegistry().getRef(schema2BaseUri, testType2RefString);
    Schema testType2 = testType2Ref.getMappedContent(Schema.class);

    assertThat(testType2.getProperty("id").getType(), is("string"));
  }
}
