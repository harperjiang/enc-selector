My task is to look for new architecture of LSTM that can improve its performance in text prediction. For this purpose, I need to hand written new LSTM structures and I have a framework in numpy, of which I would like to migrate to Nd4j. 

I have the following hyperparamters:
* numChar: Number of distinct characters
* hiddenDim: The size of hidden dimension in my LSTM cell
* batchSize: batch size

I have the following parameters:
* c2v:  a character embedding matrix, shape [numChar, hiddenDim]
* v2c:  a mapping matrix from hidden dimension to char, shape [hiddenDim, numChar]
* w1-w4: network, shape[2\*hiddenDim, hiddenDim]
* b1-b4: bias,  shape[hiddenDim]
* h0: Init hidden state, shape [batchSize, hiddenDim]
* c0: Init cell state, shape [batchSize, hiddenDim]

My simple LSTM cell follows the following steps:
* input is a vector of shape [batchSize, 1], each element is an index between 0 and numChar
* fetch the embedding from c2v using the index, get a matrix **embedded** of shape [batchSize, hiddenDim]
* concatenate **embedded** with h0, get an matrix **concat** of shape [batchSize, 2\* hiddenDim]
* create a forget_gate = sigmoid(concat * w1 + b1)
* create an info_gate = tanh(concat * w2 + b2) * sigmoid(concat * w3 + b3)
* update cell state: c_i+1 = c_i * forget_gate + info_gate
* update hidden state: h_i+1 = tanh(c_i+1) * sigmoid(concat * w4 + b4) 

The Nd4j implementation is much slower than numpy. In the attached source code, I showed the cumulative result of first 4 steps of computation
The time is in secs. Besides the weird concat operation, other nd4j operations are all at least 5-6 times slower than their numpy counterpart.

|       | Embed | Concat | F Gate | I Gate |
|------:|-------|--------|--------|--------|
| Numpy | 0.01  | 0.018  | 0.332  | 0.46   |
| Nd4j  | 0.409 | 6.056  | 6.846  | 7.449  |

I am running numpy 1.11.2 compiled with Intel MKL and Openblas on Python 3.5.2, Ubuntu 16.10.
Nd4j version is 0.7.2