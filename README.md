# MA_GIT_JAVA_ONLINE
An online web application similar to "GitHub".
The app allows the user to create an empty repository or open an existing one from an XML file.
The user can update the files in the ropository i.e update file content, add new files or change folders and commit his saves to the repository.
Furthermore the user can create a new Branch, Fork another user's repository, push new repositories to remote branches etc.

## Developing the app involved: 
 - front end design
 - using XML Dom parser to save and load repositories to XML files
 - working with local files
 - file encryption using SHA-1
 - calculating deltas between root folders and files to detect uncommited changes
 - accommodating work for multiple users
 - developing both client and server sides

### Server side:
Coded in Java. Runs on Apache Tomcat server.

### Client side:
Coded in JavaScript, jQuery, html and CSS (with bootstrap).

## Run:
1. Copy app.war from "MA_GIT_ONLINE" folder to CATALINA_HOME/webapps , e.g., c:/Tomcat8/webapps.
2. Start the Tomcat server.
3. Navigate to http://localhost:8080/app.
4. A login screen will apear:\

![image](https://user-images.githubusercontent.com/41550958/111043511-a9232b00-844b-11eb-88a9-d49b5c6a820e.png)

5. By pressing the green "New Repository" button, a browse window will open from which you can load a repository from an XML file:\

![image](https://user-images.githubusercontent.com/41550958/111043626-29e22700-844c-11eb-9281-ca014902b977.png)

   you can use the repositories from the example repository folder
   
6. Uppon loading the file successfully, your repositories will be presented on the home tab\

![image](https://user-images.githubusercontent.com/41550958/111044331-8d6e5380-8450-11eb-9d70-fe0b14b65387.png)

7. Pressing on the repository will open its control panel:\
![image](https://user-images.githubusercontent.com/41550958/111044367-cd353b00-8450-11eb-91a8-6b260d1edf00.png)


