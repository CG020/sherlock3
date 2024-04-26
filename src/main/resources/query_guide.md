

# Query Parts:

## Setting Up the Query:

main sets up the searcher with our custom coeffiecients for BM25 Similarity measure.

### k and b BM25 Similarity
Our k value is about average for saturation of term frequency. Our b value is low, meaning doc length affects scores less.

The query is Tokenized using our custom Tokenizer.

## Layers of Query

### Boolean Query - q

The Boolean Query allows for boolean combination of other query types - this is how we are able to use several different queries at same time.

Note: SHOULD gives the option to have what the query is querying for and MUST requires it, our query uses SHOULD for less exclusivity

### MultiField Query

Because our Index has several fields (summary, categories, bodyText, etc) we want to search for terms in the query across all of them.

MultiField Query lets us query for the terms across all fields with some boosted scores, so if the query word is "newspaper" and "newspaper" is in categories and bodyText but maybe not summary, it will still match that document because newspaper is in at least one of the fields. 

MultiField Query also can let us boost the scores if the word is found in a certain category. Categories is boosted higher than summary, so if newspaper appears in categories, that doc gets a higher score.

### TermQuery

TermQuery boosts the score for a certain term or phrase. We use this to boost scores of pages that have the category phrase (given from questions.txt) in their content.

### Phrase Querry

Phrase Queries search for pages that have phrases of words in the pages. In our use, it takes every adjacent pair of words across the entire query and searches for those phrases individually, if a page gets enough of those in sequence, in it will get a higher score.

### Revisit Boolean Query

All of the above queries are added to the original Boolean Query q as clauses. (All set to SHOULD not MUST)

## Getting the Results

The returned pages are checked for duplicates and returned.
