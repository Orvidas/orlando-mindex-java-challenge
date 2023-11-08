# Overview
I've made this file to share my thoughts on the changes I made throughout the challenge and my thought processes while making them.
Each task has its own section, where I explain the flow from the endpoint to the logic, and finish with going over my test cases.

## Task 1
The first task was making an endpoint to get the reporting structure for a given employee id. Since nothing is persisted 
with this endpoint, a GET mapping was appropriate for this call. 

Within the EmployeeServiceImpl, I first get the requested employee's entry, and verify that it does in-fact exist. To find 
the number of direct reports, I use recursion to go through the list of direct reports for each employee under the requested 
employee. An empty/null list is used as the termination condition for the recursion.

For test cases, I used the provided employee_database.json file to generate and insert test data into the in-memory test database. 
Then I made three test cases for this task: a use case where the employee provided has nested direct reports that need to be 
searched through, a use case where the employee provided has 0 direct reports, and one where the employee is invalid, thus 
a 500 error code is returned from the controller.

## Task 2
The second task required two new endpoints for the new compensation class. Thus, they required a POST and a GET endpoint. 
The requirements didn't explicitly mention being able to modify existing compensation entries, so I did now allow that functionality 
in the logic. If I were to add it, it would be with a third PUT endpoint that requires an existing employee id and the new 
Compensation class they want to override it with. 

When creating the Compensation class, I used the @Id annotation on the employee field to ensure that no employee duplicates can 
be added to the Compensation table. This prevents the GET endpoint from breaking due to duplicate employees found. Otherwise, the 
implementations were simply verifying the employee exists before either writing, or reading the compensation and returning it 
to the user. 

I made two test cases for this task, though the first task tests both reading and writing a valid compensation entry to the table. 
I still use the employee_database.json file to generate employee data for my test cases to compare against. The second test case 
tests an invalid employee idea, where it will return a 500 error code just like the endpoint in the first task. 