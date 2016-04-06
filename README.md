# Spiral
Spiral is a Danganronpa Modding Framework written in Java

# Usage
Run Spiral through a command line using java -jar Spiral.jar

To install a mod, type 'install', then 'install <mod>' to install a mod, or 'info <mod>' to learn more about the mod. 

You can exit the install prompt using 'exit'

To backup the .wad file, type 'backup', and to restore a backup, type 'restore'.

To extract the .wad file to make mods, type 'extract'.

# Mods
Mods are basically .zip files renamed to .dr1, and are put in a 'mods' folder.

Include a mod.info file with the format of:


Mod Name

Author Name

Description


Any files included in the .dr1 file will be included in the repacked .wad

NOTE: Installing mods requres at least 3.5 GB of harddrive space reserved for the extraction, however this folder is deleted after use.

Installing mods takes around 1 to 1 and a half minutes.

# Roadmap
Current Plan:

* Get basic mod installation working
* Get unpacking and repacking of .pak files working
* Get decompiling and recompiling of .lin files working
* Find a way to directly pull data from the .wad file, rather than having to unpack the entire .wad file

# Credit
Primary credit goes to https://github.com/AdmiralCurtiss/HyoutaTools and https://github.com/TcT2k/HLMWadExplorer for providing the basis of this program.

# Patreon
I do this work entirely in my spare time, and it's not always easy. While I enjoy doing it, it does take up a bunch of time.

As such, I've set up a Patreon page, if people want to support me, which can be found here: https://www.patreon.com/undermybrella

Please note: The Patreon page is entirely optional, and I won't stop my projects if nobody donates. However, it would be nice, as projects like this really do suck up a lot of time.
