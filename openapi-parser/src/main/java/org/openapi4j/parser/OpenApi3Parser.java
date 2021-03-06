package org.openapi4j.parser;

import org.openapi4j.core.exception.DecodeException;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.model.AuthOption;
import org.openapi4j.core.model.v3.OAI3Context;
import org.openapi4j.core.util.TreeUtil;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.validation.v3.OpenApi3Validator;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * The parser for Open API v3.x.x
 */
public class OpenApi3Parser extends OpenApiParser<OpenApi3> {
  private static final String NULL_SPEC_URL = "Failed to load spec from 'null' location";
  private static final String INVALID_SPEC = "Failed to load spec at '%s'";

  /**
   * {@inheritDoc}
   */
  @Override
  public OpenApi3 parse(URL url, List<AuthOption> authOptions, boolean validate) throws ResolutionException, ValidationException {
    if (url == null) {
      throw new ResolutionException(NULL_SPEC_URL);
    }

    OpenApi3 api;

    try {
      api = TreeUtil.load(url, authOptions, OpenApi3.class);
      api.setContext(new OAI3Context(url.toURI()));
    } catch (DecodeException | URISyntaxException e) {
      throw new ResolutionException(String.format(INVALID_SPEC, url.toString()), e);
    }

    if (validate) {
      OpenApi3Validator.instance().validate(api);
    }

    return api;
  }
}
