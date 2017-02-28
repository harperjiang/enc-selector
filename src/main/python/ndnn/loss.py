import numpy as np

class Loss(object):
    def __init__(self):
        self.acc = None
        self.grad = None
    
    def loss(self, actual, expect, fortest):
        pass
    
    def grad(self):
        return self.grad
    
    def accuracy(self):
        return self.acc
    

class SquareLoss(Loss):
    def __init__(self):
        pass
    
    def loss(self, actual, expect, fortest):
        b = actual.shape()[0]

        if not fortest:
            # Compute Gradient
            self.grad = (actual - expect) * (1. / b)
        # Loss
        return np.power(actual - expect, 2).sum(axis=1).mean() / 2
        
clip = 1e-12

class LogLoss(Loss):
    def __init__(self):
        pass
    '''
    Actual is of shape [A, B, ..., M]
    Expect is of shape [A, B, ..., 1]
    Should return an gradient of shape [A, B,...,M]    
    ''' 
    def loss(self, actual, expect, fortest):
        shape_length = len(actual.shape)
        batch_size = actual.shape[shape_length - 2]
         
        xflat = actual.reshape(-1)
        iflat = expect.reshape(-1)
        outer_dim = len(iflat)
        inner_dim = len(xflat) / outer_dim
        idx = np.int32(np.array(range(outer_dim)) * inner_dim + iflat)
        fetch = xflat[idx].reshape(expect.shape)
        clipval = np.maximum(fetch, clip)
        
        if not fortest:
            # Compute Gradient
            slgrad = -np.ones_like(expect) / (clipval * batch_size)
            self.grad = np.zeros_like(actual)
            self.grad.reshape(-1)[idx] = slgrad
                
        # Accuracy for classification
    
        predict = np.argmax(actual, axis=-1)
        self.acc = np.equal(predict, expect).sum()
        
        return -np.log(clipval).mean()
