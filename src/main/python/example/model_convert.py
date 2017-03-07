import numpy as np
from ndnn.store import ParamStore


store = ParamStore('model_LSTM.pkl')

params = store.load()

np.savetxt("c2v.npy", params[0], delimiter='\t')
np.savetxt('wf.npy', params[1], delimiter='\t')
np.savetxt('bf.npy', params[2], delimiter='\t')
np.savetxt('wi.npy', params[3], delimiter='\t')
np.savetxt('bi.npy', params[4], delimiter='\t')
np.savetxt('wc.npy', params[5], delimiter='\t')
np.savetxt('bc.npy', params[6], delimiter='\t')
np.savetxt('wo.npy', params[7], delimiter='\t')
np.savetxt('bo.npy', params[8], delimiter='\t')
np.savetxt('v.npy', params[9], delimiter='\t')
"""
self.C2V = self.param_of((num_char, hidden_dim))
self.wf = self.param_of((2 * hidden_dim, hidden_dim))
self.bf = self.param_of((hidden_dim), Zero())
self.wi = self.param_of((2 * hidden_dim, hidden_dim))
self.bi = self.param_of((hidden_dim), Zero())
self.wc = self.param_of((2 * hidden_dim, hidden_dim))
self.bc = self.param_of((hidden_dim), Zero())
self.wo = self.param_of((2 * hidden_dim, hidden_dim))
self.bo = self.param_of((hidden_dim), Zero())
self.V = self.param_of((hidden_dim, num_char))
"""