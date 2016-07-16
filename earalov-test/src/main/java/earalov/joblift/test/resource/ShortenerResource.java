package earalov.joblift.test.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import earalov.joblift.test.dao.IShortenerDao;
import earalov.joblift.test.dao.StorageException;
import earalov.joblift.test.dao.UrlEntry;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Resource for shortening and unshortening URLs.
 */
@Service
@Path("/shortener")
public class ShortenerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortenerResource.class);

    private final IShortenerDao dao;
    private final IConverter converter;

    private String baseUrl;

    @Inject
    public ShortenerResource(@Nonnull IShortenerDao dao, @Nonnull IConverter converter) {
        this.dao = dao;
        this.converter = converter;
    }

    /**
     * @param baseUrl Base URL of  shortener site
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Shortens provided URL.
     *
     * @param request contains URL.
     * @param stripUrl if {@code true}, session, login, utm and such will be removed from URL.
     * @return HTTP response.
     */
    @POST
    @Path("urls")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Nonnull
    public Response shortenURL(ShortenRequest request, @QueryParam("strip") boolean stripUrl) {
        UrlValidator urlValidator =
                new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS | UrlValidator.ALLOW_ALL_SCHEMES | UrlValidator.ALLOW_2_SLASHES);
        String url = request.getUrl();
        if (!urlValidator.isValid(url)) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "Invalid URL");
        }
        if (url.startsWith(baseUrl)) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "This URL can't be shortened");
        }
        if (stripUrl) {
            url = url.replaceAll("&(utm|session|sessionid|login|password|pass|user|email)([_a-z0-9=]+)", "")
                    .replaceAll("\\?(utm|session|sessionid|login|password|pass|user|email)([_a-z0-9=]+)", "?")
                    .replaceAll("\\?&", "?")
                    .replaceAll("\\?$", "");//strip tracking, login and session
        }
        try {
            URL aURL = new URL(url);
            final String host = aURL.getHost();
            String[] parts = host.split("\\.");
            if (parts.length > 0) {
                dao.IncrementTld(parts[parts.length - 1]);
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("Error happened when incrementing TLD counter for url: {} (error={})", url, e.getMessage());
        }
        String shortened;
        try {
            long counter = dao.IncrementCounter();
            final String hash = converter.convert(counter);
            dao.SaveURL(hash, new UrlEntry(counter, url));
            shortened = String.format("%s/%s", baseUrl, hash);
        } catch (StorageException e) {
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, String.format("Unable to write to storage: %s", e.getMessage()));
        }
        try {
            return buildOkResponse(new ShortenResponse(shortened));
        } catch (JsonProcessingException e) {
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, String.format("Error happened on returing reusult: %s", e.getMessage()));
        }
    }

    /**
     * Unshortents URL for provided key.
     *
     * @param urlHash shortented URL key
     * @return HTTP response.
     */
    @GET
    @Path("urls/{url-hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @Nonnull
    public Response unshortenURL(@PathParam("url-hash") String urlHash) throws JsonProcessingException {
        UrlEntry entry;
        try {
            entry = dao.GetUrl(urlHash);
        } catch (StorageException e) {
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, String.format("Unable to read from storage: %s", e.getMessage()));
        }
        if (entry == null) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "Requested shortened URL key wasn't found!");
        }
        try {
            return buildOkResponse(new LongURL(entry.getLongUrl()));
        } catch (JsonProcessingException e) {
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, String.format("Error happened on returing reusult: %s", e.getMessage()));
        }
    }

    /**
     * @return Top level domains statistics (how many urls were shortened for given domain)
     */
    @GET
    @Path("tld-stats")
    @Produces(MediaType.APPLICATION_JSON)
    @Nonnull
    public Response getURL() {
        try {
            return buildOkResponse(dao.GetTldStats());
        } catch (JsonProcessingException e) {
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, String.format("Error happened on returing reusult: %s", e.getMessage()));
        }
    }

    @Nonnull
    private Response buildOkResponse(Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        final Response.ResponseBuilder responseBuilder =
                Response.status(Response.Status.OK).entity(mapper.writeValueAsString(value));
        return responseBuilder.build();
    }

    @Nonnull
    private Response buildErrorResponse(final Response.Status responseCode, final String error) {
        ObjectMapper mapper = new ObjectMapper();
        final Response.ResponseBuilder responseBuilder;
        try {
            responseBuilder = Response.status(responseCode).entity(mapper.writeValueAsString(new Error(error)));
            return responseBuilder.build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); //that won't happen
        }
    }

    /**
     * Wrapper for error messahe.
     */
    private static class Error {
        @JsonProperty(value = "error", required = true)
        private String error;

        public Error(String error) {
            this.error = error;
        }
    }

    /**
     * Wrapper for unshortened url.
     */
    public static class LongURL {
        @JsonProperty(value = "url", required = true)
        private String url;

        public LongURL(String url) {
            this.url = url;
        }
    }

    /**
     * Wrapper for shortened url.
     */
    public class ShortenResponse {

        @JsonProperty(value = "short-url", required = true)
        private String shortenedURL;

        public ShortenResponse(String shortenedURL) {
            this.shortenedURL = shortenedURL;
        }
    }
}
