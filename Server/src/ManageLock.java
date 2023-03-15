import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ManageLock {
    
    private Map<String, Map<String, Object>> statusTable = new HashMap<>();
/* 
    private static long generateID()
		{
			long randomLong = ThreadLocalRandom.current().nextLong(100_000L, 999_999L);
			return randomLong;
		}
*/      
    public synchronized boolean setLock(String filePath, String operation, long currThreadId) throws InterruptedException {
        
        //long commandID = 0;
        if (!statusTable.containsKey(filePath)) {
            statusTable.put(filePath, new HashMap<>());
            statusTable.get(filePath).put("get_lock", false);
            statusTable.get(filePath).put("put_lock", false);
            statusTable.get(filePath).put("delete_lock", false);
            statusTable.get(filePath).put("command_id", null);
        }
        if (!(Boolean) statusTable.get(filePath).get("put_lock")
                    && !(Boolean) statusTable.get(filePath).get("get_lock")
                    && !(Boolean) statusTable.get(filePath).get("delete_lock")) {
                
                statusTable.get(filePath).put(operation, true);
                statusTable.get(filePath).put("command_id", currThreadId);
                showStatus();
                return true;

            }
        return false;
    }

    public synchronized Boolean releaseLock(long currThreadId) {
        /*
        if (!statusTable.containsKey(filePath)) {
            throw new IllegalArgumentException("File is not locked");
        }

        if (!statusTable.get(filePath).get("command_id").equals(commandID)) {
            throw new IllegalArgumentException("Thread does not hold the lock for the file");
        }
        */
        for (String filePath : statusTable.keySet()) {
            Map<String, Object> fileStatus = statusTable.get(filePath);
            if(fileStatus.get("command_id") != null && fileStatus.get("command_id").equals(currThreadId))
            {
                if((Boolean) fileStatus.get("get_lock"))
                {
                    statusTable.get(filePath).put("get_lock", false);
                    statusTable.get(filePath).put("command_id", null);
                    showStatus();
                    return true;
                }
                else if((Boolean) fileStatus.get("put_lock"))
                {
                    statusTable.get(filePath).put("put_lock", false);
                    statusTable.get(filePath).put("command_id", null);
                    showStatus();
                    return true;
                }
                else if((Boolean) fileStatus.get("delete_lock"))
                {
                    statusTable.get(filePath).put("delete_lock", false);
                    statusTable.get(filePath).put("command_id", null);
                    showStatus();
                    return true;
                }
            }
            
        }
        showStatus();
        return false;   
            
    }

    public Boolean getStatus(String filePath, String operation)
    {
        return (Boolean) statusTable.get(filePath).get(operation);
    }

    public void showStatus()
    {
        System.out.println("\nFile Path | Get Lock | Put Lock | Delete Lock | Command ID");

        System.out.println("----------------------------------------------");
        for (String filePath : statusTable.keySet()) {
            Map<String, Object> fileStatus = statusTable.get(filePath);
            Boolean readLock = (Boolean) fileStatus.get("get_lock");
            Boolean writeLock = (Boolean) fileStatus.get("put_lock");
            Boolean deleteLock = (Boolean) fileStatus.get("delete_lock");
            Object commandID = fileStatus.get("command_id");
            System.out.printf("%-10s| %-9s| %-10s| %-10s| %-10s%n", filePath, readLock, writeLock,deleteLock, commandID);
        }
    }
}
