import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ManageLock {
    
    private Map<String, Map<String, Object>> statusTable = new HashMap<>();

    private static long generateID()
		{
			long randomLong = ThreadLocalRandom.current().nextLong(100_000L, 999_999L);
			return randomLong;
		}
        
    public synchronized long setLock(String filePath, String operation) throws InterruptedException {
        
        long commandID = 0;
        if (!statusTable.containsKey(filePath)) {
            statusTable.put(filePath, new HashMap<>());
            statusTable.get(filePath).put("get_lock", false);
            statusTable.get(filePath).put("put_lock", false);
            statusTable.get(filePath).put("delete_lock", false);
            statusTable.get(filePath).put("command_id", null);
        }
        int iteration = 0;
        while (iteration<5) {
            if (!(Boolean) statusTable.get(filePath).get("put_lock")
                    && !(Boolean) statusTable.get(filePath).get("get_lock")
                    && !(Boolean) statusTable.get(filePath).get("delete_lock")) {
                
                commandID = generateID();
                statusTable.get(filePath).put(operation, true);
                statusTable.get(filePath).put("command_id", commandID);
                showStatus();
                return commandID;

            } else {
                Thread.sleep(1000);
            }
            iteration++;
        }
        return commandID;
    }

    public synchronized void releaseLock(String filePath, long commandID, String operation) {
        if (!statusTable.containsKey(filePath)) {
            throw new IllegalArgumentException("File is not locked");
        }

        if (!statusTable.get(filePath).get("command_id").equals(commandID)) {
            throw new IllegalArgumentException("Thread does not hold the lock for the file");
        }
            statusTable.get(filePath).put(operation, false);
            statusTable.get(filePath).put("command_id", null);
            showStatus();
    }

    public Boolean getStatus(String filePath, String operation)
    {
        return (Boolean) statusTable.get(filePath).get(operation);
    }

    public void showStatus()
    {
        System.out.println("\nFile Path | Read Lock | Write Lock | Command ID");

        System.out.println("----------------------------------------------");
        for (String filePath : statusTable.keySet()) {
            Map<String, Object> fileStatus = statusTable.get(filePath);
            Boolean readLock = (Boolean) fileStatus.get("read_lock");
            Boolean writeLock = (Boolean) fileStatus.get("write_lock");
            Object commandID = fileStatus.get("command_id");
            System.out.printf("%-10s| %-9s| %-10s| %-10s%n", filePath, readLock, writeLock, commandID);
        }
    }
}
