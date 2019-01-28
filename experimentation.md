# LZ77 Encoder Experimentation

## 1. Runtime of the Encoder

Compare running time on the following things

1. Strings with structural redundancy vs Strings with little structural redundancy
    - Use a corpus for the strutucal redundant strings
2. Different parameter combinations
    - on structurally redundant strings
    - parameter ranges for W=[2^i for i \in [1, 16]], L = [2^i for i in [3, 10]]
    - find parameters mentioned in original paper?
3. Various Media Formats - these might contains other redundancies like frequency (do this one is 1/2 doesn't pad out like 2/3-1 page)
    - hyptothesis it would be wank on images, test on images, show it is wank on images (especially JPEG!)


## 2. Runtime of the Decoder

1. High Structural Redundancy vs Low Structural Redundacy
    - Do compressed files take longer to decode than not very compressed files.
2. Perhaps some paramater combinations really slowed down the decoding?
    - larger window sizes => largest prefix compression => larger decoding time?
    - look for files that were compressed really well in 3

## 3. Compression Ratio

Same 1, 2, 3 stuff. Althought "general purpose"
perhaps some media formats will favour better, i imagine it would be shit on images but nice on text
so could claim it's not terribly general purpose.

## 4. Comparison with other compression techniques

Compare with LZ78, Huffman, bzip2?
How well it works on different formats, runtime and compression ratio 