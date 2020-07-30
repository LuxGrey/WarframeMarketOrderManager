public class Main {

  /**
   * A class that extends Thread that is to be called when program is exiting
   * main purpose is to persist data to properties file
   */
  private static class SaveConfiguration extends Thread{
    public void run() {
      PropertyManager.storeProperties();
    }
  }

  public static void main(String[] args) {
    init();
    Runtime.getRuntime().addShutdownHook(new SaveConfiguration());
    MenuController.menuLoop();
  }

  /**
   * Initialize variables where necessary before the program accepts user inputs.
   */
  public static void init() {
    PropertyManager.init();
    APIRequestManager.init();
  }
}
