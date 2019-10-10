package engine.repositories;

import engine.logic.Branch;
import engine.logic.Commit;

import java.util.*;

public class RepositoriesManager {

    private final Map<String, UserData> usersDataHashMap;

    public RepositoriesManager() {
        usersDataHashMap = new HashMap<>();
    }

    public synchronized void addUserData(String i_UserName, UserData i_UserData) {
        if (!isUserExists(i_UserName) && i_UserData.getRepositoriesDataList() != null) {
            usersDataHashMap.put(i_UserName, i_UserData);
        }
    }

    public synchronized void addRepositoryData(String i_Username, RepositoryData i_RepositoryData) {
        if (!isUserExists(i_Username)) {
            List<RepositoryData> repositoriesList = new LinkedList<RepositoryData>();
            repositoriesList.add(i_RepositoryData);
            UserData userData = new UserData(repositoriesList, i_Username);
            addUserData(i_Username, userData);
        } else {
            usersDataHashMap.get(i_Username).AddRepositoryData(i_RepositoryData);
        }
    }

    public synchronized Set<String> getUsers() {
        return Collections.unmodifiableSet(usersDataHashMap.keySet());
    }

    public boolean isUserExists(String i_UserName) {
        return usersDataHashMap.keySet().contains(i_UserName);
    }

    public UserData GetUserData(String i_UserName) {
        UserData userData = null;
        if (isUserExists(i_UserName)) {
            userData = usersDataHashMap.get(i_UserName);
        }
        return userData;
    }



}


