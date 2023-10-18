# Tic-Tac-Toe Server

Cary Keesler, Tyler Cady, and Ernesto Sola-Thomas

## Milestone 1

### Question 1: 
In your test class `SocketServerTest`, what happens if you provide a negative value for a port number? (Remember port numbers are always positive) How do you think you can fix these issues? 
### Answer 1:
It will throw an IllegalArgumentException at runtime. To fix the issue add input validation to verify the value is between (1-65535) valid port number range.

### Question 2:
In your `ServerHandler` class, what happens if you do not override the `run()` function? Is there an error? Why?
### Answer 2: 

