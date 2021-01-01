# Google Scholar Watcher
This is a small utility which helps to keep track of changes on a Google Scholar profile page.
It is particularly useful if you want to keep up with who cites somebody's work, since the alert function which Google Scholar offers is not always reliable.
Simply provide the tool with a user id of a profile page. It will parse the page and keep a record of its current state.
When you run it again at a later date, it will compare its most recent known state to the current one and list the differences.

Google Scholar Watcher is written in Kotlin and can be built using Gradle. It can be run using
```bash
gradlew run --args="<profile id>"
```
