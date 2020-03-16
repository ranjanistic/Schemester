# Schemester
Android application focussed on time table schedule, and other helpful side features.

## Contribution Guidelines
Create your own branch from master to contribute in any way. Directly commiting and pushing changes to branch-master isn't recommended and your commits might get reverted. It is safe to create own branch and then contribute.
## Configuration
The configuration (build configuration, proguard rules, etc.) files are not being checked in the repo, and therefore, may cause problems. Simply ask for configuration files, or check them in previous available releases (though outdated).
### Try NOT to:
```
- Edit instances of Firebase
...
    FirebaseUser user = FirebaseUser.getInstance()
    //instances and objects of Firebase in code to be managed by the owner of database project.
...
- Remove existing gradle dependencies
...
    implementation "xyz.abcd-1.2.3"
    //dependencies may be appended but not to be edited
...
```
## Project details

- Currently for devices running Android 5.0 or later (min API-21).
- Backend support via [Firebase](https://firebase.google.com/) Console.
- Dependencies mangaged by [Gradle](https://gradle.org/).
- Built on stable version of [Android Studio](https://developer.android.com/studio/).

### Top contributors
- **Icon design -** [Dark Mode Labs](https://github.com/darkmodelabs)
