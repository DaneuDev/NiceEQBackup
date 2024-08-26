package pl.daneu.niceeqbackup.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerializationUtil {

    public static ItemStack getItemStack(String s) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decode(s));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();

            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getItemStackString(ItemStack item){
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return new String(Base64Coder.encode(outputStream.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getLocationString(Location location){
        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getYaw() + ";" +
                location.getPitch() + ";";
    }

    public static Location getLocation(String s){
        String[] options = s.split(";");

        World world = Bukkit.getWorld(options[0]);
        double x = Double.parseDouble(options[1]);
        double y = Double.parseDouble(options[2]);
        double z = Double.parseDouble(options[3]);
        float yaw = Float.parseFloat(options[4]);
        float pitch = Float.parseFloat(options[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

}
