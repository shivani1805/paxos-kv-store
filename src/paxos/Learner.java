package paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Learner interface represents the role of a Learner in the Paxos consensus algorithm.
 * Learners receive the agreed-upon value from the acceptors and act on it (e.g., updating
 * a key-value store).
 *
 * This interface extends the Remote interface to support Remote Method Invocation (RMI),
 * allowing distributed communication between Paxos instances.
 */
public interface Learner extends Remote {

  /**
   * The learn method is invoked to notify the Learner of the value agreed upon by
   * the majority of the acceptors in the Paxos algorithm.
   *
   * This method allows the Learner to perform necessary operations with the learned value,
   * such as storing or removing it in a key-value store.
   *
   * @param value     The value that has been agreed upon by the majority.
   *                  If the operation is "delete", this may be null.
   * @param key       The key associated with the value in the key-value store.
   * @param operation The operation to perform, e.g., "put" for storing or "delete" for removing.
   *
   * @throws RemoteException If there is an issue with remote communication.
   */
  void learn(String value, String key, String operation) throws RemoteException;

}
