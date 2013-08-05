package plugins;

import play.Play;
import play.libs.WS;
import play.libs.WS.WSRequestHolder;
import org.codehaus.jackson.JsonNode;

/**
 * @author Christopher Villalobos
 * @version 1.0
 * @todo Add logging
 * @todo Add Authenticator support for play framework.
 */

public enum RestedCrowdPlugin {
    ;
    /**
     * Validation Factors are a commonly used string in the Crowd REST API.
     * Defaults to localhost installation of Crowd
     */
    public static String VALIDATION_FACTORS = "{\"validationFactors\" : [{\"name\" : \"remote_address\",\"value\" : \""+
        Play.application().configuration().getString("crowd.localaddress", "127.0.0.1")+
        "\"}]}";

    /**
     * The base REST URL for the CROWD SSO.
     * Defaults to localhost installation of Crowd
     */
    public static String AUTH_URL = Play.application().configuration()
        .getString("crowd.baseurl",
                "http://localhost:8095/crowd/rest/usermanagement/latest/");

    public static class CookieSettings {
        private String name;
        private String domain;
        private boolean secureOnly;

        public CookieSettings(String name, String domain, boolean secureOnly) {
            this.name = name;
            this.domain = domain;
            this.secureOnly = secureOnly;
        }

        public String getName() {
            return name;
        }

        public String getDomain() {
            return domain;
        }

        public boolean isSecure() {
            return secureOnly;
        }
    }

    private static CookieSettings cookieSettings = null;

    /**
     * A generic requester for Crowd SSO.
     * Uses <pre>crowd.app.name</pre> and <pre>crowd.app.password</pre> for BASIC
     * Authentication to the server. Please see the Crowd SSO manual for details
     * for each call.
     *
     * @param String the Crowd URL path (without leading /)
     * @return A request holder which allows HTTP methods to be called on it.
     */
    public static WSRequestHolder crowdRequest(String path) {
        if (path == null) {
            path = "";
        }
        return WS.url(AUTH_URL+path)
            .setAuth(Play.application().configuration().getString("crowd.app.name"),
                    Play.application().configuration().getString("crowd.app.password"))
            .setContentType("application/json")
            .setHeader("Accept", "application/json");
    }

    /**
     * Validates a Crowd SSO token and keeps session alive.
     *
     * @param String the SSO token
     * @return is the current session valid
     */
    public static boolean validCrowdSession(String token) {
        int statusCode = RestedCrowdPlugin.crowdRequest("session/"+token)
            .post(RestedCrowdPlugin.VALIDATION_FACTORS).get().getStatus();
        if (statusCode == 200) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieve the session and get the email address
     *
     * @param String the SSO token
     * @return current session email address
     */
    public static String getCrowdEmail(String token) {
        return RestedCrowdPlugin.crowdRequest("session/"+token)
            .get().get().asJson().findPath("email").asText();
    }

    /**
     * Retrieve the cookie settings from the server.
     *
     * @return cookie settings from Crowd server
     */
    public static CookieSettings getCookieSettings() {
        if (cookieSettings == null) {
            JsonNode settings = RestedCrowdPlugin.crowdRequest("config/cookie")
                .get().get().asJson();
            cookieSettings = new RestedCrowdPlugin.CookieSettings(settings.findPath("name").asText(),
                    settings.findPath("domain").asText(), false);
        }
        return cookieSettings;
    }

}
