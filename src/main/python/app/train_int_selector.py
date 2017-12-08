import numpy as np
import tensorflow as tf

hidden_dim = 800

num_feature = 13
num_class = 5

input = tf.placeholder(tf.float32, shape=[None, num_feature])
label = tf.placeholder(tf.float32, shape=[None, num_class])

W1 = tf.Variable([num_feature, hidden_dim])
b1 = tf.Variable([hidden_dim])

W2 = tf.Variable([hidden_dim, num_class])
b2 = tf.Variable([num_class])

layer1 = tf.matmul(input, W1) + b1

layer2 = tf.matmul(tf.tanh(layer1), W2) + b2

loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=label, logits=layer2))

train = tf.train.AdamOptimizer(learning_rate=0.01, beta1=0.9, beta2=0.999).minimize(loss)

# for _ in range(1000):

    # train.run(feed_dict = {input:})