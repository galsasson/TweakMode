TweakMode
=========

Tweak Mode allows changing numeric constants in a sketch in realtime.

When a sketch is being executed in tweak mode, all hard-coded numbers become interactive and can be modified by clicking and dragging to the left or right. When a value change, the PDE will update the running sketch with the new value and the result will be visible immediately. This mode is useful if you want to refine a certain feature/color/behaviour in your sketch, if you want to experiment freely with numbers, or if you try to understand someone else's code. other uses are welcome.

Requires Processing 2.0+ with OscP5 library installed.

- Based on TemplateMode: https://github.com/martinleopold/TemplateMode
- Uses JavaOSC for communication with the sketch: http://www.illposed.com/software/javaosc.html
- Extends JavaMode.

All the details are here: http://galsasson.com/tweakmode/
