This folder contains the code format settings for eclipse.

Unfortunally Eclipse still does not allow to export all 
settings via a single export file.

There are three different screens in the eclipse 
configuration relevant for the code format:

1) Java > Code style
Please see clipse-codestyle.png for a screen shot of the 
appropriate settings.

2) Java > Code style > Formatter
Import the settings from the file: 
eclipse-code-format-termgenie.xml

This will create a code format template called: 
'Custom TermGenie Eclipse'

3) Java > Code style > Organize Imports
Please see clipse-codestyle-organize-imports.png for a 
screen shot of the appropriate settings.


With these setting it should be possible to use the build-in 
code formaters/ import organizer in Eclipse, while keeping 
format related changes to a minimum.

 