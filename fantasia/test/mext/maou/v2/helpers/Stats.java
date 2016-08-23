/*
 * HiddenProject: Ente Isla
 * Copyright © 2013 Hidden Project Team
 */
package mext.maou.v2.helpers;


/**
 * ON DEVELOPMENT
 * @author Mystical
 */
public enum Stats {
    instance;
    
    static int intLevelCap = 100;
    static double PCstBase = 15;
    static double PCstGoal = 762;
    static double statsExponent = 1;
    
    public int getInnateStats(int level) {
        level = level < 1 ? 1 : level > intLevelCap ? intLevelCap : level;
        double x = ((double) (level - 1) / (intLevelCap - 1));
        return (int) Math.round(PCstBase + (Math.pow(x, statsExponent) * (PCstGoal - PCstBase)));
    }
}
