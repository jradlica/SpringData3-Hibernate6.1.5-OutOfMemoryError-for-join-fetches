# Context
The app presents the potential hibernate bug (or maybe it's a desired behaviour which was not obvious to me). (I don't know it yet)

When the pageable query contains collection join fetches the all found entities are loaded to memory not just entities for the requested page - it could lead to OutOfMemoryError in some cases. 

# OutOfMemoryError - reproduction steps
1. Start app with option: `-Xmx 256M`
2. Create 300 test entities in several requests `for i in {1..5}; do curl 'localhost:8080/create-new-entity?n=50'; done` 
3. Try to fetch the page 1 with size 5 using specification with join fetch `curl 'localhost:8080/list-entities?joinType=fetch&page=1&size=5'` => OutOfMemoryWillBeThrown
  * In the `org.hibernate.query.sqm.internal.QuerySqmImpl.java` in line 546:
      ```
      final List<R> list = resolveSelectQueryPlan().performList( executionContextToUse ); 
      ```
    all entities are loaded. Not only the 5 entities on the first page.
* For the regular joins: `curl 'localhost:8080/list-entities?joinType=regular&page=1&size=5' the problem is not occur.


