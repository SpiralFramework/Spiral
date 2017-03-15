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

### Misc Formats

## TGA

Credit: [npedotnet/TGAReader](https://github.com/npedotnet/TGAReader) (Redistributed under the MIT license with credit)

TGA files are a mess, which is why I was glad to not have to handle them myself. If you're interested in the technical details of the format, you can check [here](https://en.wikipedia.org/wiki/Truevision_TGA#Technical_details).