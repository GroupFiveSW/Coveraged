# Coveraged
In this repository you can find the code for the DIY tool, a tool that automatically detects and injects code into branches of predefined functions to calculate branch coverage.

## Summary

### Program
When running the main-routine, the program begins with initalizing the function paths and environment variables (path of Project and jdk installation). When initialization is done, the program parses the different functions (using JavaParser) to detect and inject code into branches. The functions that are being injected in the source code are the following:

* `CovergaStore.wrap(T val, String methodId, int branchId)`, where 
    * `val` is a potential value being returned in a ternary expression (e.g ```return true ? 1 : 2;``` would convert into  <br/> ```return true ? CoverageStore.wrap(1, methodId, branchId) : CoverageStore.wrap(2, methodId, branchId);``` ),
    * `methodId` is the path to the method and
    *  `branchId` is the id of the branch (unique for each branch) 

    When this function is executed the branch is considered taken and its value is inserted into a hashmap `HashMap<String, boolean[]> branchMap = new HashMap<>()` (where the key is the methodId and the value is a boolean array that has the size of the ammount branches in that method).

* `CoverageStore.init(String methodId, int count)`. <br/>
    This method is injected in the beginning of a function to tell our store how many branches there are within this function (`count` is equal to the number of branches within a function)

* `CoverageStore.writeToFile(String methodId)` <br/>
This method is injected in the last line of a function (before a potential return statement) and writes the information about branches taken (which can be found in the hashmap) to file.

When the code has been injected into the source code, our tool runs Gradle test (with the injected code) for the specified project as a background process. And, whenever one of the branches of the predefined functions are taken, our injected code executes and stores the necessary information on file. At the end of the program we are able to read the information from the file and calculate the branch coverage for the functions as well as the total branch coverage.

### Tooling

* **Programming Language:** Java was used due to all members having experience with it and its support for testing.
* **Build Tools** Maven and Gradle
* **Parsers** JavaParser
