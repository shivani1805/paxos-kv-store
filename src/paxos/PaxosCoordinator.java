package paxos;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;




public class PaxosCoordinator {

  private static final String LOG_FILE = "PAXOS_COORDINATOR_LOG.txt";
  private static final Utils utils  = new Utils(LOG_FILE);


  public static void main(String[] args) {
    if (args.length < 2) {
      utils.log("Correct Format: java paxos/PaxosCoordinator <port-number> <server-name>");
      System.exit(1);
    }
    String serverName = args[1];
    int basePort;
    try {
      int numServers = 5;
      basePort = Integer.parseInt(args[0]);;
      PaxosInstance[] servers = new PaxosInstance[numServers];

      for (int serverId = 0; serverId < numServers; serverId++) {
        int port = basePort + serverId;

        servers[serverId] = new PaxosInstance(serverId);


        Registry registry = LocateRegistry.createRegistry(port);

        registry.rebind(serverName, servers[serverId]);

        utils.log("Server " + serverId + " bound to registry at port " + port);

        servers[serverId].startFailingThread();
      }


      for (int serverId = 0; serverId < numServers; serverId++) {
        List<Acceptor> acceptors = new ArrayList<>();
        List<Learner> learners = new ArrayList<>();

        for (int i = 0; i < numServers; i++) {
          if (i != serverId) {
            acceptors.add(servers[i]);
            learners.add(servers[i]);
          }
        }

        servers[serverId].setAcceptors(acceptors);
        servers[serverId].setLearners(learners);

        utils.log("Server " + serverId + " configured with acceptors and learners.");
      }

      utils.log("All servers are ready and running.");

    } catch (RemoteException e) {
      utils.log("Error setting up Paxos nodes: " + e.getMessage());

      System.exit(1);
    }
    catch (NumberFormatException e) {
      utils.log("Invalid port number: " + args[0]);
      System.exit(1);

    }
  }
}
