# checkdoc

A Clojure library designed to check that doc-strings are up to par

## Usage

```Clojure
checkdoc.core> (checkdoc "This is a docstring." [])
;; => ()

checkdoc.core> (checkdoc "This is a docstring" [])
;; => ("First line should end in punctuation")
```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
