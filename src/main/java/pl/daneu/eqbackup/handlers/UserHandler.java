package pl.daneu.eqbackup.handlers;

import pl.daneu.eqbackup.objects.User;

import java.util.*;

public class UserHandler {

    private static final Map<UUID, User> users = new HashMap<>();

    public static User getUser(UUID uuid){ return users.get(uuid); }

    public static User getUser(String playerName){
        return users.values().stream()
                .filter(u -> u.getPlayerName().equalsIgnoreCase(playerName))
                .findAny().orElse(null);
    }

    public Map<UUID, User> getUsers(){ return users; }

    public void addUser(User user){ users.put(user.getUUID(), user); }

    public void removeUser(UUID uuid){ users.remove(uuid); }

}
