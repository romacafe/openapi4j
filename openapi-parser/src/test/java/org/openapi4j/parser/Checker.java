package org.openapi4j.parser;

import org.json.JSONException;
import org.openapi4j.core.exception.DecodeException;
import org.openapi4j.core.exception.EncodeException;
import org.openapi4j.core.model.OAI;
import org.openapi4j.core.util.TreeUtil;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.core.validation.ValidationResults;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.validation.v3.OpenApi3Validator;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.net.URL;

public class Checker {
  protected ValidationResults validate(String path) throws Exception {
    URL specPath = getClass().getResource(path);

    // Check parsing with validation
    OpenApi3 api;

    if (specPath != null) {
      api = new OpenApi3Parser().parse(new File(specPath.toURI()), false);
    } else {
      api = new OpenApi3Parser().parse((File) null, false);
    }

    ValidationResults results;
    try {
      results = OpenApi3Validator.instance().validate(api);
    } catch (ValidationException ex) {
      System.out.println(ex.getResults());
      throw ex;
    }

    // Check output
    checkModel(specPath, api);

    return results;
  }

  protected void checkModel(URL resourcePath, OAI api) throws EncodeException, JSONException, DecodeException {
    Object obj = TreeUtil.load(resourcePath, Object.class);
    String expected = TreeUtil.toJson(obj);

    String actual = TreeUtil.toJson(api);

    JSONAssert.assertEquals(expected, actual, true);
  }
}
