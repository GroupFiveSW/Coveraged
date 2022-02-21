# Assignment 3: Code Coverage (DD2480)

In this assignment we were tasked to do a series of tasks related to code coverage to our selected forked repository [Algorithms](https://github.com/GroupFiveSW/Algorithms).

## Project Structure

In this repository you can find the code for Task 1. In the forked [Algorithms](https://github.com/GroupFiveSW/Algorithms)  repository you can find the accompaning issues and tasks for this assignment.

Answers to any questions can be found in our [Google Doc document](https://docs.google.com/document/d/1bt9GIrMK8Onisi5SNo1qF5o6E88D0EX2Kfyqa3HVL58/edit?usp=sharing).

## Summary

### Task 1

Here we were tasked with writing a DIY-solution to test the code-coverage of our five selected functions. We use [JavaParser](http://javaparser.org/) to inject a code-statement at every branch into the original code. Once the injection is done, we run the tests which will run our injected statements if the branch has been reached. By saving each branched reached in a HashMap, and then find the percantages of branches reached, we find the code coverage.

### Task 2

We were tasked with implementing tests to improve our code-coverage and thus we wrote 2 tests per group member in the forked [Algorithms repo](https://github.com/GroupFiveSW/Algorithms).

### Task 3

We were tasked with documenting a plan on how we would reduce the complexity of five functions, and this documented plan can be found in our [Google Doc document](https://docs.google.com/document/d/1bt9GIrMK8Onisi5SNo1qF5o6E88D0EX2Kfyqa3HVL58/edit?usp=sharing).

### Task 4

We were tasked with documenting our way-of-working according to the Essence-standard and this can be found in our [Google Doc document](https://docs.google.com/document/d/1bt9GIrMK8Onisi5SNo1qF5o6E88D0EX2Kfyqa3HVL58/edit?usp=sharing).

### Tooling

- **Programming Language:** Java was used due to all members having experience with it and its support for testing.
- **Project Updates:** GitHub built-in _Projects_ tool was used with an active Kanban-board. You can find this board connected to the forked Algorithms repo.
- **Build tools:** Maven and Gradle

---


## Group Members:
- Gabriel Acar (Gabriel-Acar)
- Elias Bonnici (elibon99)
- Gustaf Halvardsson (gustafvh)
- Alexander Krantz (Klako)
- Oscar Spolander (Carnoustie)

## Contributions
(# = IssueNumber on Github if applicable)

### Gabriel Acar
- Task

### Elias Bonnici
- Code Coverage tool, task 1 (#8)
- Implement thorough README.md (#23)

### Gustaf Halvardsson
- Task

### Alexander Krantz
- Task

### Oscar Spolander
- Task

## How to run the code
Thanks to the already existing configuration of the Algorithms-repo you can run, build and test the code in the most easy manner with your integrated IDE (like Intellij for ex.) in accordance to their onboarding.

To run it via the command-line use `gradle test` (Make sure your JDK is correctly set in accordance to the onboarding guide).

