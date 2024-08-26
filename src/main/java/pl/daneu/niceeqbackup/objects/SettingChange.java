package pl.daneu.niceeqbackup.objects;

import org.bukkit.Material;

public record SettingChange(User user, Backup backup, Type type, int page, boolean fromBackupList) {

    public enum Type{
        ITEMS("Items", Material.STONE_PICKAXE),
        EXPERIENCE("Experience", Material.EXPERIENCE_BOTTLE),
        HEALTH("Health", Material.RED_DYE),
        FOOD("Food level", Material.COOKED_BEEF),
        LOCATION("Location", Material.COMPASS);

        final String label;
        final Material material;

        Type(String label, Material material){
            this.label = label;
            this.material = material;
        }

        public String getLabel() {
            return label;
        }

        public Material getMaterial() {
            return material;
        }
    }

}
