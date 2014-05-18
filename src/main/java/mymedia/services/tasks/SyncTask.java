package mymedia.services.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mymedia.services.MediaManager;

public class SyncTask implements Runnable {
	
	private final static Logger log = Logger.getLogger(SyncTask.class.getName());
	
	@Override
	public void run() {
		try {
			MediaManager.syncTorrents();
		} catch (Exception e) {
    		log.log(Level.WARNING, "Exception occurred running MediaManager.syncTorrents()", e);
			e.printStackTrace();
		}
		
		
		
		
		
		
		/*ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Task());

        try {
            System.out.println("Started..");
            System.out.println(future.get(30, TimeUnit.SECONDS));
            System.out.println("Finished!");
        } catch (TimeoutException e) {
            System.out.println("Terminated!");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException!");
            e.printStackTrace();
		} catch (ExecutionException e) {
            System.out.println("ExecutionException!");
            e.printStackTrace();
		} finally {
            System.out.println("Cancel task!");
            future.cancel(true);
        	executor.shutdownNow();
			
		}
		
		*/
		
	}

	/*public class Task implements Callable<String> {
	    @Override
	    public String call() throws Exception {
	    	try {
	            System.out.println("Started task!");
				MediaManager.syncTorrents();
		        //Thread.sleep(4000); // Just to demo a long running task of 4 seconds.
	            System.out.println("Finished task!");
			} catch (InterruptedException e) {
				System.out.println("[ERROR] caught InterruptedException #################################################");
				e.printStackTrace();
				//Thread.currentThread().interrupt();
			}
	        return "Done";
	    }
	    
	    public void interrupt() {
            System.out.println("interrupt????????????????????????????????????????????");
	    }
	    
	}*/
}