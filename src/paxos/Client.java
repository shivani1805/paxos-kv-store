package paxos;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * The server.Client class implements a remote client that connects to a
 * server using RMI. It allows users to perform operations like
 * storing, retrieving, and deleting key-value pairs through a
 * console interface or by executing commands from a script file.
 */
public class Client {
  private static final String LOG_FILE = "CLIENT_LOG.txt";
  private static final String OPERATIONS_SCRIPT = "operations-script.txt";
  private static final String DATA_POPULATE_SCRIPT = "data-population-script.txt";

  private final Utils utils  = new Utils(LOG_FILE);

  private ServerInterface server;

  public static void main(String[] args) {
    Client client = new Client();
    client.run(args);

  }

  private void run(String[] args) {

    if (args.length != 4) {
      utils.log("Incorrect Arguments");
      throw new IllegalArgumentException("Please enter the following arguments - <IP/hostname> <Port-number> <server-name> <client-name>");
    }

    String hostname = args[0];
    int port;
    try {
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new NumberFormatException("Please enter a valid integer port");
    }
    String serverName = args[2];
    String clientName = args[3];

    utils.log("Starting the Client");
    utils.log("Attempting connection to " + hostname + " on port " + port);

    try {
      Registry registry = LocateRegistry.getRegistry(hostname, port);
      server = (ServerInterface) registry.lookup(serverName);
      utils.log("Connection established to the server: " + serverName);


      Scanner scanner = new Scanner(System.in);
      utils.log("Do you want to prepopulate data? Type 'yes' to proceed or 'no' to skip.");
      String prepopulateChoice = scanner.nextLine().trim().toLowerCase();

      if ("yes".equals(prepopulateChoice)) {
        utils.log("Starting Data Population...");
        processScript(DATA_POPULATE_SCRIPT, clientName);
        utils.log("Data Population Completed.");
      } else {
        utils.log("Skipping Data Population.");
      }

      while (true) {
        utils.log("Enter 'run' to execute commands from the script, 'console' to enter commands manually, or 'close' to exit.");
        String userInput = scanner.nextLine().trim().toLowerCase();

        if ("console".equals(userInput)) {
          utils.log("Entering manual command mode. Type 'exit' to return to the main menu.");
          while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine();
            if ("exit".equalsIgnoreCase(command)) break;
            handleRequest(command, clientName);
          }
        } else if ("close".equals(userInput)) {
          utils.log("Exiting client.");
          break;
        } else if ("run".equals(userInput)) {
          processScript(OPERATIONS_SCRIPT, clientName);
          utils.log("All Operations Completed.");
        } else {
          utils.log("Invalid input. Please enter 'console', 'close', or 'run'.");
        }
      }
    } catch (Exception e) {
      utils.log("Error connecting to server: " + e.getMessage());
    }
  }

  /**
   * Processes a given script by reading commands from the specified script file
   * for performing operations or pre-populating the data.
   *
   * @param scriptPath the path to the script file
   */
  private void processScript(String scriptPath, String clientName) {
    try (BufferedReader scriptReader = new BufferedReader(new FileReader(scriptPath))) {
      String line;
      while ((line = scriptReader.readLine()) != null) {
        handleRequest(line, clientName);
      }
    } catch (FileNotFoundException e) {
      utils.log("Script file not found: " + scriptPath);
    } catch (IOException e) {
      utils.log("Error reading script file: " + e.getMessage());
    }
  }

  /**
   * Handles a request based on the provided command string.
   *
   * @param command the command string to process
   */
  private void handleRequest(String command, String clientName) {
    String[] parts = command.split(" ", 3);
    if (parts.length < 2) {
      utils.log("Invalid command format. Use: PUT key value, GET key, or DELETE key.");
      return;
    }

    String operation = parts[0].toUpperCase();
    String key = parts[1];
    String response = null;

    try {
      switch (operation) {
        case "PUT":
          if (parts.length != 3) {
            utils.log("PUT command requires a key and a value.");
            return;
          }
          String value = parts[2];
          response = server.put(key, value, clientName);
          break;
        case "GET":
          response = server.get(key, clientName);
          break;
        case "DELETE":
          response = server.delete(key, clientName);
          break;
        default:
          utils.log("Invalid operation. Supported operations: PUT, GET, DELETE.");
          return;
      }

      utils.log("Response from server: " + response);
    } catch (RemoteException e) {
      utils.log("Remote exception: " + e.getMessage());
    }
  }


}
