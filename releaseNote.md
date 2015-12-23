Note that this project is currently configured to release to maven central, via sonatype.  You must have a sonatype account to do this, and 
a GPG signing key (publicly registered).  See http://central.sonatype.org/pages/apache-maven.html#deploying-to-ossrh-with-apache-maven-introduction

If you do not want to release to that repo, don't enable the publicRelease profile, and set the parameter -DnoDeploy

For a public release - published to Maven Central - use a command like this:
```
mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=1.4.1 
-DdevelopmentVersion=1.5-SNAPSHOT -PpublicRelease
```

For a release that will not be published to Maven Central - (but will still be tagged and pushed to git) use a command like this:
```
mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=0.0.5 
-DdevelopmentVersion=0.0.6-SNAPSHOT -DnoDeploy
```