import numpy as np
import tensorflow as tf
from encsel import graph
hidden_dim = 800

num_feature = 13
num_class = 5

train_writer = tf.summary.FileWriter("/home/harper/tftest")
g = graph.int_graph(hidden_dim)

with tf.Session() as sess:

    train_writer.add_graph(sess.graph)

    sess.close()
# for _ in range(1000):

    # train.run(feed_dict = {input:})