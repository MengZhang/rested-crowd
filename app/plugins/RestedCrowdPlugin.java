package plugins;

//import org.codehaus.jackson.JsonNode;
import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.Play;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;

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
	Boolean isValid  = RestedCrowdPlugin
	    .crowdRequest("session/"+token)
	    .post(RestedCrowdPlugin.VALIDATION_FACTORS)
	    .map(
		 new Function<WSResponse, Boolean>() {
		     public Boolean apply(WSResponse res) throws Throwable {
			 int statusCode = res.getStatus();
			 if (statusCode == 200) {
			     return new Boolean(true);
			 } else {
			     return new Boolean(false);
			 }
		     }
		 }).get(30000);
       return isValid.booleanValue();
    }

    /**
     * Retrieve the session and get the email address
     *
     * @param String the SSO token
     * @return current session email address
     */
    public static String getCrowdEmail(String token) {
	return RestedCrowdPlugin.crowdRequest("session/"+token).get().map(
	    new Function<WSResponse, String>() {
		public String apply(WSResponse res) throws Throwable {
		    if(res.getStatus() != 200) {
			Logger.error("Error with status: "+res.getStatus()+" Body: "+res.getBody());
			return null;
		    } else {
			JsonNode j = res.asJson();
			return j.findPath("email").asText();
		    }
		}
	    }).get(30000);
    }


    /**
     * Retrieve the cookie settings from the server.
     *
     * @return cookie settings from Crowd server
     */
    public static CookieSettings getCookieSettings() {
        if (cookieSettings == null) {
	    JsonNode settings = RestedCrowdPlugin.crowdRequest("config/cookie")
                .get().map(
		    new Function<WSResponse, JsonNode>() {
			public JsonNode apply(WSResponse res) throws Throwable {
			    return res.asJson();
			}
		    }).get(30000);
            cookieSettings = new RestedCrowdPlugin.CookieSettings(settings.findPath("name").asText(),
                    settings.findPath("domain").asText(), false);
        }
        return cookieSettings;
    }

}
