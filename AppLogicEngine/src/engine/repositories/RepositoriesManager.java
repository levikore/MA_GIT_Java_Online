package engine.repositories;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RepositoriesManager {

        private final Map<String, UserData> usersHashMap;

        public RepositoriesManager() {
            usersHashMap = new HashMap<>();
        }

        public synchronized void addRepository(String username,UserData userData) {
            if(!isUserExists(username)) {
                usersHashMap.put(username, userData);
            }
        }

        public synchronized Set<String> getUsers() {
            return Collections.unmodifiableSet(usersHashMap.keySet());
        }

        public boolean isUserExists(String username) {
            return usersHashMap.keySet().contains(username);
        }
    }


