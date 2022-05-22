# Bug-jet Printer

A recent poll found that 100% of developers write code that doesn't work. This tool aims to help mitigate the effects of that problem by providing an easy way to track a variable's value across the life of a program. The tool can be used in place of the less elegant solutions developers often resort to, like inserting thousands of print statements into their already appalling code. 


# Screen Shot:

<img width="1094" alt="Screen Shot 2022-05-21 at 12 01 07 PM" src="https://user-images.githubusercontent.com/70608555/169698772-48cd6442-14f8-4d02-94a7-48fabf226487.png">

## How it works
1. Write some code that doesn't work
2. For each variable you wish to track add the `@Track` annotation above the method where the variable is first seen. For example, if you wanted to track the variable `myObject`:
   ```
   @Track(var = "myObject", nickname = "myObject")
   void myMethod() {
       MyObject myObject = new MyObject();
       myObject.doStuffThatChangesMyObject();
   }
   ```
   
   `@Track` takes in two String arguments: `var`—the name of the variable you want to track—and `nickname`—the name you want the variable to be represented as in the tool's visualization. The `nickname` argument can be anything, including the actual name of the variable. It is intended to be used to differentiate two variables with the same name. e.g.
      ```
      @Track(var = "i", nickname = "myMethodI")
      void myMethod(int i) {
        ...
      }
   
      @Track(var = "i", nickname = "otherMethodI")
      void otherMethod() {
         int i = 5;
         ...
      }
      ```
      The tool automatically limits tracking to the scope of a method, thus it's not necessary for users to give unique names to variables or nicknames for the program to function properly. However, you may wish to do so to avoid confusion when looking at the visualization. 

   Ensure that you do not delete the default import statement for the `Track` annotation when adding your code to the editor. Not having this statement will cause errors.  
3. (Steps 3-4 assume the use of IntelliJ) Start the backend by running `Project2Group10/src/main/java/com/restservice/RestServiceApplication.java`. If the imports in this class are not recognized then right click the directory `Project2Group10/lib` and select "Add as Library...".
4. Start the frontend by running the following commands from the terminal:
   ```
   cd frontend
   npm install
   npm run start
   ```
   A browser window running the tool should open.
5. Import a file or add your code to the tool's editor. Run it to render variable histories. Step through the visualization to see how and where your variable changes.
6. To return to the editor screen after running use the "Refresh" button in your browser.

See the `Project2Group10/examples` folder for some example code snippets that you can try out. These examples programs were purposely injected with bugs
which can be debugged with the analysis tool.
Additional (test) examples can be found within the `Project2Group10/backend/test` directory.

## What we support
1. Tracking primitive, array, and user-defined local variables. Note that arrays include arrays of primitives and user-defined objects. e.g.
   ```
   @Track(var = "prim", nickname = "prim")
   @Track(var = "arr", nickname = "arr")
   @Track(var = "userDefObj", nickname = "userDefObj")
   void myMethod() {
        int[] arr = new int[10];
        UserDefinedObject userDefObj = new UserDefinedObject();
        userDefObj.setNum(10);
        for (int prim = 0; prim < arr.length; prim++) {
            arr[prim] = obj.getNum();
        }
   }
   ```
2. Tracking of primitive, array, and user-defined method arguments 
      ```
   @Track(var = "prim", nickname = "prim")
   @Track(var = "arr", nickname = "arr")
   @Track(var = "userDefObj", nickname = "userDefObj")
   void myMethod(int i, int prim, int[] arr, UserDefinedObj userDefObj) {
        UserDefinedObject userDefObj = new UserDefinedObject();
        userDefObj.setNum(10);
        for (i = 0; i < arr.length; i++) {
            arr[i] = prim;
        }
   }
   ```
3. Tracking of aliases for objects (including arrays). By aliases we refer to both other local variables within the same scope, e.g.
   ```
   @Track(var = "userDefObj", nickname = "userDefObj"
   void myMethod() {
        UserDefinedObject userDefObj = new UserDefinedObject();
        UserDefinedObject alias = userDefObj;
        alias.setNum(11);         // will be tracked
   }
   ```
   and method arguments, e.g.
   ```
   @Track(var = "userDefObj", nickname = "userDefObj"
   void myMethod() {
        UserDefinedObject userDefObj = new UserDefinedObject();
        doSomething(userDefObj);
   }
   
   void doSomething(UserDefObj anotherAlias) {
        anotherAlias.setNum(12);   // will be tracked
   }
   ```
   Note that this second category, aliases as method arguments, also includes the tracking of mutations at an arbitrary level of nesting. e.g.
   ```
   @Track(var = "userDefObj", nickname = "userDefObj"
   void myMethod() {
        UserDefinedObject userDefObj = new UserDefinedObject();
        userDefObj.num = 0;
        doRecursiveSomething(userDefObj);
   }
   
   void doRecursiveSomething(UserDefObj alias) {
        if (alias.num >= 10) {
            return;
        }
        alias.num += 1;
        doRecursiveSomething(alias);
   }
   // mutation history for userDefObj.num will be logged as [0, 1, 2, ... , 10]
   ```

## What we don't support
1. Tracking of individual fields. e.g. 
   ```
   @Track(var = "a.size", nickname = "a.size")
   void myMethod() {
        A a = new A();
        a.size = 5;
   }
   ```
   Individual fields can be indirectly tracked by tracking their enclosing object. In the example above the history of changes to `a.size` can be collected by tracking `a`. Note, however, that changes to `a`'s other fields will also be captured in this history.
2. Tracking of non-user defined objects (with the exception of arrays). e.g.
   ```
      @Track(var = "myList", nickname = "myList")
      void myMethod() {
           List<String> list = new ArrayList<>(); 
           list.add("a"); // we do not guarantee that this will be tracked properly
      }
   ```
   Note that this includes fields of user defined objects inherited from non-user defined classes. Any inherited non-user defined field that is declared as private cannot be accessed by our tool and thus will not be tracked properly. e.g.
   ```
   public class MyMap extends HashMap {
        ...
   }
   
   ...
   
   @Track(var = "myMap", nickname = "myMap")
   void myMethod() {
      MyMap<String, String> myMap = new MyMap<>(); 
      myMap.put("a", "b"); // we do not guarantee that this will be tracked properly
   }
   ```
3. Separate tracking of multiple local variables that have the same name within a single method. e.g.
   ```
   @Track(var = "i", nickname = "i")
   void myMethod() {
      for (int i = 0; i < 5; i++) {
         ...
      }
   
      if (conditionalMethod()) {
         int i = 6;
      } else {
         int i = 7;
      }
   }
   ```
   Mutations to `i` in both the for loop and the if/else block will be logged as part of a single history for `i`.
4. Precise tracking of loop iterators. We are not able to log the final value of a loop iterator declared inside a for loop. e.g.
   ```
   @Track(var = "i", nickname = "i")
   void myMethod() {
      for (int i = 0; i < 5; i++) {
         ...
      }
   }
   ```
   In the above example `i` reaches a final value of 5 before the loop is broken and it goes out of scope. This happens before we're able to log its final value. Thus its history will be logged as `[0, 1, 2, 3, 4]` while its true history is `[0, 1, 2, 3, 4, 5]`. 

## Program requirements
Code fed to our tool must 
1. include a `main` method with the signature `public static void main(String[] args)`. The signature can also include any exceptions the method throws.
   ```
   public static void main(String[] args) {
      // this is valid 
   }
   
   @Track(var = "i", nickname = "i")
   void myMethod(int i) {
      ...
   }
   ```

   ```
   public static void main(String[] args) throws FileNotFoundException {
      // this is also valid 
   }
   
   @Track(var = "i", nickname = "i")
   void myMethod(int i) {
      ...
   }
   ```

   ```
   public static void main(String[] x) {
      // this is not valid
   }
   
   @Track(var = "i", nickname = "i")
   void myMethod(int i) {
      ...
   }
   ```
2. follow Java naming conventions for variables. Local variables, method arguments, and non-final fields must be named using camelcase e.g.
   ```
   @Track(var = "userDefObj", nickname = "validlyNamedVariable")
   void myMethod() {
        UserDefinedObject userDefObj = new UserDefinedObject();
        userDefObj.setNum(10);            // userDefObj is named in camelcase, this is valid
        int i = userDefObj.FINAL_FIELD;   // FINAL_FIELD is not camelcase, this is valid since the field is final
        System.out.println("hello world") // System is not camelcase, this is valid since System is not a local variable/field/method arg
   }
   ```

   ```
   @Track(var = "UserDefObj", nickname = "InvalidlyNamedVariable")
   void myMethod() {
        UserDefinedObject UserDefObj = new UserDefinedObject(); // UserDefObj is local and not camelcase, this is not valid
   }
   ```
3. use curly braces (i.e. `{}`) for any control flow blocks. This includes loops and if/else statements. For example, if the user wanted to track variable `i`:

    ```a
    for (int i = 0; i < size; i++)   // this is not valid
        doSomething();
    ```
    
    ```
    for (int i = 0; i < size; i++) { // this is valid
        doSomething();
    }
   ```
