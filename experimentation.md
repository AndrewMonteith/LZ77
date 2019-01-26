# LZ77 Encoder Experimentation

## 1. Runtime of the Encoder

Compare running time on the following things

1. Strings with structural redundancy vs Strings with little structural redundancy
    - Use a corpus for the strutucal redundant strings
2. Different parameter combinations
    - on structurally redundant strings
3. Various Media Formats - these might contains other redundancies like frequency (do this one is 1/2 doesn't pad out like 2/3-1 page)

Can we define some kind of notion of best running time? Maybe (Time Taken / Compression Ratio)

## 2. Runtime of the Decoder

1. High Structural Redundancy vs Low Structural Redundacy
    - Do higly compressed files take longer to decode than not very compressed files.
2. Perhaps some paramater combinations really slowed down the decoding?
    - larger window sizes => largest prefix compression => larger decoding time?

## 3. Compression Ratio

Same 1, 2, 3 stuff. Althought "general purpose"
perhaps some media formats will favour better, i imagine it would be shit on images but nice on text
so could claim it's not terribly general purpose.

# All This shit is wrong (below).

## WRONG - 1. Analysing the Encoder

Selected because the files were all roughly compressed.
the same amount of various different compression algorithms (Find a better quote?)

Experimentation (Look for compression rate, if textual bits per character and running time):
    Strings with structural redundancy vs Randomly Generated Strings ( / 1st million digits of PI)
    Different window sizes
        - Some kinda relationship between the two values?
    Talk about various media formats containing more / less structural redundancy

Might be good to mention (is for LZ78 but might impress max <3 ):
    https://semidoc.github.io/lagarde-catastrophe

## WRONG - 2. Analysing the Decoder

The decoding process is like really bloody simple.
There isn't really any part that could cause a bottleneck
Implementationally speaking perhaps if the thing was really compresed
and the decoder had to constantly look back on itself perhaps it could be slow?
We could definately precompute the size of the file though and avoid any problems
in that terms.

Might have to use slightly larger file sizes to see any difference?
Could talk about how slow it is for large files if we dont preallocate memory buffer
but this would require an O(n) pass to compute size of entire buffer
which for large files could get costly perhaps?

## WRONG - 3. Compression Ratio

Different kinds of files, files with different kind of structure

## 4. Comparison with other compression techniques

Compare with LZ78, Huffman, bzip2?
How well it works on different formats, runtime and compression ratio 