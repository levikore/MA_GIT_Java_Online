package engine.repositories;

import java.util.*;

public class RepositoriesManager {
    private final Map<String, UserData> m_UsersDataHashMap;

    public RepositoriesManager() {
        m_UsersDataHashMap = new HashMap<>();
    }

    public synchronized void addUserData(String i_UserName, UserData i_UserData) {
        if (!isUserExists(i_UserName) && i_UserData.getRepositoriesDataList() != null) {
            m_UsersDataHashMap.put(i_UserName, i_UserData);
        }
    }

    public synchronized void addRepositoryData(String i_Username, RepositoryData i_RepositoryData) {
        if (!isUserExists(i_Username)) {
            List<RepositoryData> repositoriesList = new LinkedList<RepositoryData>();
            repositoriesList.add(i_RepositoryData);
            UserData userData = new UserData(repositoriesList, i_Username);
            addUserData(i_Username, userData);
        } else {
            m_UsersDataHashMap.get(i_Username).AddRepositoryData(i_RepositoryData);
        }
    }

    public synchronized Set<String> getUsers() {
        return Collections.unmodifiableSet(m_UsersDataHashMap.keySet());
    }

    public boolean isUserExists(String i_UserName) {
        return m_UsersDataHashMap.keySet().contains(i_UserName);
    }

    public UserData GetUserData(String i_UserName) {
        UserData userData = null;
        if (isUserExists(i_UserName)) {
            userData = m_UsersDataHashMap.get(i_UserName);
        }
        return userData;
    }

    public Map<String, UserData> getUsersDataHashMap() {
        return m_UsersDataHashMap;
    }

    public int GetVersion() {
        return m_UsersDataHashMap.size();
    }


}


