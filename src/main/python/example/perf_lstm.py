import numpy as np
from time import time

hidden_dim = 200
batch_size = 50
num_char = 100

stime = time()

def xavier(shape):
    sq = np.sqrt(3.0 / np.prod(shape[:-1]))
    return np.random.uniform(-sq, sq, shape)

C2V = xavier([num_char, hidden_dim])
w1 = xavier([2 * hidden_dim, hidden_dim])
b1 = np.zeros([hidden_dim])
w2 = xavier([2 * hidden_dim, hidden_dim])
b2 = np.zeros([hidden_dim])
w3 = xavier([2 * hidden_dim, hidden_dim])
b3 = np.zeros([hidden_dim])
w4 = xavier([2 * hidden_dim, hidden_dim])
b4 = np.zeros([hidden_dim])
V2C = xavier([hidden_dim, num_char])

def sigmoid(input):
    return 1. / (1. + np.exp(-input))
def tanh(input):
    x_exp = np.exp(input)
    x_neg_exp = np.exp(-input)
    return (x_exp - x_neg_exp) / (x_exp + x_neg_exp)
# Generate random batch
length = 500
batch = np.random.randint(num_char, size = (batch_size, length))

h0 = np.zeros([batch_size, hidden_dim])
c0 = np.zeros([batch_size, hidden_dim])

for i in range(batch.shape[1]):
    item = batch[:, i]
    embed = C2V[np.int32(item),:]
    concat = np.concatenate((embed, h0), axis = 1)

    fgate = sigmoid(np.matmul(concat, w1) + b1)
    igate = sigmoid(np.matmul(concat, w2) + b2)
t = time() - stime
print(t)
