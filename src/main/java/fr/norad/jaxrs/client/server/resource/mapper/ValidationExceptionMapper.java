/**
 *
 *     Copyright (C) norad.fr
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package fr.norad.jaxrs.client.server.resource.mapper;

import static fr.norad.jaxrs.client.server.resource.mapper.ExceptionMapperUtils.buildError;
import static fr.norad.jaxrs.client.server.resource.mapper.ExceptionMapperUtils.findMediaType;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO should be replace by javax.ws.rs.ValidationException processing
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String RESPOND = "Respond ValidationException";

    @Override
    public Response toResponse(ValidationException exception) {
        if (log.isDebugEnabled()) {
            log.debug(RESPOND, exception);
        } else {
            log.info(RESPOND, exception.getMessage());
        }
        return Response.status(Status.BAD_REQUEST).entity(buildError(exception)).type(findMediaType()).build();
    }
}
