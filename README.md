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

5. By pressing the green "New Repository" button, a browse window will open from which you can load a repository from an XML file:

![image](https://user-images.githubusercontent.com/41550958/111043626-29e22700-844c-11eb-9281-ca014902b977.png)

   you can use the repositories from the example repository folder
   
6. Uppon loading the file successfully, your repositories will be presented on the home tab

![image](https://user-images.githubusercontent.com/41550958/111044331-8d6e5380-8450-11eb-9d70-fe0b14b65387.png)

7. Pressing on the repository will open its control panel:

![image](https://user-images.githubusercontent.com/41550958/111044394-0bcaf580-8451-11eb-9bc9-bdab9a4dc37b.png)

- On the screen center you can see the branches list. The active branch will be branded as "Active" near its name.
- To the right you can see the list of the branche's commits.
- Pressing on a commit will present the files in it's root folder.
- "push local head branch" button will push the local branch into the remote repository (blocket fro all rtbs).
- "push" button pushes head branch rtb ino rb (blocked if the is no correlating local head branch).
- "pull" button pulls rb into rtb (blocked if there is no Remote Reference).

- uppon entering rb name in checkout:

![image](https://user-images.githubusercontent.com/41550958/111044655-89dbcc00-8452-11eb-948c-cfd6b31e26ca.png)

- the "commit" button is blocked if there are no open changes.
- "working copy" button will open the root folder control panel:

![image](https://user-images.githubusercontent.com/41550958/111044688-c3acd280-8452-11eb-9352-6c47d42b209d.png)





