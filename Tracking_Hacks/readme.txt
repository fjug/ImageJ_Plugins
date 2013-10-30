How to install this script(s) as ImageJ or Fiji plugins:
========================================================
Simply copy the entire folder into your ImageJ or Fiji plugins folder.
On a mac you will have to browse to 'Applications/Fiji.app' and open 'Fiji.app' to see its contents. (It is basically a folder that you can open by right-click+show package content).

After you restart ImageJ/Fiji you will find the plugin in the plugin-menu.

After you've started and used the tool you will find a folder 'output' containing the cropped images in the source-folder that contained the original '.tif'-files.

Have fun! :)



How to run this script without making it a ImageJ or Fiji plugin:
=================================================================

Open Fiji
File - New - Script
File - Open - open ManuallyCropFrames.bsh
Click 'Run'
Decide on size of movie + boring file prefix
Choose a input folder (should contain '.tif'-files)

start using aw(ful|some) tool (pushing button 'h' shows and hides help)

In selected folder you will now find a subfolder 'output'.
Import this folder as a stack into Fiji.
Export as movie...

:)