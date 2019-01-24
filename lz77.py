def do_sequences_match(source, index1, index2, length):
    "time optimised version for comparing 2 subsequences for equality"
    terminate = index1+length

    while index1 < terminate:
        if source[index1] != source[index2]:
            return False
        index1 += 1
        index2 += 1
    return True


def find_longest_prefix_in_window(symbols, symbol_i, 
        window_size, lookahead_window_size):

    if symbol_i == 0:
        return (symbol_i, 1)

    start_of_window = max(0, symbol_i-window_size)
    end_of_lookahead = min(len(symbols), symbol_i+lookahead_window_size+1)

    longest_prefix_i, longest_prefix_len = symbol_i, 1
    window_i, cur_prefix_len = symbol_i-1, 1

    while start_of_window <= window_i:
        looking_outside_window = window_i + cur_prefix_len > symbol_i

        if looking_outside_window:
            window_i -= 1
        elif do_sequences_match(symbols, window_i, symbol_i, cur_prefix_len):
            longest_prefix_i = window_i
            longest_prefix_len = cur_prefix_len

            cur_prefix_len += 1  # going to look for a longer prefix

            looking_outside_lookahead_window = symbol_i + cur_prefix_len > end_of_lookahead
            if looking_outside_lookahead_window:
                break  # can
        else:
            window_i -= 1

    return (longest_prefix_i, longest_prefix_len)

def lz77_encode(symbols, window_size=65535, lookahead_window_size=255):
    result = []
    symbol_i = 0

    while symbol_i < len(symbols):
        (prefix_i, prefix_len) = find_longest_prefix_in_window(
            symbols, symbol_i, window_size, lookahead_window_size)

        if prefix_i == symbol_i:
            result.append((0, 0, symbols[symbol_i]))
            symbol_i += 1
        else:
            how_far_back = symbol_i - prefix_i
            next_symbol = symbols[symbol_i+prefix_len] if \
                symbol_i + prefix_len < len(symbols) else None

            result.append((how_far_back, prefix_len, next_symbol))
            symbol_i += prefix_len+1

    return result


def lz77_decode(symbols):
    "Decodes a LZ77 encoded sequence of symbols"
    result, ptr = [], 0
    for (far_back, length, symbol) in symbols:
        if length > 0:
            result.extend(result[ptr-far_back:ptr-far_back+length])
        if symbol != None:
            result.append(symbol)
        ptr += length+1

    return result


def string_to_bytes(str):
    "Converts a string into an array of bytes."
    b = bytearray()
    b.extend(map(ord, str))
    return b


def lz77_encode_string(string, window_size=65535, lookahead_window_size=255):
    "Encodes string using LZ77 coding."
    return lz77_encode(
        string_to_bytes(string), window_size, lookahead_window_size)


def lz77_decode_string(source):
    "Decodes LZ77 encoded string."
    return ''.join(chr(x) for x in lz77_decode(source))


def check_string(string):
    "asserts that string can be encoded and decoded using LZ77 coding"
    matches = string == lz77_decode_string(lz77_encode_string(string))
    if not matches:
        print("failed on string:", string)
        assert False
