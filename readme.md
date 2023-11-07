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

## Milestone 2

## Question 1:
Do you have a working System that allows two players to play TicTacToe once? If Yes, support your answer with screenshots of the gameplay at the 
beginning, mid-game, and game over. If No, explain your progress and challenges.

## Answer 1: 

## Question 2:
Explain how the game uses the attribute turn and player of the TicTacToe class to manage the game state between the two distributed game boards. Can
we still achieve the same thing without introducing player attributes in this milestone? Why?

## Answer 2: 
The `TicTacToe` class uses the `turn` attribute to keep track of which player's turn it is while playing the game. 
The `play` method updates game state by marking current player's symbol (1 or 2) and toggles `turn`. If the `play` method
were removed it would become very difficult to keep track of the game's state and as such, yeah, without it there is no 
way to know if the buttons should be enabled or disabled to allow or keep a player from making a move

## Question 3:
The server currently deletes or resets the game move once it sends a GamingResponse to the client. Is this necessary? 
What are the pros and cons?

## Answer 3: 
By deleting an resetting the game move once it sends a `GamingResponse` to the client the game gets a few benefits: 
firstly, by deleting the move in the response you ensure that the response is the most accurate and up to date state; 
second, there is a security benefit by deleting data it prevents clients from manipulating game state on their end. 
Cons include a lack of game history as players can't see previous moves or games. Because tictactoe is a solved game this
isn't major. However if this were a game like go or chess it may be beneficial to see game history to improve play. Second, 
another con is that you can't easily add features like undo.

## Question 4:
The ButtonHandler inner class in MainActivity currently loops through all 9 buttons to know which button is clicked. 
Can you think of a way to not loop through all the buttons? 
You can implement it to support your explanation, but it is not compulsory.

## Answer 4:
Yes. It is possible to not loop through all the buttons. This could be achieved using a modified `ButtonHandler` that uses
`setTag` and `getTag` methods from android's view class. In this case `setTag` could be used to give every button a unique 
tag in the format `row:col` where row and col are the positions of the button. `getTag` could then be used to retrieve the tag 
that matches the button in the `onClick` method. 