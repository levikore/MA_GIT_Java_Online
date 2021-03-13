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

#### Future todo list:
- upload project to an AWS server

# Instructions:
1. Copy app.war from "MA_GIT_ONLINE" folder to CATALINA_HOME/webapps , e.g., c:/Tomcat8/webapps.
2. Start the Tomcat server.
3. Navigate to http://localhost:8080/app.

## Setup:
5. A login screen will apear:

![image](https://user-images.githubusercontent.com/41550958/111043511-a9232b00-844b-11eb-88a9-d49b5c6a820e.png)

5. By pressing the green "New Repository" button, a browse window will open from which you can load a repository from an XML file:

![image](https://user-images.githubusercontent.com/41550958/111043626-29e22700-844c-11eb-9281-ca014902b977.png)

   you can use the repositories from the example repository folder
   
6. Uppon loading the file successfully, your repositories will be presented on the home tab

![image](https://user-images.githubusercontent.com/41550958/111044331-8d6e5380-8450-11eb-9d70-fe0b14b65387.png)

7. Pressing on the repository will open its control panel:

![image](https://user-images.githubusercontent.com/41550958/111044394-0bcaf580-8451-11eb-9bc9-bdab9a4dc37b.png)

## root folder update, commit:
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

- uppon pressing on a file name you will see it's content
- to edint press "Edit" and when you're done press "done!"
- to remove a file press "Remove"
- uppon picking a folder, you can press "add new file to current folder" to add a new file to the folder.
  A new empty file will appear in the file list, you cannot save it until you add content to it.
- to save the root folder's content press "save" before exiting the control panel.
- uppon saving the conten, you can commit the changes you have made.
  the number of uncommited changes will apear on the "Uncommitted Files" button.
  by pressing it you can see the uncomitted changes:
  
  ![image](https://user-images.githubusercontent.com/41550958/111044944-2ce11580-8454-11eb-942d-a5355526fc7e.png)

## Pull:
- Pull Request (blocked if there is no remote reference):

![image](https://user-images.githubusercontent.com/41550958/111045001-6d409380-8454-11eb-8ab6-415aa7c474d7.png)

  uppon pressing submit, the remote user will get a message: 
  
  ![image](https://user-images.githubusercontent.com/41550958/111047678-c0671600-8455-11eb-852e-03b17c74f2e9.png)

## Fork, Push Request: 
9. Pressing "All Users" will present the user list
   - pressing on a user name will present his repository.
   
   ![image](https://user-images.githubusercontent.com/41550958/111048806-0d4aec80-8456-11eb-9bc5-d968000bea8b.png)
   
10. Clicking Repository will display the repository information and its contents.
11. Click Fork for this repository: 
  
![image](https://user-images.githubusercontent.com/41550958/111050654-8c402500-8456-11eb-885c-e08558d23018.png)

12. After the Fork, you can see the new repository on the Home page and go to its control page
In this case all remote reference options will be open

13. A user who has received a PR will click on the PR List in the toolbar and will see his entire PR history: 

![image](https://user-images.githubusercontent.com/41550958/111050684-c4dffe80-8456-11eb-986b-4b45baae427e.png)

14. Clicking on the PR body will display its Delta Commits, clicking on each comit in the list will display its list of files, update type and contents. 

![image](https://user-images.githubusercontent.com/41550958/111050731-08d30380-8457-11eb-9249-1b36a0056635.png)

15. You can write a comment in "rejection messege" and by clicking on reject it will be sent to the user who sent the PR and will appear in his alerts, otherwise you can click on accept and it will also appear in the alerts.

![image](https://user-images.githubusercontent.com/41550958/111050781-4df73580-8457-11eb-8f81-7f864177342e.png)

![image](https://user-images.githubusercontent.com/41550958/111050785-551e4380-8457-11eb-8051-d0ca05699729.png)


