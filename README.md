jsontreefilter
==============

Itch: You've got some fairly complex JSON and would like to get a copy with only certain branches/fields.

Scratch: Deserialise into a Jackson tree model object, then use this little library to create a partial copy.

See test cases for usage.

Limitations
-----------

* This won't allow you to pick sub-trees from anywhere in the tree (that's what Jackson's JSON Pointer support does very nicely). It just allows you to create a copy of the tree with specified branches (starting at the root object, i.e. preserving the original "meta-structure") and others left out.
* Whitelisting only (you can explicitly select what you like or use a wildcard, but you can't say "everything but this branch").
* Only moderately tested so far, and let's not talk about nice documentation.
