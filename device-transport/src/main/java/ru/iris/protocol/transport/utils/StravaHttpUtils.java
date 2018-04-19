package ru.iris.protocol.transport.utils;

import javastrava.api.v3.auth.AuthorisationService;
import javastrava.api.v3.auth.impl.retrofit.AuthorisationServiceImpl;
import javastrava.api.v3.auth.model.Token;
import javastrava.api.v3.auth.ref.AuthorisationApprovalPrompt;
import javastrava.api.v3.auth.ref.AuthorisationResponseType;
import javastrava.api.v3.auth.ref.AuthorisationScope;
import javastrava.api.v3.service.exception.BadRequestException;
import javastrava.api.v3.service.exception.StravaInternalServerErrorException;
import javastrava.api.v3.service.exception.UnauthorizedException;
import javastrava.config.StravaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Nikolay Viguro, 19.04.18
 */

@Slf4j
public class StravaHttpUtils {
    private final AuthorisationResponseType DEFAULT_RESPONSE_TYPE = AuthorisationResponseType.CODE;
    private final String DEFAULT_REDIRECT_URI = "http://localhost/redirects";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Integer clientId;
    private final String clientSecret;

    public StravaHttpUtils(String clientId, String clientSecret) {
        this.clientId = Integer.valueOf(clientId);
        this.clientSecret = clientSecret;
    }

    private String acceptApplication(final String authenticityToken, final AuthorisationScope... scopes) {
        String scopeString = ""; //$NON-NLS-1$
        for (final AuthorisationScope scope : scopes) {
            scopeString = scopeString + scope.toString() + ","; //$NON-NLS-1$
        }
        String location = null;
        try {
            final HttpUriRequest post = RequestBuilder.post().setUri(new URI(StravaConfig.AUTH_ENDPOINT + "/oauth/accept_application")) //$NON-NLS-1$
                    .addParameter("client_id", clientId.toString()).addParameter("redirect_uri", DEFAULT_REDIRECT_URI) //$NON-NLS-1$ //$NON-NLS-2$
                    .addParameter("response_type", DEFAULT_RESPONSE_TYPE.toString()).addParameter("authenticity_token", authenticityToken) //$NON-NLS-1$ //$NON-NLS-2$
                    .addParameter("scope", scopeString).build(); //$NON-NLS-1$
            final CloseableHttpResponse response2 = httpClient.execute(post);
            final int status = response2.getStatusLine().getStatusCode();
            if (status != 302) {
                throw new StravaInternalServerErrorException(post.getMethod() + " " + post.getURI() + " returned status code " //$NON-NLS-1$ //$NON-NLS-2$
                        + Integer.valueOf(status).toString(), null, null);
            }
            try {
                final HttpEntity entity = response2.getEntity();
                location = response2.getFirstHeader("Location").getValue();
                EntityUtils.consume(entity);

            } finally {
                response2.close();
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        // Get the code parameter from the redirect URI
        if (location.contains("&code=")) { //$NON-NLS-1$
            return location.split("&code=")[1].split("&")[0];
        } else {
            return null;
        }

    }

    private String approveApplication(final AuthorisationScope... scopes) {
        final String authenticityToken = getAuthorisationPageAuthenticityToken(scopes);
        return acceptApplication(authenticityToken, scopes);
    }

    private String getAuthorisationPageAuthenticityToken(final AuthorisationScope... scopes) {
        String scopeString = ""; //$NON-NLS-1$
        for (final AuthorisationScope scope : scopes) {
            if (!scopeString.equals("")) { //$NON-NLS-1$
                scopeString = scopeString + ","; //$NON-NLS-1$
            }
            scopeString = scopeString + scope.toString();
        }
        Document authPage;
        try {
            if (scopeString.equals("")) { //$NON-NLS-1$
                authPage = httpGet(StravaConfig.AUTH_ENDPOINT + "/oauth/authorize", //$NON-NLS-1$
                        new BasicNameValuePair("client_id", clientId.toString()), //$NON-NLS-1$
                        new BasicNameValuePair("response_type", //$NON-NLS-1$
                                DEFAULT_RESPONSE_TYPE.toString()),
                        new BasicNameValuePair("redirect_uri", DEFAULT_REDIRECT_URI), new BasicNameValuePair( //$NON-NLS-1$
                                "approval_prompt", AuthorisationApprovalPrompt.FORCE.toString())); //$NON-NLS-1$
            } else {
                authPage = httpGet(StravaConfig.AUTH_ENDPOINT + "/oauth/authorize", //$NON-NLS-1$
                        new BasicNameValuePair("client_id", clientId.toString()), //$NON-NLS-1$
                        new BasicNameValuePair("response_type", //$NON-NLS-1$
                                DEFAULT_RESPONSE_TYPE.toString()),
                        new BasicNameValuePair("redirect_uri", DEFAULT_REDIRECT_URI), //$NON-NLS-1$
                        new BasicNameValuePair("approval_prompt", AuthorisationApprovalPrompt.FORCE.toString()), new BasicNameValuePair("scope", scopeString)); //$NON-NLS-1$//$NON-NLS-2$

            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final Elements authTokens = authPage.select("input[name=authenticity_token]"); //$NON-NLS-1$
        if ((authTokens == null) || (authTokens.first() == null)) {
            return null;
        }
        return authTokens.first().attr("value"); //$NON-NLS-1$
    }

    private String getLoginAuthenticityToken() {
        final BasicNameValuePair[] params = null;
        Document loginPage;
        try {
            loginPage = httpGet(StravaConfig.AUTH_ENDPOINT + "/login", params); //$NON-NLS-1$
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final Elements authTokens = loginPage.select("input[name=\"authenticity_token\"]"); //$NON-NLS-1$
        if (authTokens.isEmpty()) {
            return null;
        }
        return authTokens.first().attr("value"); //$NON-NLS-1$
    }

    public Token getStravaAccessToken(final String username, final String password, final AuthorisationScope... scopes) throws BadRequestException, UnauthorizedException {
        final AuthorisationService service = new AuthorisationServiceImpl();
        final String authenticityToken = getLoginAuthenticityToken();
        login(username, password, authenticityToken);
        final String approvalCode = approveApplication(scopes);

        return service.tokenExchange(clientId, clientSecret, approvalCode, scopes);
    }

    @SuppressWarnings("resource")
    private Document httpGet(final String uri, final NameValuePair... parameters) throws IOException {
        HttpUriRequest get;
        Document page;
        if (parameters == null) {
            get = RequestBuilder.get(uri).build();
        } else {
            get = RequestBuilder.get(uri).addParameters(parameters).build();
        }
        final CloseableHttpResponse response = httpClient.execute(get);
        final int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            throw new StravaInternalServerErrorException("GET " + get.getURI() + " returned status " + Integer.valueOf(status).toString(), null, null); //$NON-NLS-1$ //$NON-NLS-2$
        }
        try {
            final HttpEntity entity = response.getEntity();
            page = Jsoup.parse(EntityUtils.toString(entity));

            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        return page;
    }

    private String login(final String email, final String password, final String authenticityToken) {
        String location = null;
        try {
            final HttpUriRequest login = RequestBuilder.post().setUri(new URI(StravaConfig.AUTH_ENDPOINT + "/session")).addParameter("email", email) //$NON-NLS-1$ //$NON-NLS-2$
                    .addParameter("password", password).addParameter("authenticity_token", authenticityToken).addParameter("utf8", "✓").build(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            final CloseableHttpResponse response2 = httpClient.execute(login);
            final int status = response2.getStatusLine().getStatusCode();
            if (status != 302) {
                throw new StravaInternalServerErrorException("POST " + login.getURI() + " returned status " + Integer.valueOf(status).toString(), null, null); //$NON-NLS-1$ //$NON-NLS-2$
            }
            try {
                final HttpEntity entity = response2.getEntity();
                location = response2.getFirstHeader("Location").getValue(); //$NON-NLS-1$
                EntityUtils.consume(entity);

            } finally {
                response2.close();
            }
        } catch (final IOException | URISyntaxException e) {
            logger.error("Strava error", e);
        }

        return location;

    }

}
