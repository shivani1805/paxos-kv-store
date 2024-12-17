package paxos;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;



public class PaxosInstance extends UnicastRemoteObject implements Acceptor, Learner, Proposer,ServerInterface {
  private int nodeId;
  private long highestProposalId = -Integer.MAX_VALUE;
  private long acceptedProposalId = -Integer.MAX_VALUE;
  private String acceptedValue = null;
  private List<Acceptor> acceptors;
  private List<Learner> learners;
  private String LOG_FILE ;
  private Map<String, String> kvStore;

  private boolean isPrepopulated = false;


  private static final AtomicLong counter = new AtomicLong(0);

  private boolean failInPreparePhase = false;
  private boolean failInAcceptPhase = false;




  public PaxosInstance(int nodeId) throws RemoteException {
    super();
    this.nodeId = nodeId;
    this.kvStore = new ConcurrentHashMap<>();
    this.LOG_FILE = "SERVER_LOG_"+nodeId+".txt";
  }

  @Override
  public synchronized String receivePrepare(long proposalId) {
    if (failInPreparePhase) {
      Utils.log(LOG_FILE, "Acceptor " + nodeId + " failed during prepare phase.");
      return "REJECT";
    }
    if (proposalId > highestProposalId) {
      this.highestProposalId = proposalId;
      return "PROMISE";
    }
    return "REJECT";
  }


  @Override
  public synchronized String receiveAcceptRequest(long proposalId, String value) {
    if (failInAcceptPhase) {
      Utils.log(LOG_FILE,"Acceptor " + nodeId + " failed during accept phase.");
      return "REJECT";
    }
    if (proposalId >= highestProposalId) {
      this.highestProposalId = proposalId;
      this.acceptedProposalId = proposalId;
      this.acceptedValue = value;
      return "ACCEPT";
    }
    return "REJECT";
  }

  private void failRandomly() {

      Random rand = new Random();
      try {
        while (!Thread.interrupted()) {
          if(!kvStore.isEmpty() || isPrepopulated) {
          int sleepTime = rand.nextInt(5000) + 1000;
          Thread.sleep(sleepTime);

          if (rand.nextInt(10) < 2) {
            if (rand.nextBoolean()) {
              failInPreparePhase = true;
             Utils.log(LOG_FILE,"Acceptor " + nodeId + " set to fail during prepare phase.");
            } else {
              failInAcceptPhase = true;
              Utils.log(LOG_FILE,"Acceptor " + nodeId + " set to fail during accept phase.");
            }

            int failureDuration = rand.nextInt(4000) + 6000;
            Thread.sleep(failureDuration);


            failInPreparePhase = false;
            failInAcceptPhase = false;
            Utils.log(LOG_FILE,"Acceptor " + nodeId + " restarted.");
          }
        }
      }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

  }


  public void startFailingThread() {
    new Thread(this::failRandomly).start();
  }


  @Override
  public boolean propose(String value, String key, String operation) throws RemoteException {
    long proposalId = generateProposalId();

    int promises = 0;


    for (Acceptor acceptor : acceptors) {

        String response = acceptor.receivePrepare(proposalId);
        if ("PROMISE".equals(response)) {
          promises++;
        }

    }
    Utils.log(LOG_FILE,"Number of promises received: "+promises);

    if (promises > acceptors.size() / 2) {

      int accepts = 0;
      for (Acceptor acceptor : acceptors) {

          String response = acceptor.receiveAcceptRequest(proposalId, value);
          if ("ACCEPT".equals(response)) {
            accepts++;
          }

      }
      Utils.log(LOG_FILE,"Count of accept responses: " + accepts);
      if(accepts> acceptors.size()/2) {
        for (Learner learner : this.learners)
        {
          learner.learn(value,key,operation);
        }
        Utils.log(LOG_FILE,"Accept message accepted by the majority");
        return true;
      } else {
       Utils.log(LOG_FILE,"PAXOS FAIL: Accept message rejected by the majority");
        return false;
      }
    } else {
      Utils.log(LOG_FILE,"PAXOS FAIL : Quorum not reached, majority acceptors may be down. Number of promises received - "+promises);
    }
    return false;
  }


  @Override
  public void learn(String value, String key, String operation) {

      if (operation.equals("put")) {
        this.kvStore.put(key, value);
        Utils.log(LOG_FILE,"Learner "+nodeId+" learned value: " + value);
      } else if(operation.equals("delete")){
        this.kvStore.remove(key);
        Utils.log(LOG_FILE,"Learner executed.");
    }

  }



  public void setAcceptors(List<Acceptor> acceptors) {
    this.acceptors = acceptors;
  }

  public void setLearners(List<Learner> learners) {
    this.learners = learners;
  }


  @Override
  public synchronized String put(String key, String value, String clientName) throws RemoteException {

    if (!isPrepopulated && kvStore.isEmpty()) {
      isPrepopulated = true;
    }
    if (propose(value,key,"put")) {
      kvStore.put(key, value);
      Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+" Success PUT : Key=" + key + ", Value=" + value + " stored.");
      return "SUCCESS: Key=" + key + ", Value=" + value + " stored.";
    } else {
      Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+"FAIL: Paxos execution failed.");
      return "FAIL: Paxos execution failed.";
    }
  }

  @Override
  public synchronized String get(String key,String clientName) throws RemoteException {
    if (kvStore.containsKey(key)) {
      String result = kvStore.get(key);
      Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+" Success GET : Success: Key=" + key + ", Value=" + result);
      return result;
    } else {
      Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+" Error GET : Key="+key+" not found");
      return "Error: Key not found";
    }  }

  @Override
  public synchronized String delete(String key,String clientName) throws RemoteException {
    if (kvStore.containsKey(key)) {
      if (propose(null,key,"delete")) {
        kvStore.remove(key);
        Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+" Success DELETE : Key=" + key + " deleted.");
        return "SUCCESS: Key=" + key + " deleted.";
      } else {
        Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+"FAIL: Paxos execution failed.");
        return "FAIL: Paxos execution failed.";
      }
    } else {
      Utils.log(LOG_FILE,"Client Name - "+clientName+" >"+" Error DELETE: Key="+key+" not found");
      return "Error: Key not found.";
    }
  }



    public long generateProposalId() {
      long currentTime = System.currentTimeMillis();
      long counterValue = counter.getAndIncrement();
      return (currentTime << 20) | (counterValue << 16) | (this.nodeId & 0xFFFF);
    }


}
