import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * This class is responsible for persisting certain values used by the program to a properties file.
 * It also acts as a container for those values when they are loaded into the program.
 */
public class PropertyManager {
  private static final String PROPERTY_FILE_LOCATION = "user_info.properties";
  private static Properties properties;

  public static void init() {
    properties = new Properties();
    loadProperties();
  }

  /**
   * Loads properties from property file and stores them in properties member field.
   */
  private static void loadProperties(){
    try {
      FileInputStream inputStream = new FileInputStream(PROPERTY_FILE_LOCATION);
      properties.load(inputStream);
      inputStream.close();
    }
    catch (FileNotFoundException e) {
      //initialize with standard values
      setVisibleNewLoka(true);
      setVisibleThePerrinSequence(true);
      setVisibleRedVeil(true);
      setVisibleSteelMeridian(true);
      setVisibleCephalonSuda(true);
      setVisibleArbitersOfHexis(true);
      setUserName("");
      setJWT("");

      //create new properties file
      storeProperties();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Persists data in properties member field to property file.
   */
  public static void storeProperties() {
    try {
      FileOutputStream fos = new FileOutputStream(PROPERTY_FILE_LOCATION);
      properties.store(fos, null);
      fos.close();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }
  }

  public static String getUserName() {
    return properties.getProperty("userName", null);
  }

  public static void setUserName(String newUserName) {
    properties.setProperty("userName", newUserName);
  }

  public static String getJWT() {
    return properties.getProperty("jwt", null);
  }

  public static void setJWT(String newJWT) {
    properties.setProperty("jwt", newJWT);
  }

  //long and painful-to-look-at sequence of almost identical getters and setters ahead
  //probably could have generalized these, but chose not to

  public static boolean getVisibleNewLoka() {
    String strVisible = properties.getProperty("visibleNewLoka", null);
    return Boolean.parseBoolean(strVisible);
  }

  public static void setVisibleNewLoka(boolean visible) {
    String strVisible = String.valueOf(visible);
    properties.setProperty("visibleNewLoka", strVisible);
  }

  public static boolean getVisibleThePerrinSequence() {
    String strVisible = properties.getProperty("visibleThePerrinSequence", null);
    return Boolean.parseBoolean(strVisible);
  }

  public static void setVisibleThePerrinSequence(boolean visible) {
    String strVisible = String.valueOf(visible);
    properties.setProperty("visibleThePerrinSequence", strVisible);
  }

  public static boolean getVisibleRedVeil() {
    String strVisible = properties.getProperty("visibleRedVeil", null);
    return Boolean.parseBoolean(strVisible);
  }

  public static void setVisibleRedVeil(boolean visible) {
    String strVisible = String.valueOf(visible);
    properties.setProperty("visibleRedVeil", strVisible);
  }

  public static boolean getVisibleSteelMeridian() {
    String strVisible = properties.getProperty("visibleSteelMeridian", null);
    return Boolean.parseBoolean(strVisible);
  }

  public static void setVisibleSteelMeridian(boolean visible) {
    String strVisible = String.valueOf(visible);
    properties.setProperty("visibleSteelMeridian", strVisible);
  }

  public static boolean getVisibleCephalonSuda() {
    String strVisible = properties.getProperty("visibleCephalonSuda", null);
    return Boolean.parseBoolean(strVisible);
  }

  public static void setVisibleCephalonSuda(boolean visible) {
    String strVisible = String.valueOf(visible);
    properties.setProperty("visibleCephalonSuda", strVisible);
  }

  public static boolean getVisibleArbitersOfHexis() {
    String strVisible = properties.getProperty("visibleArbitersOfHexis", null);
    return Boolean.parseBoolean(strVisible);
  }

  public static void setVisibleArbitersOfHexis(boolean visible) {
    String strVisible = String.valueOf(visible);
    properties.setProperty("visibleArbitersOfHexis", strVisible);
  }
}
