package pl.daneu.eqbackup.handlers;

import pl.daneu.eqbackup.objects.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserHandler {

    private static final Set<User> users = new HashSet<>();

    public static User getUser(UUID uuid){
        return users.stream()
                .filter(u -> u.getUUID().equals(uuid))
                .findAny().orElse(null);
    }

    public static User getUser(String playerName){
        return users.stream()
                .filter(u -> u.getPlayerName().equalsIgnoreCase(playerName))
                .findAny().orElse(null);
    }

    public Set<User> getUsers(){ return users; }

    public void addUser(User user){
        users.add(user);
    }

    public void removeUser(UUID uuid){
        users.removeIf(u -> u.getUUID().equals(uuid));
    }

}
