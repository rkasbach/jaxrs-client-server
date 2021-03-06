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

import java.io.*;
import java.lang.annotation.Annotation;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;

@Provider
public class GenericResponseExceptionMapper implements ResponseExceptionMapper<Exception> {

    private static final String SERVER_ERROR = "Error from server";
    private final MessageBodyReader<Error> reader;

    @SuppressWarnings("unchecked")
    public GenericResponseExceptionMapper(@SuppressWarnings("rawtypes") MessageBodyReader jsonReader) {
        this.reader = jsonReader;
    }

    @SuppressWarnings("unchecked")
    public static <T, U> MultivaluedMap<T, U> cast(MultivaluedMap<?, ?> p, Class<T> t, Class<U> u) {
        return (MultivaluedMap<T, U>) p;
    }

    private static String fromStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }

    protected Error findError(Response r) {
        String contentType = (String) r.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
        String content;
        try {
            content = fromStream((InputStream) r.getEntity());
        } catch (IOException e1) {
            throw new RuntimeException("Cannot read content of errorModel", e1);
        }
        if (MediaType.APPLICATION_JSON.equals(contentType)) {
            try {
                return reader.readFrom(Error.class, Error.class, new Annotation[]{},
                        MediaType.APPLICATION_JSON_TYPE, cast(r.getMetadata(), String.class, String.class),
                        new ByteArrayInputStream(content.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("Unparsable json errorModel content : " + content, e);
            }
        } else if (MediaType.APPLICATION_XML.equals(contentType)) {
            try {
                JAXBContext jc = JAXBContext.newInstance(Error.class);
                Unmarshaller u = jc.createUnmarshaller();
                return (Error) u.unmarshal(new ByteArrayInputStream(content.getBytes()));
            } catch (JAXBException e) {
                throw new RuntimeException("Unparsable xml error content : " + content, e);
            }
        }
        throw new RuntimeException("Unparsable errorModel content type : " + contentType);
    }

    @Override
    public Exception fromResponse(Response r) {
        Error error = findError(r);
        Exception exception = null;
        if (error.getException() != null) {
            try {
                if (WebApplicationException.class.isAssignableFrom(error.getException())) {
                    exception = error.getException().getConstructor(Response.class).newInstance(r);
                } else {
                    exception = error.getException().getConstructor(String.class)
                            .newInstance(error.getMessage() == null ? "" : error.getMessage());
                }
            } catch (Exception e) {
                try {
                    exception = (Exception) error.getException().getSuperclass().getConstructor(String.class)
                            .newInstance(error.getMessage() == null ? "" : error.getMessage());
                } catch (Exception e2) {
                    throw new RuntimeException("Cannot Create Exception from Error : " + error, e);
                }
            }
        } else {
            exception = new RuntimeException(error.getMessage() == null ? SERVER_ERROR : error.getMessage());
        }
        return exception;
    }
}
