# inkt

A lightweight incremental query framework for Kotlin

Inspired by [Salsa](https://github.com/salsa-rs/salsa)

## Quick Start

Define a query with the `defineQuery` delegate function.

```kotlin
val fibonacci: QueryDefinition<Int, Int> by defineQuery { input ->
    print("$n, ")
    if ((0..1).contains(input)) {
        input
    } else {
        query(fibonacci, input - 1) + query(fibonacci, input - 2)
    }
}
```

To run queries, initialize a `QueryDatabase` (in this case, using `QueryDatabaseImpl`)

```kotlin
fun main() {
    val db = QueryDatabaseImpl()
    db.readTransaction {
        println("fibonacci(5) ${query(fibonacci, 5)}")
        println("fibonacci(6) ${query(fibonacci, 6)}")
        println("fibonacci(10) ${query(fibonacci, 10)}")
        println("fibonacci(10) ${query(fibonacci, 10)}")
    }
}
```

The output:
```
5, 4, 3, 2, 1, 0, fibonacci(5) 5
6, fibonacci(6) 8
10, 9, 8, 7, fibonacci(10) 55
fibonacci(10) 55
```

The first invocation, the cache is empty so values are computed for inputs 0 through 5.
During the second invocation, the cache is populated with the previous results so the only value computed is 6.
The third invocation leads to the computation of 7 through 10, using the cached value of 5 and 6.
Finally, the last invocation uses the already cached result for 10.

For some more complex examples of queries, including using input queries to seed the cache, see
[ShorthandExample.kt](src/main/kotlin/dev/dialector/inkt/query/example/ShorthandExample.kt) for an example of how to define queries and use the database.