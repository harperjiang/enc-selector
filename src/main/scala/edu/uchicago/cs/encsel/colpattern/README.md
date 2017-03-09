# Find Common Patterns in Text

1. Count word appearance frequency
2. Set a cursor for each line (token stream).
3. If the token in a line is frequent enough, advance the cursor for that line, else stay still. This generates a categorical sub-column
4. If no sub-column is emitted in previous step, forward the cursor one step and generate an arbitrary sub-column / merge with preceding sub-column