from ndnn.node import Param, Input
from ndnn.init import Xavier

class Graph(object):
    def __init__(self, loss, update):
        self.params = []
        self.inputs = []
        self.out = None
        self.loss = loss
        self.update = update
    
    def input(self):
        x = Input()
        self.inputs.append(x)
        return x
    
    def param(self):
        param = Param()
        self.params.append(param)
        return param
    
    def param_of(self, shape, init=Xavier()):
        param = Param()
        param.value = init.apply(shape)
        self.params.append(param)
        return param
        
    def output(self, node):
        self.out = node

    def expect(self, value):
        self.expect_val = value

    def train(self):
        for x in self.inputs:
            x.forward(x)
        for p in self.params:
            p.forward(p)
        # Compute loss
        loss_val = self.loss.loss(self.out.value, self.expect_val, False)
        # Backward
        self.out.backward(self.out, self.loss.grad)
        for p in self.params:
            self.update.update(p) 
        return loss_val, self.loss.accuracy()
    
    def test(self):
        for x in self.inputs:
            x.forward(x)
        for p in self.params:
            p.forward(p)
        # Compute loss
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
