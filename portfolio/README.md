This directory is where you'll write all of your code!

By default it contains a barebones web app. To run a local server, execute this
command:

```bash
mvn package appengine:run
```
Make sure JAVA_HOME is pointed to the correct path. 

To  deploy:
1. `gcloud auth login` and login to gcloud account
2. `gcloud config set project chenyu-zhang` to set project id
3. `gcloud app deploy src/main/webapp/WEB-INF/appengine-web.xml`
Reference: https://cloud.google.com/sdk/gcloud/reference/app/deploy

