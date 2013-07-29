TweakMode
=========

Tweak Mode allows changing numeric constants in a sketch in realtime.

When the sketch is being executed, Tweak Mode will replace all occurences of numbers in the code with interactive 
handles in the PDE. A handle can be dragged around to change the value of the number. The sketch window will be updated
with the new value showing the result while running. This mode is very useful if you want to refine a certain 
feature/color/behaviour in your sketch, if you want to experiment freely with numbers, or if you try to understand
someone else's code. other uses are welcome.

Requires Processing 2.0+ with OscP5 library installed.

- Based on TemplateMode: https://github.com/martinleopold/TemplateMode
- Uses JavaOSC for communication with the sketch: http://www.illposed.com/software/javaosc.html
- Extends JavaMode.

Build
-----
  # ant build
  
Install
-------
- Make sure you have a folder named "modes" in your Sketchbook folder, if not create one. The Sketchbook folder location can be found in Processing->Preferences if you don't know where it is.
- Copy "dist/TweakMode" to the "modes" directory.
- Restart Processing.

