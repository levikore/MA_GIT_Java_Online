import com.google.gson.Gson;
import engine.users.UserManager;

public class TestDependenciesWork {
    private void foo(){
        UserManager userManager=new UserManager();
        Gson gson=new Gson();
        gson.toJson("Yair");
        System.out.println("is Yair exist?"+ userManager.isUserExists("Yair"));
    }
    public static void main(String args[]){
        TestDependenciesWork test=new TestDependenciesWork();
        test.foo();
    }
}
