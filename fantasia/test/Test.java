import java.util.Calendar;
import mext.maou.v2.helpers.Stats;

/*
 * HiddenProject: Ente Isla
 * Copyright © 2013 Hidden Project Team
 */

/**
 * FOR TESTING PURPOSES ONLY
 * @author Mystical
 */
public class Test {
    //MEMORY TEST
    
    public Test() {
        //WUT
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Beginning Test...");
        long startTime = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        long remainTime = cal.getTimeInMillis();
        long diffTime = (remainTime - System.currentTimeMillis());

        if(diffTime > 0) {
            System.out.println(diffTime);
            //World.instance.sendResponse(new String[] {"warning", "You are mute! Chat privileges have been temporarily revoked."}, user);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Test End, Total Time: " + (endTime - startTime) + "ms");
    }
}
