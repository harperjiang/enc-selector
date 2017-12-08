import tensorflow as tf

def build_graph(num_feature,num_class,hidden_dim):
    input = tf.placeholder(tf.float32, shape=[None, num_feature])
    label = tf.placeholder(tf.float32, shape=[None, num_class])

    w1 = tf.Variable([num_feature, hidden_dim])
    b1 = tf.Variable([hidden_dim])

    w2 = tf.Variable([hidden_dim, num_class])
    b2 = tf.Variable([num_class])

    layer1 = tf.matmul(input, w1) + b1
    layer2 = tf.matmul(tf.tanh(layer1), w2) + b2

    loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=label, logits=layer2))

    return loss

def int_graph(hidden_dim):
    return build_graph(13,5,hidden_dim)

def str_graph(hidden_dim):
    return build_graph(10,4,hidden_dim)
