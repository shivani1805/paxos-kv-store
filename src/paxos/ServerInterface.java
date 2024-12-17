package paxos;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The ServerInterface defines the remote methods that can be invoked
 * by clients to interact with the server's data store.
 * It extends the Remote interface to enable RMI.
 */
public interface ServerInterface extends Remote {

  /**
   * Stores a key-value pair in the server's data store.
   *
   * @param key the key to store
   * @param value the value associated with the key
   * @return a success message confirming the storage operation
   * @throws RemoteException if an RMI error occurs during the operation
   */
  String put(String key, String value, String clientName) throws RemoteException;

  /**
   * Retrieves the value associated with a given key from the store.
   *
   * @param key the key whose value is to be retrieved
   * @return the value associated with the key, or an error message if the key is not found
   * @throws RemoteException if an RMI error occurs during the operation
   */
  String get(String key, String clientName) throws RemoteException;

  /**
   * Deletes a key-value pair from the store using the specified key.
   *
   * @param key the key to be deleted
   * @return a success message if the key was found and deleted, or an error message if not found
   * @throws RemoteException if an RMI error occurs during the operation
   */
  String delete(String key, String clientName) throws RemoteException;

}
