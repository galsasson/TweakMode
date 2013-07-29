TweakMode
=========

Tweak Mode allows changing numeric constants in a sketch in realtime.

When the sketch is being executed, Tweak Mode will replace all occurences of numbers in the code with interactive 
handles in the PDE. A handle can be dragged around to change the value of the number. The sketch window will be updated
with the new value showing the result while running. This mode is very useful if you want to refine a certain 
feature/color/behaviour in your sketch, if you want to experiment freely with numbers, or if you try to understand
someone else's code. other uses are welcome.

This mode extends JavaMode.

Build
-----
  # ant build
  
Install
-------
Make sure you have 'modes' folder in your SketchBook folder (http://processing.org/reference/environment/#Sketchbook)
If not, create one. Then copy 'dist/TweakMode' to the 'modes' directory.

Restart Processing.

