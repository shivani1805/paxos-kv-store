package paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Proposer interface defines the behavior for a Proposer
 * in the Paxos consensus algorithm. The Proposer is responsible
 * for initiating proposals and attempting to achieve consensus
 * with the majority of Acceptors.
 *
 * This interface extends the Remote interface, enabling it to
 * be used in an RMI (Remote Method Invocation) environment for
 * distributed systems.
 */
public interface Proposer extends Remote {

  /**
   * Sends a proposal to the Paxos Acceptors to attempt to achieve consensus.
   * The proposal consists of a value, a key (to identify the operation),
   * and an operation type (e.g., "put" or "delete").
   *
   * @param value The value associated with the proposal.
   * @param key The key associated with the proposal (used for key-value storage operations).
   * @param operation The type of operation (e.g., "put" to insert or update, "delete" to remove).
   * @return true if the proposal was successfully accepted by the majority of Acceptors, false otherwise.
   * @throws RemoteException If a remote communication error occurs during the process.
   */
  boolean propose(String value, String key, String operation) throws RemoteException;
}
