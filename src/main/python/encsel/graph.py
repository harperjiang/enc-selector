import tensorflow as tf


def build_graph(num_feature, num_class, hidden_dim):
    with tf.name_scope("graph"):
        x = tf.placeholder(tf.float32, shape=[None, num_feature], name="x")
        y = tf.placeholder(tf.float32, shape=[None, num_class], name="y")
        with tf.name_scope("layer1"):
            w1 = tf.Variable(tf.truncated_normal([num_feature,hidden_dim],stddev=0.1), name="w1")
            b1 = tf.Variable(tf.zeros([hidden_dim]), name="b1")
            layer1 = tf.tanh(tf.matmul(x, w1) + b1,name="tanh")
        with tf.name_scope("layer2"):
            w2 = tf.Variable(tf.truncated_normal([hidden_dim, num_class],stddev=0.1), name="w2")
            b2 = tf.Variable(tf.zeros([num_class]), name="b2")

            layer2 = tf.matmul(layer1, w2) + b2

        loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=y, logits=layer2),name="loss")

        return loss


def int_graph(hidden_dim):
    return build_graph(13, 5, hidden_dim)


def str_graph(hidden_dim):
    return build_graph(10, 4, hidden_dim)
