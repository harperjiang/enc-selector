# Pattern Mining

Pattern mining targets at looking for a common pattern describing the dataset. Here without loss of generality, we assume the dataset is a collection of strings, each representing a data record.

## Pattern Generation
The process of generating patterns from a collection of given token sequence is comprised of the following steps:

1. **Tokenize** Using a lexer to convert each line to a list of token streams. The tokens can be either *PrimitiveToken*, which is a single word / number / symbol, or *GroupToken*, which is a list of tokens grouped by parenthetical symbols.
2. **Preprocessing** Apply preprocessing rules to the token sequences
3. **Generate** Create a sequence for each line of tokens. Create a Union containing each sequence. The union will be returned as generated pattern
4. **Refine** Apply refining rules repeatedly to the generated pattern until no more transformation can be applied
### Preprocessing
1. **Merging** Analyze the word frequency and combine words that **ALWAYS** appear together as a single word.
### Refine Rules

1. **Common Sequence** Find Common Sequence from the token streams. Here we define the common sequence to be a sub-list of tokens having the same type. The common sequences separate the list of token streams into sub-chunks.
2. **Frequent Similar Words** Sub-chunks do not contain common sequences and thus cannot be further separated by the steps above. To deal with this, we look for frequent similar words in those sub-chunks as separator. This further split sub-chunks into smaller sub-chunks.
3. **Merge Sequence** Remove unnecessary sequence. E.g., `Seq(Seq(a,b),Seq(x,y)) => Seq(a,b,x,y)`
4. **Remove Unnecessary Structure** 
   ~~~~
   Seq(a) => a
   Union(a) => a
   Seq(a,Empty) => Seq(a)
   Seq() => Empty
   Union() => Empty
   ~~~~
## Pattern Matching
Given a pattern and a sequence of tokens, Pattern matching looks for the correspondence between pattern elements and tokens. To achieve this, each pattern element is assigned a unique name. If a matching is found, a mapping between name and token is returned.
## Pattern Evolving 

Patterns generated from the steps above may be interpreted in many ways. For example, the following pattern
~~~~
Seq {
    Union {
    "ABC"
    "DDD"
    }
    "3234"
}
~~~~
can be described by either of the following regular expressions
* `(ABC|DDD)3234`
* `([A-Z]+)[0-9]+`

As both expressions accurately describe the given data samples, there's no way to prefer one description over another. Instead, we look at another set of validation samples, which are extracted independently from the original dataset.

The validator matches given pattern against the validation samples, and rewrite them when necessary. 


## Pattern Query Rewriting
