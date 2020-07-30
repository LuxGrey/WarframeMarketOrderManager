import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * Handles communication with the Warframe Market API with HTTPS requests and responses
 */
public class APIRequestManager {
  private static final String BASE_URL = "https://api.warframe.market/v1";
  //minimum time to wait (in milliseconds) between separate requests to Warframe Market API
  //this comes from the constraint that only 3 requests per second should be made to
  //the Warframe Market API
  private static final long MIN_DELAY_BETWEEN_REQUESTS = 333;
  //timestamp with millisecond precision
  private static Date timestampLastAPIRequest;

  public static void init() {
    timestampLastAPIRequest = new Date();
  }

  /**
   * Requests the details for a specific order from the Warframe Market API.
   * @param orderId a String containing the ID of the order to request details for
   * @return a JSON String containing the details of the order
   */
  public static String getOrder(String orderId) {
    String responseBody = null;
    try {
      URL url = new URL(BASE_URL + "/profile/orders/" + orderId);
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      //add request headers
      setAuthHeaders(con);

      //get response
      int status = sendRequest(con);
      if(status > 299) {
        responseBody = getResponseBody(con.getErrorStream());
        throw new IOException(responseBody);
      }
      else {
        responseBody = getResponseBody(con.getInputStream());
      }

      //close connection
      con.disconnect();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    return responseBody;
  }

  /**
   * Request an update for a specific order with the provided data from the Warframe Market API.
   * @param orderID a String containing the ID for the order to request an update for
   * @param jsonOrderValues a JSON String containing the key-value pairs to update the order with
   */
  public static void updateOrder(String orderID, String jsonOrderValues) {
    try {
      URL url = new URL(BASE_URL + "/profile/orders/" + orderID);
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      con.setRequestMethod("PUT");

      //add request headers
      setAuthHeaders(con);

      //set request content
      setRequestBody(con, jsonOrderValues);

      int status = sendRequest(con);

      if(status > 299) {
        throw new IOException(getResponseBody(con.getErrorStream()));
      }
      else {
        String newJWT = extractJWTFromResponseHeaders(con.getHeaderFields());
        PropertyManager.setJWT(newJWT);
      }
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Requests info for all items known to Warframe Market.
   * @return a JSON String containing information about all items
   */
  public static String getAllItemsInfo() {
    String responseBody = null;
    try {
      URL url = new URL(BASE_URL + "/items");
      responseBody = handleStandardGETRequest(url);
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    return responseBody;
  }

  /**
   * Requests all info about a specified item from Warframe Market.
   * @param urlName item name used for the URL which contains the item's info
   * @return a JSON String containing all information about the item
   */
  public static String getItemInfo(String urlName) {
    String responseBody = null;
    try{
      URL url = new URL(BASE_URL + "/items/" + urlName);
      responseBody = handleStandardGETRequest(url);
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    return responseBody;
  }

  /**
   * Retrieves a list of all orders from the Warframe Market profile of the user;
   * specifically all orders visible to the authenticated Warframe Market profile owner.
   * @return a JSON String containing all orders of the user
   */
  public static String getAllOrdersFromOwnProfile() {
    String responseBody = null;
    try {
      URL url = new URL(BASE_URL + "/profile/" + PropertyManager.getUserName() + "/orders");
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      //add request headers
      setAuthHeaders(con);

      //get response
      int status = sendRequest(con);
      if(status > 299) {
        responseBody = getResponseBody(con.getErrorStream());
        throw new IOException(responseBody);
      }
      else {
        responseBody = getResponseBody(con.getInputStream());
      }

      //close connection
      con.disconnect();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    return responseBody;
  }

  /**
   * Takes care of building and sending a regular HTTPS GET request and returns its response body.
   * as a JSON String
   * @param url the URL to send the HTTPS GET request to
   * @return a JSON String containing the response body
   * @throws IOException
   */
  private static String handleStandardGETRequest(URL url) throws IOException {
    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setRequestMethod("GET");

    //add request headers
    setCommonHeaders(con);

    //get response
    int status = sendRequest(con);
    String responseBody = null;
    if(status > 299) {
      responseBody = getResponseBody(con.getErrorStream());
      throw new IOException(responseBody);
    }
    else {
      responseBody = getResponseBody(con.getInputStream());
    }

    //close connection
    con.disconnect();

    return responseBody;
  }

  /**
   * Reads the body of a HTTPS response from an InputStream and returns it as a JSON String.
   * @param instream the InputStream object to read the response from
   * @return a JSON String containing the response body
   * @throws IOException
   */
  private static String getResponseBody(InputStream instream) throws IOException {
    StringBuilder responseBuilder = new StringBuilder();
    try(BufferedReader br = new BufferedReader(
        new InputStreamReader(instream, StandardCharsets.UTF_8))) {
      String responseLine;
      while((responseLine = br.readLine()) != null) {
        responseBuilder.append(responseLine.trim());
      }
    }
    return responseBuilder.toString();
  }

  /**
   * Tries to locate and extract the JSON Web Token from the headers of a HTTPS response.
   * @param responseHeaders map with all headers
   * @return returns a String containing the JSON Web Token extracted from the headers
   */
  private static String extractJWTFromResponseHeaders(Map<String, List<String>> responseHeaders) {
    for(Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
      if(entry.getKey() == null || !entry.getKey().contains("Authorization")) {
        continue;
      }
      //retrieves the JWT String from the Authorization header and cuts the 4 leading "JTW " chars
      return entry.getValue().get(0).substring(4);
    }
    return null;
  }

  /**
   * Attempts to send the request from the passed con argument and returns the response code.
   * @param con the HttpsURLConnection object from which the request should be sent
   * @return an int containing the HTTPS response code
   */
  private static int sendRequest(HttpsURLConnection con) {
    //if necessary wait long enough so that a third of a second has passed since the last
    //request made to the API
    long timeSinceLastRequest;
    if( (timeSinceLastRequest = new Date().getTime() - timestampLastAPIRequest.getTime())
        < MIN_DELAY_BETWEEN_REQUESTS) {
      try {
        Thread.sleep(MIN_DELAY_BETWEEN_REQUESTS - timeSinceLastRequest);
      }
      catch (InterruptedException e) {
        System.out.println(e.getMessage());
        System.exit(-1);
      }
    }
    int responseCode = 0;
    try {
      responseCode = con.getResponseCode();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }
    //getResponseCode() implicitly sends the HTTPS request to the API,
    //therefore refresh the timestamp for the last request
    timestampLastAPIRequest = new Date();

    return responseCode;
  }

  /**
   * Sets the body of a HTTPS request from a JSON String.
   * @param con the HttpsURLConnection object to set the HTTPS request body of
   * @param jsonContent a JSON String with the content that will be written into the request body
   * @throws IOException
   */
  private static void setRequestBody(HttpsURLConnection con, String jsonContent)
      throws IOException {
    con.setDoOutput(true);
    try(OutputStream os = con.getOutputStream()) {
      byte[] input = jsonContent.getBytes(StandardCharsets.UTF_8);
      os.write(input, 0, input.length);
    }
  }

  /**
   * Adds HTTPS headers that are common for all API requests to HTTPSURLConnection object.
   * @param con the HTTPSURLConnection object to add the headers to
   */
  private static void setCommonHeaders (HttpsURLConnection con) {
    con.setRequestProperty("Content-Type", "application/json; utf-8");
    con.setRequestProperty("accept", "application/json");
    con.setRequestProperty("language", "en");
    con.setRequestProperty("platform", "pc");
  }

  /**
   * Adds HTTPS all headers that are required for authenticated API requests to HTTPSURLConnection object.
   * @param con the HTTPSURLConnection object to add the headers to
   */
  private static void setAuthHeaders (HttpsURLConnection con) {
    setCommonHeaders(con);
    con.setRequestProperty("auth_type", "header");
    con.setRequestProperty("Authorization", "JWT " + PropertyManager.getJWT());
  }
}
