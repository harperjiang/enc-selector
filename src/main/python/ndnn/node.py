import numpy as np

dt = np.float64 

def diff(x, y):
    xs = np.array(x.shape)
    ys = np.array(y.shape)
    pad = len(xs) - len(ys)
    if pad > 0:
        ys = np.pad(ys, [[pad, 0]], 'constant')
    elif pad < 0:
        xs = np.pad(xs, [[-pad, 0]], 'constant')
    os = np.maximum(xs, ys)
    xred = tuple([idx for idx in np.where(xs < os)][0])
    yred = tuple([idx for idx in np.where(ys < os)][0])
    return xred, yred

class Node(object):
    
    def __init__(self, inputs):
        self.inputs = inputs
        self.outputs = []
        self.readyInputs = set()
        self.readyOutputs = set()
        for x in inputs:
            x.outputs.append(self)
    
    def forward(self, src):
        if src in self.inputs:
            self.readyInputs.add(src)
        
        if len(self.readyInputs) == len(self.inputs):
            self.value = self.compute
            self.readyInputs.clear()
            self.grad = dt(0)
            for o in self.outputs:
                o.forward(self)
    
    def backward(self, src, grad):
        if self == src:
            self.grad = grad
        if src in self.outputs:
            self.readyOutputs.add(src)
            self.grad += grad
        if len(self.outputs) == len(self.readyOutputs):
            for node, grad in self.updateGrad().items():
                node.backward(self, grad)
    
class Input(Node):
    def __init__(self, x=None):
        if x is not None:
            Node.__init__(self, [x])
        else:
            Node.__init__(self, [])
        self.x = x
        
    def compute(self):
        if self.x is not None:
            return self.x.value
        else:
            self.value
    
    def updateGrad(self):
        if self.x is not None:
            return {self.x : self.grad}
        else:
            return {}
    
class Param(Input):
    def __init__(self):
        Input.__init__(self, None)
        self.env = {}
    
class Add(Node):
    def __init__(self, l, r):
        super(Add, self).__init__([l, r])
        self.left = l
        self.right = r
        
    def compute(self):
        return self.left.value + self.right.value
    
    def updateGrad(self):
        xdiff, ydiff = diff(self.left.value, self.right.value)
        
        lgrad = np.reshape(np.sum(self.grad, axis=xdiff, keepdims=True),
                self.left.value.shape)

        rgrad = np.reshape(np.sum(self.grad, axis=ydiff, keepdims=True),
                self.right.value.shape)
        return {self.left: lgrad, self.right:rgrad }

class Mul(Node):
    def __init__(self, l, r):
        super(Mul, self).__init__([l, r])
        self.left = l
        self.right = r
        
    def compute(self):
        return self.left.value * self.right.value
    
    def updateGrad(self):
        xdiff, ydiff = diff(self.left.value, self.right.value)
        lgrad = np.reshape(np.sum(self.grad * self.right.value, axis=xdiff, keepdims=True),
                self.left.value.shape)
        rgrad = np.reshape(np.sum(self.grad * self.left.value, axis=ydiff, keepdims=True),
                self.right.value.shape)
        return {self.left:lgrad, self.right:rgrad }

class Dot(Node):  # Matrix multiply (fully-connected layer)
    def __init__(self, x, y):
        super(Dot, self).__init__([x, y])
        self.x = x
        self.y = y

    def compute(self):
        return np.matmul(self.x.value, self.y.value)
    def updateGrad(self):
        return {self.x: np.matmul(self.y.value, self.grad.T).T,
                self.y: np.matmul(self.x.value.T, self.grad)}

class Sigmoid(Node):
    def __init__(self, x):
        super(Sigmoid, self).__init__([x])
        self.x = x

    def compute(self):
        return 1. / (1. + np.exp(-self.x.value))

    def updateGrad(self):
        return {self.x: self.grad * self.value * (1. - self.value)}


class Tanh(Node):
    def __init__(self, x):
        super(Tanh, self).__init__([x])
        self.input = x

    def compute(self):
        x_exp = np.exp(self.input.value)
        x_neg_exp = np.exp(-self.input.value)

        return (x_exp - x_neg_exp) / (x_exp + x_neg_exp)

    def updateGrad(self):
        return {self.input: self.grad * (1 - self.value * self.value)}

class ReLU(Node):
    def __init__(self, x):
        super(ReLU, self).__init__([x])
        self.x = x

    def compute(self):
        return np.maximum(self.x.value, 0)

    def updateGrad(self):
        return {self.x: self.grad * (self.value > 0)}

class LeakyReLU(Node):
    def __init__(self, x):
        super(LeakyReLU, self).__init__([x])
        self.x = x

    def compute(self):
        return np.maximum(self.x.value, 0.01 * self.x.value)

    def updateGrad(self):
        return {self.x: self.grad * np.maximum(0.01, self.value > 0)}

class SoftMax(Node):
    def __init__(self, x):
        super(SoftMax, self).__init__([x])
        self.x = x

    def compute(self):
        lmax = np.max(self.x.value, axis=-1, keepdims=True)
        ex = np.exp(self.x.value - lmax)
        return ex / np.sum(ex, axis=-1, keepdims=True)

    def updateGrad(self):
        gvdot = np.matmul(self.grad[..., np.newaxis, :], self.value[..., np.newaxis]).squeeze(-1)
        return {self.x: self.value * (self.grad - gvdot)}

class Concat(Node):

    def __init__(self, x, y):
        super(Concat, self).__init__([x, y])
        self.x = x 
        self.y = y
       
    def compute(self):
        return np.concatenate((self.x.value, self.y.value), axis=1)
        
    def updateGrad(self):     
        
        dim_x = self.x.value.shape[1]
        dim_y = self.y.value.shape[1]
        
        return {self.x: self.grad[:, 0:dim_x],
                self.y:self.grad[:, dim_x:dim_x + dim_y]}

class Collect(Node):
    def __init__(self, nodes):
        super(Collect, self).__init__(nodes)
        self.nodes = nodes
    
    def compute(self):
        withNewAxis = [n.value.get[np.newaxis, :] for n in self.nodes]
        return np.concatenate(withNewAxis, 0)
    
    def updateGrad(self):
        gradmap = {}
        idx = 0
        for n in self.nodes:
            gradmap[n] = self.grad[idx, :]
            idx += 1
        return gradmap

class Embed(Node):
    def __init__(self, idx, w2v):
        super(Embed, self).__init__([w2v])
        self.idx = idx
        self.w2v = w2v

    def compute(self):
        return self.w2v.value[np.int32(self.idx.value), :]
    def updateGrad(self):
        grad = np.zeros_like(self.w2v.value)
        grad[np.int32(self.idx.value), :] += self.grad
        return {self.w2v, grad}
            
class ArgMax(Node):
    def __init__(self, x):
        super(ArgMax, self).__init__([x])
        self.x = x 
  
    def compute(self):
        return np.argmax(self.x.value)

    def backward(self):     
        pass
