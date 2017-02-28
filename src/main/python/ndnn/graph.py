from ndnn.node import Param, Input
from ndnn.init import Xavier

class Graph(object):
    def __init__(self, loss, update):
        self.params = []
        self.inputs = []
        self.out = None
        self.loss = loss
        self.update = update
        self.nodes = []
    
    def input(self):
        x = Input(self)
        self.inputs.append(x)
        return x
    
    def param(self):
        param = Param(self)
        self.params.append(param)
        return param
    
    def param_of(self, shape, init=Xavier()):
        param = Param(self)
        param.value = init.apply(shape)
        self.params.append(param)
        return param
        
    def output(self, node):
        self.out = node

    def expect(self, value):
        self.expect_val = value

    # Trace all nodes attached to the inputs
    def attach_node(self, node):
        self.nodes.append(node)
            
            
    def train(self):
        # Forward
        for node in self.nodes:
            node.forward()
        # Compute loss and set gradient
        loss_val = self.loss.loss(self.out.value, self.expect_val, False)
        self.out.grad = self.loss.grad
        # Backward
        for node in self.nodes[::-1]:
            node.backward()
            
        for p in self.params:
            self.update.update(p) 
        return loss_val, self.loss.accuracy()
    
    def test(self):
        for node in self.nodes:
            node.forward()
        # Compute loss if a loss function is available
        if self.loss is not None:
            loss_val = self.loss.loss(self.out.value, self.expect_val, True)
            return loss_val, self.loss.accuracy()
        else:
            return -1., -1
    
    def dump(self):
        return [p.value for p in self.params]
    
    def load(self, ps):
        for i, p in enumerate(ps):
            self.params[i].value = p
