# Lejos NXJ AI Labs

Each lab is described in the comments in the respective programs.

## LineFollower

Speed-and-Kp-Table.txt shows the results of this lab.

## ColorID

ColorData.xlsx shows the results of this lab.

## HillClimber and RandRestartHC:

After working through the Hill Climber algorithm and testing it a number of times I noticed that my robot only found local maximums, not the global maximum. I also noticed that the robot never needed to go south (only after completing this did I realize there are situations when I would want to go backwards). From these two observations I decided to go with random restart to allow my robot to break out of local maximums, and I also implemented a bit of basic Tabu in that my robot never goes backwards. The random restart hill climber worked extremely well (always found the global goal) with a the highest number of restarts being 6 and the lowest being 2.
