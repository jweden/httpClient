This demo requires git and maven to run.

After cloning this repository, the maven command required for this to run is:

mvn -DargLine="-Dapikey=<your api key> -Duser=<your username> -Dpassword=<your password>" test

This goes against the Constant Contact API.
It uses the soon-to-be-deprecated basic authentication login.
Tests are harnessed in TestNG.  The http client used in the tests is
based upon Apache HTTP Components (i.e. not the old deprecated Apache
Commons HttpClient).

The first test is a multi-threaded test that sends in the same request
5 times and ensures a correct response (Content-Type header) to each of
these requests.  The second test sends in a bad oauth2 login and parses
the json response for correct error messages.

This was tested with the following environment:

>mvn -version
Apache Maven 3.0.1 (r1038046; 2010-11-23 05:58:32-0500)
Java version: 1.6.0_23
Java home: C:\Program Files\Java\jdk1.6.0_23\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 7" version: "6.1" arch: "amd64" Family: "windows"

