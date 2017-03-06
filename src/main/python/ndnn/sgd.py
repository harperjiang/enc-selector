import numpy as np

etaDefault = 0.001
etaDecay = 1

gradClip = -1

momentumKey = "momentum"
mAlpha = 0.9

rmspropKey = "rmsprop"
rmspropBeta = 0.9
rmsEpsilon = 1e-8

adammeanKey = "adammean"
adamvarKey = "adamvar"
adamAlpha = 0.9
adamBeta = 0.9


class UpdatePolicy(object):
    def __init__(self):
        pass

    def update(self, param):
        self.clip_grad(param)
        self.inner_update(param)

    def weight_decay(self):
        self.eta *= self.decay

    def clip_grad(self, param):
        if self.grad_clip > 0:
            norm = np.linalg.norm(param.grad)
            if norm >= self.grad_clip:
                param.grad *= self.grad_clip / norm
        

class SGD(UpdatePolicy):
    def __init__(self, eta=etaDefault, decay=etaDecay, gc=gradClip):
        self.eta = eta
        self.decay = decay
        self.grad_clip = gc
        
    def inner_update(self, param):
        param.value -= param.grad * self.eta
  

class Momentum(UpdatePolicy):

    def __init__(self, e=etaDefault, d=etaDecay, a=mAlpha, gc=gradClip):
        self.eta = e
        self.decay = d
        self.alpha = a
        self.grad_clip = gc

    def inner_update(self, param):
        if momentumKey in param.env:
            momentum = param.env[momentumKey]
        else:
            momentum = param.grad
        
        momentum = momentum * self.alpha + param.grad * (1 - self.alpha)
        param.value -= momentum * self.eta
        param.env[momentumKey] = momentum


class RMSProp(UpdatePolicy):
    def __init__(self, e=etaDefault, d=etaDecay, b=rmspropBeta, gc=gradClip):
        self.eta = e
        self.decay = d
        self.beta = b
        self.grad_clip = gc

    def inner_update(self, param):
        gradsqr = np.power(param.grad, 2)
        if rmspropKey in param.env:
            oldrms = param.env[rmspropKey]
        else:
            oldrms = gradsqr
        rms = oldrms * self.beta + gradsqr * (1 - self.beta)
        param.value -= param.grad * self.eta / (np.sqrt(rms) + rmsEpsilon) 
        param.env[rmspropKey] = rms
  

class Adam(UpdatePolicy):

    def __init__(self, e=etaDefault, d=etaDecay, a=adamAlpha, b=adamBeta, gc=gradClip):
        self.eta = e
        self.decay = d
        self.alpha = a
        self.beta = b
        self.grad_clip = gc
        
    def inner_update(self, param):
        if adammeanKey in param.env:
            oldmomen = param.env[adammeanKey]
        else:
            oldmomen = param.grad
        momentum = oldmomen * self.alpha + param.grad * (1 - self.alpha)

        gradsqr = np.power(param.grad, 2)
        if adamvarKey in param.env:
            oldrms = param.env[adamvarKey]
        else:
            oldrms = gradsqr
        rms = oldrms * self.beta + gradsqr * (1 - self.beta)
        param.value -= momentum * self.eta / (np.sqrt(rms) + rmsEpsilon)

        param.env[adammeanKey] = momentum
        param.env[adamvarKey] = rms

'''
Dense-Sparse-Dense Training
See https://arxiv.org/pdf/1607.04381.pdf

The training process contains 3 phases
* phase 1: normal training
* phase 2: apply a watermark to weight and update only those above watermarks
* phase 3: remove the watermark and train normally
'''
maskKey = "weight.mask"
threshold = 0.001


class DSD(UpdatePolicy):
    def __init__(self, childpolicy, phase1, phase2):
        self.child = childpolicy
        self.phase1 = phase1
        self.phase2 = phase2
        self.current_epoch = 0
        
    def inner_update(self, param):
        self.child.inner_update(param)
        if self.phase1 <= self.current_epoch < self.phase2 :
            # Apply mask
            if maskKey not in param.env: 
                mask = np.greater(np.abs(param.value), threshold)
                param.env[maskKey] = mask
            else:
                mask = param.env[maskKey]
            param.value *= mask
        
    def clip_grad(self, param):
        self.child.clip_grad(param)
        
    def weight_decay(self):
        self.child.weight_decay()
        self.current_epoch += 1