(ns checkdoc.core
  (:require [clojure.string :as str]))

;; don't care about punctuation inside backtics, see
;; https://stackoverflow.com/a/1757107/9422
(def punctuation-re #"\.!?(?=([^`]*`[^`]*`)*[^`]*$)")

(defn all-sentences-end-with-space [{:keys [doc-string]}]
  (let [[_ & sentences] (str/split doc-string punctuation-re)]
    (when (->> sentences
               (map #(re-find #"^\s" %))
               (filter (comp not identity))
               seq)
      ["All sentences should end with a space-character"])))

(defn ends-with-period [{:keys [doc-string]}]
  (when-not (re-find #"\.$" doc-string)
    ["Doc-string should end with `.`"]))

(defn no-wider-than-limit [limit {:keys [doc-string]}]
  (let [lines (str/split doc-string #"\n")
        long-lines (filter #(< limit (count %)) lines)]
    (when (seq long-lines)
      [(str "Doc-strings should not contain lines longer than " limit " characters")])))

(defn first-line-complete-sentence
  "Checks if the first line of a doc string is a complete sentence."
  [{:keys [doc-string]}]
  (let [[first-line] (str/split doc-string #"\n")
        [first-chr & _] first-line
        last-chr (last first-line)]
    (filter identity [(when-not (#{\.\!\?} last-chr)
                        "First line should end in punctuation")
                      (when-not (Character/isUpperCase first-chr)
                        "First line should be capitalized")])))

(defn no-indentation [{:keys [doc-string]}]
  (let [lines (str/split doc-string #"\n")
        lines-starting-with-space (filter #(re-find #"^\s+" %) lines)]
    (when (seq lines-starting-with-space)
      ["Don't indent doc-strings"])))

(defn no-whitespace-at-start-or-end [{:keys [doc-string]}]
  (when (or (= \ (first doc-string))
            (= \ (last doc-string)))
    ["Don't start or end doc-string with whitespace"]))

(defn all-args-should-be-documented [{:keys [doc-string args]}]
  (keep #(when-not (re-find (re-pattern (str "`" % "`")) doc-string)
           (str % " is not mentioned or not quoted in the doc-string")) args))

(defn var->str [v]
  (let [vs (str v)]
    (cond (str/starts-with? vs "#'") (.substring vs 2)
          (str/starts-with? vs "class") (.substring vs 6)
          (str/starts-with? vs "interface") (.substring vs 10)
          :else vs)))

(defn all-symbols-should-be-quoted [{:keys [doc-string args ns-map]}]
  (let [doc-symbols (->> ns-map
                         vals
                         (map var->str)
                         (keep #(re-find (re-pattern (str "\\b\\Q" % "\\E\\b")) doc-string)))]
    (def dc doc-symbols)
    (keep #(when-not (re-find (re-pattern (str "`" % "`")) doc-string)
           (str % " should be quoted with ``")) doc-symbols)))

(def rules [no-whitespace-at-start-or-end
            all-sentences-end-with-space
            no-indentation
            first-line-complete-sentence
            all-args-should-be-documented
            all-symbols-should-be-quoted
            (partial no-wider-than-limit 60)])

(defn checkdoc
  "Checks that `doc-string` and `args` adhear to the standards.
`env` is a map that at least contains symbols, which is
a sequence of known symbols for the var containing the docstring.
See `rules` for what rules are checked."
  [doc-string args ns-map]
  (->> rules
       (mapcat #(% {:doc-string doc-string :args args :ns-map ns-map}))
       (filter identity)))

(comment
  (checkdoc "Checks that `doc-string` and `args` adhear to the standards.
`env` is a map clojure.core/identity clojure.lang.Compiler that at least contains symbols, which
is a sequence of known symbols for the var containing
the docstring. See `rules` for what rules are checked." ["doc-string" "args" "env"] (ns-map 'checkdoc.core))
  )
