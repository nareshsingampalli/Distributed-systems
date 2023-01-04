

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.zookeeper.KeeperException;

import clustermanagament.OnElectionCallback;
import clustermanagament.ServiceRegistry;

public class OnElectionAction implements OnElectionCallback {
	private final ServiceRegistry serviceRegistry;
	private final int port;
	
	public OnElectionAction(ServiceRegistry serviceRegistry,int port) {
		this.port = port;
		this.serviceRegistry = serviceRegistry;
	}
	public void onElectionToBeLeader() throws KeeperException, InterruptedException {
		// TODO Auto-generated method stub
		serviceRegistry.unregisterFromCluster();
		serviceRegistry.registerForUpdates();
	}

	public void onWorker() {
		// TODO Auto-generated method stub
		try {
			String currentServerAddress = String.format("http://%s:%d", InetAddress.getLocalHost(),port);
			serviceRegistry.registerToCluster(currentServerAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
