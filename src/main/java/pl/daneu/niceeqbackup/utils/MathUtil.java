package pl.daneu.niceeqbackup.utils;

public class MathUtil {

    public static int getLevelFromTotalExperience(int experience){
        if(experience < 353)
            return (int) (Math.sqrt(experience + 9) - 3);
        else if(experience < 1508)
            return (int) ((double) 81/10 + Math.sqrt((double) 2/5 * (experience - (double) 7839 /40)));
        else
            return (int) ((double) 325/18 + Math.sqrt((double) 2/9 * (experience - (double) 54215 /72)));
    }

    public static float getTotalExperienceForLevelFromLevel(int experience){
        int level = getLevelFromTotalExperience(experience) + 1;

        if(level < 17)
            return level*level + 6*level;
        else if(experience < 32) {
            return 2.5f*level*level - 40.5f*level + 360;
        } else
            return 4.5f*level*level - 162.5f*level + 2220;
    }

    public static int calculateItemsCount(int total, int page){
        if(total == 0)
            return 0;
        else if(total % 45 == 0)
            return 45;
        else if(total/((page + 1)*45) > 0)
            return 45;

        return total - page*5*9;
    }

    public static int calculateInventorySize(int total, int page){
        int itemsCount = calculateItemsCount(total, page);

        return 1 + (itemsCount%9 == 0 ? itemsCount/9 : itemsCount/9 + 1);
    }

}
