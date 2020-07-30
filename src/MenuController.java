import java.util.Scanner;

/**
 * Takes care of all user console input to navigate menus.
 */
public class MenuController {

  /**
   * Starts the menu loop for user inputs. Exits loop if user inputs 'exit'.
   */
  public static void menuLoop() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to the Warframe Market order manager.\n"
        + "Input 'help' to get a list of your options.");
    String userInputLine = "";
    do {
      System.out.print("> ");
      userInputLine = scanner.nextLine();
      String[] inputSplit = userInputLine.split(" ");
      switch(inputSplit[0]) {
        case "help":
          printHelp();
          break;
        case "syndicate":
          setSyndicateVisibility(inputSplit[1], inputSplit[2]);
          break;
        case "update":
          updateSiteOrderStatus();
          break;
        case "status":
          printCurrentSyndicateStatus();
          break;
        case "username":
          setUserName(inputSplit[1]);
          break;
        case "jwt":
          setJWT(inputSplit[1]);
          break;
        case "exit":
          break;
        default:
          System.out.println("Invalid input, try again.");
      }
    } while (!userInputLine.equals("exit"));
  }

  /**
   * Prints a help message to console that lists and explains all user commands.
   */
  private static void printHelp() {
    System.out.println("User options:\n"
        + "'help' - prints this user options info\n"
        + "'syndicate [loka/perrin/veil/meridian/suda/arbiters] [visible/invisible]' - sets new visibility status for syndicate\n"
        + "'update' - updates status of orders on Warframe Market according to syndicate visibilities\n"
        + "'status' - prints current stored username and visibility setting for all syndicates\n"
        + "'username <username>' - set username of your Warframe Market profile so that it can be found by the application\n"
        + "'jwt <jwt>' - set the JSON Web Token that will be used for authenticated requests to Warframe Market\n"
        + "'exit' - close this program"
    );
  }

  /**
   * Calls for an update of a Syndicate's visibility in this program.
   * @param syndicate a String containing the Syndicate name for which the visibility should be updated in the program
   * @param visibility a String containing the new visibility for the Syndicate
   */
  private static void setSyndicateVisibility(String syndicate, String visibility) {
    boolean visible;
    try {
      visible = visibilityStringToBoolean(visibility);
    }
    catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return;
    }

    switch (syndicate) {
      case "loka":
        PropertyManager.setVisibleNewLoka(visible);
        break;
      case "perrin":
        PropertyManager.setVisibleThePerrinSequence(visible);
        break;
      case "veil":
        PropertyManager.setVisibleRedVeil(visible);
        break;
      case "meridian":
        PropertyManager.setVisibleSteelMeridian(visible);
        break;
      case "suda":
        PropertyManager.setVisibleCephalonSuda(visible);
        break;
      case "arbiters":
        PropertyManager.setVisibleArbitersOfHexis(visible);
        break;
      default:
        System.out.println("Invalid argument for Syndicate");
        break;
    }
  }

  /**
   * Converts a String expressing a visibility status to its corresponding boolean value.
   * @param visibility a String expressing visibility
   * @return a boolean expressing visibility; true = 'visible', false = 'invisible'
   * @throws IllegalArgumentException if passed visibility argument is invalid
   */
  private static boolean visibilityStringToBoolean(String visibility) throws IllegalArgumentException {
    switch (visibility) {
      case "visible":
        return true;
      case "invisible":
        return false;
      default:
        throw new IllegalArgumentException("Invalid argument for visibility");
    }
  }

  /**
   * Calls for the update of all sell orders on Warframe Market that require it, based on the
   * current settings for Syndicate visibility in the program.
   * Also tells the user how many orders have been updated.
   */
  private static void updateSiteOrderStatus() {
    System.out.println("Updating sell orders on Warframe Market.\n"
        + "This may take a couple minutes...");
    int updatedOrders = JSONProcessor.updateAffectedOrders();
    if(updatedOrders > 0) {
      System.out.println("Updated " + updatedOrders + " orders.");
    } else {
      System.out.println("No orders were updated.");
    }
  }

  /**
   * Prints the current visibility status within this program for all Syndicates.
   */
  private static void printCurrentSyndicateStatus() {
    System.out.println("Current visibility status within program for all Synicates:"
        + "\nNew Loka: " + visibilityBooleanToString(PropertyManager.getVisibleNewLoka())
        + "\nThe Perrin Sequence: " + visibilityBooleanToString(PropertyManager.getVisibleThePerrinSequence())
        + "\nRed Veil: " + visibilityBooleanToString(PropertyManager.getVisibleRedVeil())
        + "\nSteel Meridian: " + visibilityBooleanToString(PropertyManager.getVisibleSteelMeridian())
        + "\nCephalon Suda: " + visibilityBooleanToString(PropertyManager.getVisibleCephalonSuda())
        + "\nArbiters of Hexis: " +visibilityBooleanToString(PropertyManager.getVisibleArbitersOfHexis()));
  }

  /**
   * Converts a boolean expressing a visibility status to its corresponding String value.
   * @param visibility a boolean expressing visibility
   * @return a String expressing visibility; can only be "visible" or "invisible"
   */
  private static String visibilityBooleanToString(boolean visibility) {
    if(visibility) {
      return "visible";
    } else {
      return "invisible";
    }
  }

  /**
   * Calls for the username stored in this program to be updated with the passed argument.
   * @param userName a String containing the new username
   */
  private static void setUserName(String userName) {
    PropertyManager.setUserName(userName);
  }

  /**
   * Calls for the JSON Web Token stored in this program to be updated with the passed argument.
   * @param jwt a String containing the new JSON Web Token
   */
  private static void setJWT(String jwt) {
    PropertyManager.setJWT(jwt);
  }
}
