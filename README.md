# checkdoc

A Clojure library designed to check that doc-strings are up to par

## Usage

```Clojure
checkdoc.core> (checkdoc "This is a docstring." [] (ns-map 'checkdoc.core))
;; => ()

checkdoc.core> (checkdoc "This is a docstring" [] (ns-map 'checkdoc.core)))
;; => ("First line should end in punctuation")
```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
