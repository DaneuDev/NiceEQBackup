package pl.daneu.eqbackup.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final Material material;
    private int amount;
    private String name;
    private List<String> lore = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private Set<ItemFlag> itemFlags = new HashSet<>();
    private int durability = -1;
    private boolean isUnbreakable;

    public ItemBuilder(ItemStack item){
        material = item.getType();
        amount = item.getAmount();

        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            return;

        if(meta.hasDisplayName())
            name = meta.getDisplayName();
        if(meta.hasLore())
            lore = meta.getLore();
        if(meta.hasEnchants())
            enchantments = item.getEnchantments();
        if(!meta.getItemFlags().isEmpty())
            itemFlags = meta.getItemFlags();

        Damageable damageable = (Damageable) meta;
        if(damageable.hasDamage())
            durability = damageable.getDamage();
    }

    public ItemBuilder(Material material){
        this(material, 1);
    }

    public ItemBuilder(Material material, Integer amount){
        this(material, amount, null);
    }

    public ItemBuilder(Material material, Integer amount, String name){
        this.material = material;
        this.amount = amount;
        this.name = name;
        isUnbreakable = false;
    }

    public Material getMaterial(){ return material; }
    public int getAmount(){ return amount; }
    public String getName(){ return name; }
    public List<String> getLore(){ return lore; }
    public Set<ItemFlag> getItemFlags(){ return itemFlags; }
    public int getDurability(){ return durability; }
    public boolean isUnbreakable(){ return isUnbreakable; }

    public ItemBuilder setName(String name){
        this.name = name;

        return this;
    }

    public ItemBuilder setAmount(Integer amount){
        this.amount = amount;

        return this;
    }

    public ItemBuilder setLore(List<String> lore){
        this.lore = lore;

        return this;
    }

    public ItemBuilder setLore(String... lore){
        this.lore = Arrays.stream(lore).collect(Collectors.toList());

        return this;
    }

    public ItemBuilder setLoreLine(String line, int num){
        lore.set(num, line);

        return this;
    }

    public ItemBuilder setEnchantment(Enchantment enchant, int lvl){
        enchantments.put(enchant, lvl);

        return this;
    }

    public ItemBuilder setDurability(Integer durability){
        this.durability = durability;

        return this;
    }

    public ItemBuilder setUnbreakable(boolean isUnbreakable){
        this.isUnbreakable = isUnbreakable;

        return this;
    }

    public ItemBuilder addFlag(ItemFlag flag){
        itemFlags.add(flag);

        return this;
    }

    public ItemStack create(){
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if(meta != null){
            if(name != null)
                meta.setDisplayName(name);
            if(!lore.isEmpty())
                meta.setLore(lore);
            if(!enchantments.isEmpty())
                enchantments.forEach((ench, lvl) -> meta.addEnchant(ench, lvl, true));
            if(!itemFlags.isEmpty())
                itemFlags.forEach(meta::addItemFlags);

            meta.setUnbreakable(isUnbreakable);
            if(durability != -1)
                ((Damageable) meta).setDamage(durability);

            item.setItemMeta(meta);
        }
        return item;
    }
}
