import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Processes more complicated JSON Strings from Warframe Market API responses
 * and makes complicated decisions based on API data. Very complex.
 */
public class JSONProcessor {
  private static final String[] SYNDICATE_SOURCES = {
      "New Loka", "The Perrin Sequence", "Red Veil",
      "Steel Meridian", "Cephalon Suda", "Arbiters of Hexis"
  };

  /**
   * Updates all sell orders on the user's Warframe Market profile that require it,
   * based on the visibility settings for Syndicates within the program.
   * @return an int containing the number of orders that have been updated
   */
  public static int updateAffectedOrders() {
    int updatedOrders = 0;
    String jsonAllUserOrders = APIRequestManager.getAllOrdersFromOwnProfile();
    JSONArray sellOrders = new JSONObject(jsonAllUserOrders)
        .getJSONObject("payload").getJSONArray("sell_orders");
    for(int i = 0; i < sellOrders.length(); i++) {
      JSONObject currentOrder = sellOrders.getJSONObject(i);
      if(!isUpdateCandidate(currentOrder)) {
        continue;
      }

      String orderId = currentOrder.getString("id");
      //used to build JSON String
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("order_id", orderId);
      jsonObject.put("platinum", currentOrder.getFloat("platinum"));
      jsonObject.put("quantity", currentOrder.getInt("quantity"));
      //for the update simply invert the current visibility boolean from the order
      jsonObject.put("visible", !(currentOrder.getBoolean("visible")));
      if(currentOrder.has("mod_rank")) {
        jsonObject.put("mod_rank", currentOrder.getInt("mod_rank"));
      }

      String updateOrderRequestBody = jsonObject.toString();
      APIRequestManager.updateOrder(orderId, updateOrderRequestBody);
      updatedOrders++;
    }

    return updatedOrders;
  }

  /**
   * Determines whether an item order should have its visibility updated on Warframe Market.
   * @param order a JSONObject containing the order to be checked
   * @return a boolean stating whether the order is an update candidate (true) or not (false)
   */
  private static boolean isUpdateCandidate(JSONObject order) {
    List<String> syndicateDropSources = itemDropsFromSyndicates(order);
    //early return when item from order is not available from a Syndicate in the first place
    if(syndicateDropSources.isEmpty()) {
      return false;
    }

    boolean currentOrderVisibility = order.getBoolean("visible");
    boolean desiredOrderVisibility = getTotalVisibility(syndicateDropSources);

    //if both visibility values do not match, the order is an update candidate (true)
    return currentOrderVisibility != desiredOrderVisibility;
  }

  /**
   * Takes a JSONObject with an item order and determines which Syndicates, if any, provide the item.
   * @param order a JSONObject containing the item order for which the syndicate drop sources should be determined
   * @return a List of Strings naming all Syndicate drop sources for the item; empty if it doesn't have any
   */
  private static List<String> itemDropsFromSyndicates(JSONObject order) {
    String itemUrlName = order.getJSONObject("item").getString("url_name");
    String jsonItemDetails = APIRequestManager.getItemInfo(itemUrlName);

    JSONArray itemDropSources = getItemDropSources(jsonItemDetails);
    List<String> syndicateDropSources = new ArrayList<>();
    for(int i = 0; i < itemDropSources.length(); i++) {
      String currentDropSource = itemDropSources.getJSONObject(i).getString("name");
      String currentSyndicate = stringIsSyndicate(currentDropSource);
      if(currentSyndicate != null) {
        syndicateDropSources.add(currentSyndicate);
      }
    }

    return syndicateDropSources;
  }

  /**
   * Takes a JSON String with the full details for an item and returns that item's drop sources in a JSONArray.
   * @param jsonItemDetails a JSON String containing the item details from which the drop source info is extracted
   * @return a JSONArray containing all drop sources
   */
  private static JSONArray getItemDropSources(String jsonItemDetails) {
    //id used for locating requested item within items_in_set array
    String itemId = new JSONObject(jsonItemDetails)
        .getJSONObject("payload").getJSONObject("item").getString("id");
    //array with all items that share a set with the requested item
    JSONArray itemsInSetArray = new JSONObject(jsonItemDetails)
        .getJSONObject("payload").getJSONObject("item").getJSONArray("items_in_set");
    //array containing all drop sources for the requested item
    JSONArray itemDropSources = null;

    //if only 1 element is in the array it automatically is the requested item
    if(itemsInSetArray.length() == 1) {
      itemDropSources = itemsInSetArray.getJSONObject(0)
          .getJSONObject("en").getJSONArray("drop");
    }
    else {
      //locate requested item in array
      for(int i = 0; i < itemsInSetArray.length(); i++) {
        String currentItemId = itemsInSetArray.getJSONObject(i).getString("id");
        if(currentItemId.equals(itemId)) {
          itemDropSources = itemsInSetArray.getJSONObject(i)
              .getJSONObject("en").getJSONArray("drop");
        }
      }
    }

    return itemDropSources;
  }

  /**
   * Checks if a given String contains any of the Strings from a constant member and if so
   * returns that member. Otherwise, if there is no match, it returns null.
   * @param inputStr the String for which shall be checked if it matches any of the list items
   * @return a String containing the matched Syndicate String constant; null if no match could be found
   */
  private static String stringIsSyndicate(String inputStr) {
    for(int i = 0; i < SYNDICATE_SOURCES.length; i++) {
      if(inputStr.contains(SYNDICATE_SOURCES[i])) {
        return SYNDICATE_SOURCES[i];
      }
    }
    return null;
  }

  /**
   * Takes a List of Strings describing Syndicate drop sources and determines
   * based on the visibilities of these Syndicates in the program whether an item order
   * with these Syndicate drop sources should be visible or invisible.
   * Decision criteria: if one ore multiple Syndicates from the drop sources are set as visible,
   * then a corresponding order should be visible;
   * if all involved Syndicates are invisible, the order should be invisible.
   * @param syndicateDropSources
   * @return a boolean expression whether a corresponding order should be visible; true = 'visible', false = 'invisible'
   */
  private static boolean getTotalVisibility(List<String> syndicateDropSources) {
    for(String sds : syndicateDropSources) {
      boolean currentVisibility = false;
      switch(sds) {
        case "New Loka":
          currentVisibility = PropertyManager.getVisibleNewLoka();
          break;
        case "The Perrin Sequence":
          currentVisibility = PropertyManager.getVisibleThePerrinSequence();
          break;
        case "Red Veil":
          currentVisibility = PropertyManager.getVisibleRedVeil();
          break;
        case "Steel Meridian":
          currentVisibility = PropertyManager.getVisibleSteelMeridian();
          break;
        case "Cephalon Suda":
          currentVisibility = PropertyManager.getVisibleCephalonSuda();
          break;
        case "Arbiters of Hexis":
          currentVisibility = PropertyManager.getVisibleArbitersOfHexis();
          break;
      }
      if(currentVisibility) {
        return true;
      }
    }
    return false;
  }
}
