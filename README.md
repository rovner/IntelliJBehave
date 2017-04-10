JBehave Syntax Support for IntelliJ IDEA
=========================================

IntelliJ IDEA Plugin for JBehave

This plugin provides some support for JBehave.

It is a fork of JBehaveSupport, adapted and updated by Bert Van Vlerken and Victor Rosenberg.
JBehaveSupport in turn is a fork of IntelliJBehave, originally created by Aman Kumar.

This plugin also incorporates changes from JBehaveSupport fork by RodrigoQuesadaDev to support Kotlin 1.0.4.

See https://github.com/kumaraman21/IntelliJBehave/wiki

Most of the original code has been retained, but several improvements have been incorporated by various contributors:
* https://github.com/jarosite
* https://github.com/Arnauld
* https://github.com/harley84

Latest
------
Latest changes in this plugin are to support IntelliJ IDEA 2016.1 and up.

Update 2017/4/10:
In reference to issue #2, added completion suggestions for Lifecycle keywords.
Added Lifecycle keywords to the Colors & Fonts settings page, so one can set highlighting for these keywords.
In a first attempt for issue #1, added basic support for completion of Given Stories;
This first implementation searches for story files within the module path, including dependencies and libraries
and return the list of found files as suggestions. It assumes story files are found within the 'resources' folder.

Update 2017/3/24:
Fixed issue #2, added recognition of Lifecycle keywords, no highlighting added yet.

Update 2017/3/20:
Update localized lexer to handle GivenStories.
Added GivenStories type and text to the highlighter and Colors & Fonts settings page for JBehave.
Change highlighting of Meta keyword and tags.

Features
--------
The plugin provides the following features:
* Basic syntax highlighting for JBehave story files
* Jump to step definition in Java or Groovy
* Error Highlighting in story if step was not defined
* Create new story files from a configurable story template
* Comment/uncomment lines in story files
* Code inspections to report unused steps definitions and undefined step usages
* Run *.story files

Known limitations
-----------------
* Searches complete module classpath, no configuration available to limit scope
* Does not take into account any custom JBehave configuration
