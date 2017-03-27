# Pattern Mining

Pattern mining targets at looking for a common pattern describing the dataset. Here without loss of generality, we assume the dataset is a collection of strings, each representing a data record.

The process is comprised of the following steps:

1. **Tokenize** Using a lexer to convert each line to a list of token streams. The tokens can be either *PrimitiveToken*, which is a single word / number / symbol, or *GroupToken*, which is a list of tokens grouped by parenthetical symbols.
2. **Merging** Analyze the word frequency and combine words that **ALWAYS** appear together as a single word.
3. **Common Sequence** Find Common Sequence from the token streams. Here we define the common sequence to be a sub-list of tokens having the same type. The common sequences separate the list of token streams into sub-chunks.
4. **Frequent Similar Words** Sub-chunks do not contain common sequences and thus cannot be further separated by the steps above. To deal with this, we look for frequent similar words in those sub-chunks as separator. This further split sub-chunks into smaller sub-chunks.
5. **Determine Type** For numeric and symbolic tokens, the types is basically themselves. For word tokens, we have two choices: either declare it as a enum token with predefined values, or an arbitrary word token. This choice is done by looking at the frequence of words in that location.