package pl.daneu.niceeqbackup.handlers;

import pl.daneu.niceeqbackup.objects.User;

import java.util.*;

public class UserHandler {

    private static final Map<UUID, User> users = new HashMap<>();

    public static User getUser(UUID uuid){
        return users.get(uuid); }

    public static User getUser(String playerName){
        return users.values().stream()
                .filter(u -> u.getPlayerName().equalsIgnoreCase(playerName))
                .findAny().orElse(null);
    }

    public Set<User> getUsers(){
        return new HashSet<>(users.values());
    }

    public void addUser(User user){
        users.put(user.getUUID(), user);
    }

    public void removeUser(UUID uuid){
        users.remove(uuid);
    }

}
