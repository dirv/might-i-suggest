# might-i-suggest

A ClojureScript library to add autosuggest capability to a textbox, given a local copy of the search data. The library performs no network requests.

## Example

```clojure
(def search-data [
  ["My first page title" "/my-site/first-page-title"]
  ["My second page title" "/my-site/second-page-title"]
  ["Something else" "/something-else"]])

; define your select-function
(defn select-fn [url]
  (.fetch js/window url))

(let [handler (make-autosuggest-on-change-handler-fn search-data select-fn)]
  (.addEventListener text-box handler))
```

 * Matches are searched for each word with three or more characters.
 * Results are ordered according to the order they appear in the search-data array.

# Running tests

    npm install
    clj -Atest

