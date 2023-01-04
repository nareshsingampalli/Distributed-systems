package clustermanagament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ServiceRegistry implements Watcher {
	private static final String REGSTRY_ZNODE = "/service_reigstry";
	private final ZooKeeper zooKeeper;
	private List<String> allServiceAddress = null;
	private String currentZnode = null;
	public ServiceRegistry(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
		createServiceRegistryZnode();
		
	}
	
	//node for joining cluster
	public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
		this.currentZnode = zooKeeper.create(REGSTRY_ZNODE + "/c", metadata.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	}
	
	
	//creating service registry Znode
	public void createServiceRegistryZnode() {
		try {
			if(zooKeeper.exists(REGSTRY_ZNODE, false) == null) {
				zooKeeper.create(REGSTRY_ZNODE, new byte[] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void registerForUpdates() {
		try {
			updateAddress();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//get all addresses
	public synchronized List<String> getAllAddresses() throws KeeperException, InterruptedException{
		if(allServiceAddress == null) {
			updateAddress();
		}
		return allServiceAddress;
	}
	
	//unregister from cluster
	public void unregisterFromCluster() throws KeeperException, InterruptedException {
		if(currentZnode != null && zooKeeper.exists(REGSTRY_ZNODE, false)!= null) {
			zooKeeper.delete(REGSTRY_ZNODE, 0);
			
		}
	}
	
	
	//get updates to registry node
	private synchronized void updateAddress() throws KeeperException, InterruptedException {
		List<String> workerZnodes = zooKeeper.getChildren(REGSTRY_ZNODE,this);
		
		List<String> addresses = new ArrayList<String>(workerZnodes.size());
		
		for(String workerZnode:workerZnodes) {
			String workerZnodeFullPath = REGSTRY_ZNODE +"/"+workerZnode;
			Stat stat = zooKeeper.exists(workerZnodeFullPath,false);
			if(stat == null) {
				continue;
			}
			
			byte[] addressBytes = zooKeeper.getData(workerZnodeFullPath, false, stat);
			String address = new String(addressBytes);
			addresses.add(address);
		}
		this.allServiceAddress = Collections.unmodifiableList(addresses);
		System.out.println("The cluster addresses are: " + this.allServiceAddress);
		
	}
	
	//handle notifications from updateAddresses 
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		try {
			updateAddress();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
