def do_sequences_match(source, index1, index2, length):
    "time optimised version for comparing 2 subsequences for equality"
    terminate = index1+length

    while index1 < terminate:
        if source[index1] != source[index2]:
            return False
        index1 += 1
        index2 += 1
    return True


def lz77_encode(source, window_size=65535, lookahead_window_size=255):
    def find_shared_sequence_in_window(source_i):
        if source_i == 0:
            return (source_i, 1)

        start_of_window = max(0, source_i-window_size)
        end_of_lookahead = min(len(source), source_i+lookahead_window_size+1)

        longest_seq_index, longest_seq_len = source_i, 1
        window_i, cur_seq_len = source_i-1, 1

        while start_of_window <= window_i:
            would_overflow_window = window_i + cur_seq_len > source_i

            if would_overflow_window:
                window_i -= 1
            elif do_sequences_match(source, window_i, source_i, cur_seq_len):
                longest_seq_index = window_i
                longest_seq_len = cur_seq_len

                cur_seq_len += 1  # going to look for a longer sequence

                looking_outside_lookahead_window = source_i + cur_seq_len > end_of_lookahead
                if looking_outside_lookahead_window:
                    break  # can
            else:
                window_i -= 1

        return (longest_seq_index, longest_seq_len)

    result = []
    byte_ptr = 0

    while byte_ptr < len(source):
        (ptr, seq_len) = find_shared_sequence_in_window(byte_ptr)

        if ptr == byte_ptr:
            result.append((0, 0, source[byte_ptr]))
            byte_ptr += 1
        else:
            how_far_back = byte_ptr - ptr
            next_byte = source[byte_ptr+seq_len] if byte_ptr + \
                seq_len < len(source) else None

            result.append((how_far_back, seq_len, next_byte))
            byte_ptr += seq_len+1

    return result


def lz77_decode(source):
    result, ptr = [], 0
    for (far_back, length, byte) in source:
        if length > 0:
            result.extend(result[ptr-far_back:ptr-far_back+length])
        if byte != None:
            result.append(byte)
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
    matches = string == lz77_decode_string(lz77_encode_string(string))
    if not matches:
        print("failed on string:", string)
        assert False


if __name__ == "__main__":
    import random
    import string

    while True:
        s2 = ''.join(random.choice(string.ascii_uppercase + string.digits)
                     for _ in range(10))
        check_string(s2)
