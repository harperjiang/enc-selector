import numpy as np
import os
import pickle
from time import time
from ndnn.dataset import LSTMDataSet
from ndnn.rnn import LSTMTrainGraph, LSTMPredictGraph


trainds = LSTMDataSet('data/ptb.train.txt')
validds = LSTMDataSet('data/ptb.valid.txt')
testds = LSTMDataSet('data/ptb.test.txt')


hidden_dim = 200
batch_size = 50
parameters = []
model = 'model_LSTM.pkl'
eta = 0.5
decay = 0.9

np.random.seed(0)

param_store = []
# Load model if exists
if os.path.exists(model):
    with open(model, 'rb') as f:
        p_value = pickle.load(f)
        for p in p_value:
            param_store.append(p)
                    

def Predict(max_step, prefix):

    predictGraph = LSTMPredictGraph(trainds.num_char(), hidden_dim)
    if len(param_store) > 0:
        predictGraph.load(param_store)

    predictGraph.build(prefix, max_step)

    predictGraph.test()

    idx = [pred.value for pred in predictGraph.predicts]
    stop_idx = trainds.translate_to_num('}')[0]

    if stop_idx in idx:
        return idx[0:idx.index(stop_idx) + 1]
    else:
        return idx

def Eval(ds):
    total_num = 0
    total_acc = 0
    total_loss = 0
    for batch in ds.batches(batch_size):
        graph = LSTMTrainGraph(trainds.num_char(), hidden_dim)
        if len(param_store) > 0:
            graph.load(param_store)
        graph.build(batch)
        loss, acc = graph.test()
        total_num += np.product(batch.data.shape)
        total_acc += acc
        total_loss += loss
    return total_loss / ds.num_batch() , total_acc / total_num


############################################### training loop #####################################################

epoch = 10

# initial Perplexity and loss
loss, acc = Eval(validds)
print("Initial: Perplexity: - Avg loss = %0.5f, accuracy %0.5f" % (loss, acc))
best_loss = loss
prefix = 'the agreements bring'
generation = Predict(400, trainds.translate_to_num(prefix))
print("Initial generated sentence ")
print (trainds.translate_to_str(generation))

for ep in range(epoch):
 
    stime = time()
 
    for batch in trainds.batches(batch_size):
        graph = LSTMTrainGraph(trainds.num_char(), hidden_dim)
        if len(param_store) > 0:
            graph.load(param_store)
        graph.build(batch)
        graph.train()
        param_store = graph.dump()
         
    graph.update.weight_decay()
    duration = (time() - stime) / 60.
     
    param_store = graph.dump()
     
    loss, acc = Eval(validds)
    print("Epoch %d: Perplexity: - Avg loss = %0.5f, accuracy %0.5f [%.3f mins]" % (ep, loss, acc, duration))
 
    # generate some text given the prefix and trained model
    prefix = 'the agreements bring'
    generation = Predict(400, trainds.translate_to_num(prefix))
    print("Epoch %d: generated sentence " % ep)
    print (trainds.translate_to_str(generation))
