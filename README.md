# EntityLocker

Reusable utility class that provides synchronization mechanism similar to row-level DB locking.

The class is supposed to be used by the components that are responsible for managing storage and caching of different type of entities in the application. EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities.

### References

- [Acquire a Lock by a Key in Java](https://www.baeldung.com/java-acquire-lock-by-key)
- [Java Lock](https://www.youtube.com/watch?v=MWlqrLiscjQ&ab_channel=JakobJenkov)
- [Java Concurrency Patterns and Features](https://github.com/LeonardoZ/java-concurrency-patterns)
- [Example EntityLocker](https://github.com/stden/EntityLocker)