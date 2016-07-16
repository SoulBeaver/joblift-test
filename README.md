# joblift-test
Test Dropwizard/Spring application for joblift.de that shortens URLs

To run:
0. open ```earalov-test``` in console
1. ```mvn clean install```
2. ```java -jar ./target/earalov-test-app-1.0-SNAPSHOT.jar server ./target/classes/
config.yml```

Available URLs:

    GET     /shortener/tld-stats
    POST    /shortener/urls?strip={boolean}
    GET     /shortener/urls/{url-hash}