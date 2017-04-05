# Formats
Due to *unnecessary* complications, I've made this file because the file formats used are... complicated, and having a place to write down the formats to allow for my own development and others is pretty useful, I'd say.

This should be divided into three main sections: Danganronpa formats, SPIRAL formats, and Misc formats.

In 99% of cases the formats here have been discovered and reverse engineered by someone else, in which case I'll have a link to the article/s that I used where applicable.

Should also be noted that, unless specified, the formats seem to use little endian byte orders.

# Danganronpa Formats

Fairly self explanatory, this section is for formats that are either specialised for Danganronpa, or are special/proprietary formats used.

Common examples:
* WAD
* PAK
* LIN
* CPK

## WAD

Credit: [TcT2k/HLMWadExplorer](https://github.com/TcT2k/HLMWadExplorer)

While listed here as a Danganronpa format, from what I can tell, WAD files were first used in Hotline Miami.

If you do a search for ["WAD Format"](https://www.google.com.au/search?q=WAD+format), you do get two other results - DOOM, and a Wii homebrew packaging format. From what I can tell, neither of these are the formats used.

WAD files are general purpose archives, for the most part. No compression here, thank god. They store a version number, a file list, and a list of directories and the file hierarchy, as well as the actual file data.

Enough talk, let's move onto the format itself!

* Wad Type? - 4 byte header, spelling out AGAR in ASCII
* Major Version - 4 byte integer
* Minor Version - 4 byte integer
* Header Size - 4 byte integer
* Header - [Header Size] bytes
* Number of Files - 4 byte integer, file format is as follows
  * File Name Length - 4 byte integer
  * File Name - [File Name Length] bytes
  * File Size - 8 byte long (number)
  * File Offset - 8 byte long (number)
* Number of Directories - 4 byte integer, directory format is as follows
  * Directory Name Length - 4 byte integer
  * Directory Name - [Directory Name Length] bytes
  * Number of Subfiles - 4 byte integer, subfile format is as follows
    * Subfile Name Length - 4 byte integer
    * Subfile Name - [Subfile Name Length] bytes
    * Is File - 1 byte, 0 for true, 1 for false
* File Data, at offsets defined in [File Format]

There's two peculiarities about the Danganronpa interpretation that should be noted though.
1. The Major/Minor versions do not matter. Danganronpa handles any version just fine. May steal for SPIRAL versioning, who knows.
2. **Do not attempt to use a header.** I repeat - ***Do not attempt to use the header field for a Danganronpa WAD file at this time***. 
    * Danganronpa ignores this field, but worst of all, it will crash, since it doesn't even read the size and skip over. Do not use this field.
    * If you need to store header information, SPIRAL provides a nifty Spiral Header file, which is included. Doesn't do anything, just there as a nice, easy alternative to the default WAD header.
    
## PAK

Credit: [AdmiralCurtiss/HyoutaTools](https://github.com/AdmiralCurtiss/HyoutaTools)

In a similar situation to WAD files, the PAK format may exist outside of Danganronpa, although I haven't been able to find suitable information on it's existance.

Similar to WAD files, PAK files are general archives. With one key difference, however - PAK files have no concept of format or names.

PAK archives can be thought more of as lists of data blobs than an actual structured archive. 

The use case for these tends to be when the engine knows what type of data it's expecting, and doesn't care about things like names. Things like animations work well here.

* Number of files - 4 byte integer, SPIRAL caps it out at 1024 to prevent memory issues.
* List of file offsets - Many 4 byte integers, up to [Number of Files] (Total size is therefore [Number of Files] * 4)
* File Data - Data is read from one file offset to another, meaning the first file is from the first offset to the second, the second file is the second offset to the third, and so on.

##LIN

Credit: [AdmiralCurtiss/HyoutaTools](https://github.com/AdmiralCurtiss/HyoutaTools)

A completely new format (I believe), and also the most complicated of them all. The LIN format is responsible for scripting files.

Ideally, you don't deal directly with LIN formats, and can use the custom SPIRAL format as a form of abstraction. However, sometimes you may require, or prefer, the precision of the LIN format.

* LIN Type - 4 byte integer, 1 for text, and 2 for any other. 
* Header Space - 4 byte integer.
* Size | Text Block - 4 byte integer. The use of this number varies from the two format.
* Size - 4 byte integer. Only present if LIN Type is `other`. If it's a Text LIN, then Text Block is equal to the size.
* Presumably the header - `[Header Space] - 16` bytes. Unknown
* Script Entry Definitions - Any number, between [Header Space] and [Text Block]. Format is as follows
  * `0x70` - If not present, skip over until we reach `0x70`
  * OP Code - 1 byte, short obviously for Operation Code. See the list below for a full list.
  * Arguments for the OP Code - 1 byte per argument, up till either the number of arguments for the code, or till `0x70` if the argument number is `-1`
* Number of Text Entries - 4 byte integer, details the number of text entries.
* Text Entries - [Number of Text Entries] long. Format is below:
  * Text Position - 4 byte integer. The starting point of the text to read.
  * Next Text Position - 4 byte integer. The ending point of this line of text.
  * Text - [Next Text Position] - [Text Position] bytes. UTF-16 string. The actual text itself, assigned to the entry `i`, where `i` is the iteration count (0-indexed)

# Misc Formats

## TGA

Credit: [npedotnet/TGAReader](https://github.com/npedotnet/TGAReader) (Redistributed under the MIT license with credit)

TGA files are a mess, which is why I was glad to not have to handle them myself. If you're interested in the technical details of the format, you can check [here](https://en.wikipedia.org/wiki/Truevision_TGA#Technical_details).

# OP Codes

## Danganronpa 1

* `0x00` - Text Count, two arguments. The number of text lines in the file. Argument 1 is the remainder of the full number divided by 256, and the second is the full number divided by 256.
* `0x01` - Unknown, 3 arguments.
* `0x02` - Text. The basis of the game, really. Two arguments. The first is the starting offset (0-indexed) of the text to read, and the second argument is the ending offset.
* `0x03` - Formatting. One argument. This is used for things like bolding, self talk, and so forth. `3` is **bold**, `4` is used for when the protag talks to themself, `17` is used for the Weak Points in class trials, and `69` is used when you `agree` with a statement.
* `0x04` - Filter. Four arguments. The filter to apply to a scene. Details unknown, will investigate further.
* `0x05` - Movie. Two arguments. Argument 1 is the full movie ID divided by 256, and the second is the remainder of that operation.
* `0x06` - Animation. Eight arguments. Argument 1 is the animation ID divided by 256, the second is the remainder. The third to seventh arguments are unknown. The eighth argument is the frame to use, and 255 to hide it.
* `0x07` - Unknown
* `0x08` - Voice line, Five arguments. The first is the character ID (See below). The second is the chapter to pull from, set to `99` for no chapter. The third argument is the voice line number divided by 256, and the fourth is the remainder. The fifth argument is the volume *percentage* (At least, I assume so). Default to 100.
* `0x09` - Music, three arguments. First is the music number, second is likely the volume. Third is unknown, `0` is a safe value.
* `0x0A` - Sound, three arguments. Suspect it follows the same format as `0x09`, but for sound effects.
* `0x0B` - SoundB, two arguments. Unknown.
* `0x0C` - ToggleTruthBullet, two arguments. The first argument is the ID of the truth bullet, or piece of evidence, to either enable or disable. The second argument is 0 to disable, and 1 to enable. Using `255, 0` as the arguments should clear the evidence list.
* `0x0D` - Unknown, three arguments.
* `0x0E` - Unknown, two arguments.
* `0x0F` - SetTitle, three arguments. Argument 1 is character ID, argument 2 seems to always be `0`, and argument 3 is the state of the title? Purpose is unknown, further testing is required.