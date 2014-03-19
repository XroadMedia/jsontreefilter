jsontreefilter
==============

Itch: You've got some fairly complex JSON and would like to get a copy with only certain branches/fields.

Scratch: Read into a Jackson tree model object, then use this little library to create a partial copy.

Example
-------

Given the JSON

```json
{
    "a": "b",
    "b": {
        "c": ["one", "two"],
        "d": true,
        "e": {
            "f": {
                "name": "Herbert",
                "nickname": "Bertie"
            },
            "p": {
                "name": "Robert",
                "nickname": "Bob"
            }
        }
    },
    "x": "y"
}
```

in memory as a JsonNode object called tree, and the following lines

```java
Spec s = Spec.spec("a", "b.c", "b.e.*.nickname");
JsonNode copy = FilteredTreeCopier.copyTree(tree, s.toNodes());
```

the copy object will contain this JSON:

```json
{
    "a": "b",
    "b": {
        "c": ["one", "two"],
        "e": {
            "f": {
                "nickname": "Bertie"
            },
            "p": {
                "nickname": "Bob"
            }
        }
    }
}
```

Limitations
-----------

* This won't allow you to pick sub-trees from anywhere in the tree (that's what Jackson's JSON Pointer support does very nicely). It just allows you to create a copy of the tree with specified branches (starting at the root object, i.e. preserving the original "meta-structure") and others left out.
* This works only on object fields (names). You can't select specific entries from a JSON array, for instance.
* Whitelisting only (you can explicitly select what you like or use a wildcard, but you can't say "everything but this branch").
* The textual specification doesn't handle escaping yet. Mainly, this means field names with periods and asterisks won't work.
* Only moderately tested so far, and let's not talk about nice documentation.
