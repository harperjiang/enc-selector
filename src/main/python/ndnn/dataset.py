import numpy as np


class Batch:
    def __init__(self, sz, data, label):
        self.size = sz
        self.data = data
        self.expect = label
    

class LSTMDataSet:
    def __init__(self, filename):
        self.vocab = {}
        self.vocab['@'] = 0
        self.idx = []
        self.idx.append('@')
        self.datas = []
        lines = open(filename, "rb").readlines()
    
        for line in lines:
            raw = '{' + line.decode('utf-8', errors='replace').strip().lower() + '}'
           
            chars = [char for char in raw]
            
            idx = np.ndarray((len(chars),), dtype=np.int32)
            for i, char in enumerate(chars):
                if char not in self.vocab:
                    self.vocab[char] = len(self.vocab)
                    self.idx.append(char)
                idx[i] = self.vocab[char]
            self.datas.append(idx)
        self.datas.sort(key=len)
    
    def num_char(self):
        return len(self.vocab)
    
    def translate_to_str(self, numarray):
        return ''.join([self.idx[n] for n in numarray])
    
    def translate_to_num(self, string):
        return [self.vocab[c] for c in [char for char in string]]
    
    def num_batch(self):
        return self.numbatch
    
    def batches(self, batch_size):
        batch_range = range(0, len(self.datas), batch_size)
        batches = [self.datas[idx:idx + batch_size] for idx in batch_range]
        self.numbatch = len(batches)
        perm = np.random.permutation(len(batches)).tolist()
        for p in perm:
            batch = batches[p]
            # Pad data
            length = [len(seq) for seq in batch]
            max_len = max(length)
            pad_data = []
            
            for i, item in enumerate(batch):
                item_pad = np.zeros(max_len, dtype=np.int32)
                item_pad[0:length[i]] = item
                pad_data.append(item_pad)
            
            pad_data = np.float64(pad_data).copy()
            yield Batch(len(batch), pad_data, None)
