package engine.repositories;

import java.util.*;

public class RepositoriesManager {

        private final Map<String, UserData> usersDataHashMap;

        public RepositoriesManager() {
            usersDataHashMap = new HashMap<>();
        }

        public synchronized void addUserData(String i_Username,UserData i_UserData) {
            if(!isUserExists(i_Username)) {
                usersDataHashMap.put(i_Username, i_UserData);
            }
        }

        public synchronized void addRepositoryData(String i_Username,RepositoryData i_RepositoryData)
        {
            if(!isUserExists(i_Username)) {
            List<RepositoryData> repositoriesList=new LinkedList<RepositoryData>();
            repositoriesList.add(i_RepositoryData);
            UserData userData=new UserData(repositoriesList);
               addUserData(i_Username,userData);
            }
            else{
                usersDataHashMap.get(i_Username).AddRepositoryData(i_RepositoryData);
            }
        }

        public synchronized Set<String> getUsers() {
            return Collections.unmodifiableSet(usersDataHashMap.keySet());
        }

        public boolean isUserExists(String i_UserName) {
            return usersDataHashMap.keySet().contains(i_UserName);
        }


    }


