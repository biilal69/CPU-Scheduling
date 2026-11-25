package scheduler.acs;

import java.util.List;
import model.Process;


public class AgingPolicy {   
            
    public static final int DEFAULT_INTERVAL= 4;

    private final int interval;
    public AgingPolicy(int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Aging interval must be at least 1, but got " + interval);
        }
        this.interval =interval;
    }

    public void apply(List<Process> tier2, int time) {
        for (Process p : tier2) {       
            int waitedTime = time - p.getEnteredTier2At();
            int levelsGained = waitedTime / interval;  
                     
            p.setPriority(p.getInitialPriority() - levelsGained);      
        } 
    }
}
