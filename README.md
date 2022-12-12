# OutOfMemoryError - reproduction steps
1. Start app with option: `-Xmx 256M`
2. Create 300 test entities `curl 'localhost:8080/create-new-entity?n=300'` 
3. Try to fetch the page 1 with size 5 using specification with join fetch `curl 'localhost:8080/list-entities?joinType=fetch&page=1&size=5'` => OutOfMemoryWillBeThrown
  * In the `QuerySqmImpl.java` in line 546:
      ```
      final List<R> list = resolveSelectQueryPlan().performList( executionContextToUse );
      ```
    all entities will be loaded. Not only the entities from the first page.


