# Tic-Tac-Toe Server

Cary Keesler, Tyler Cady, and Ernesto Sola-Thomas

## Milestone 1

Git Practice: [Repo](https://github.com/esola-thomas/Intro_to_Git)

During this milestone we created outlines of the classes that we are going to be using in later milestones. The
classes `Event`, `User`, `ServerHandler`, `SocketServer`, `GamingResponse`, `Request`, and `Response` were created in
three different packages: `model`, `server`, and `socket`. Along with all of these classes we needed to create tests to
make sure that the classes' functionality works as intended. We also added comments for the classes and their respective
methods so that we could create Javadoc documentation for the project.

### Question 1:

In your test class `SocketServerTest`, what happens if you provide a negative value for a port number? (Remember port
numbers are always positive) How do you think you can fix these issues?

#### Answer 1:

It will throw an `IllegalArgumentException` at runtime. To fix the issue add input validation to verify the value is
between (1-65535) valid port number range.

### Question 2:

In your `ServerHandler` class, what happens if you do not override the `run()` function? Is there an error? Why?

#### Answer 2:

There would be an error if run wasn't overridden. Since `ServerHandler` extends `Thread` and `Thread` has a
method `run`, if you were to call `run` on a `ServerHandler` instance, without overriding, it would use the
implementation provided by `Thread` and would not produce the expected outcome.