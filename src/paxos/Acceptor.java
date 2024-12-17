package paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Acceptor interface represents the role of an Acceptor in the Paxos consensus algorithm.
 * Acceptors are responsible for handling proposal requests and maintaining consistency
 * in the distributed system by following the Paxos protocol rules.
 *
 * This interface extends the Remote interface to enable communication between Paxos nodes
 * over Remote Method Invocation (RMI).
 */
public interface Acceptor extends Remote {

  /**
   * Handles the "Prepare" phase of the Paxos algorithm.
   * When a proposer sends a "prepare" request with a proposal ID, the Acceptor responds
   * based on whether the proposal ID is greater than any previously seen proposal ID.
   *
   * - If the proposal ID is higher, the Acceptor promises not to accept proposals
   *   with lower IDs and returns a "PROMISE".
   * - If the proposal ID is lower or equal, the Acceptor rejects the request and returns "REJECT".
   *
   * @param proposalId The unique identifier for the proposal being sent by the Proposer.
   * @return A string response:
   *         - "PROMISE" if the Acceptor promises to consider this proposal in subsequent phases.
   *         - "REJECT" if the proposal ID is not acceptable.
   * @throws RemoteException If there is an issue with remote communication.
   */
  String receivePrepare(long proposalId) throws RemoteException;

  /**
   * Handles the "Accept" phase of the Paxos algorithm.
   * When a proposer sends an "accept" request with a proposal ID and a value, the Acceptor
   * responds based on whether the proposal ID matches or exceeds the highest promised ID.
   *
   * - If the proposal ID is valid, the Acceptor accepts the proposal, updates its state,
   *   and returns "ACCEPT".
   * - If the proposal ID is invalid, the Acceptor rejects the request and returns "REJECT".
   *
   * @param proposalId The unique identifier for the proposal being sent by the Proposer.
   * @param value      The value associated with the proposal.
   * @return A string response:
   *         - "ACCEPT" if the Acceptor accepts the proposal.
   *         - "REJECT" if the proposal ID is not valid or acceptable.
   * @throws RemoteException If there is an issue with remote communication.
   */
  String receiveAcceptRequest(long proposalId, String value) throws RemoteException;

}
