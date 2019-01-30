# sudoku
A java program to solve sudoku games, with an enhanced algorithm, can be 200 times faster than the standard backtracking method

Tested with 501 games, could be completed within 5 seconds, comparing to around 1000 seconds if only apply the backtracking method.

The enhanced part is in the method Game.foreplay(), which is to reduce the alternatives for each undetermined cell with additional logical calculation. As a result, it reduced 58.9% alternatives for average with the test data shared.
