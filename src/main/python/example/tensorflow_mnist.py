import io

import numpy as np
import tensorflow as tf
import collections

hidden_dim = 200
input_size = 28 * 28
output_size = 10

train_data_file = "/home/harper/dataset/mnist/"
train_label_file = "/home/harper/dataset/mnist/"
test_data_file = "/home/harper/dataset/mnist/"
test_label_file = "/home/harper/dataset/mnist/"

Datasets = collections.namedtuple("Datasets", ['train','test'])

class Dataset(object):

    def __init__(self, data, label):
        pass

    def next_batch(self, batch_size):
        return None


def read_data(file):
    with io.open(file, 'r') as stream:
        magic = stream.read(4)

        num_record = stream.read(4)

        raw = stream.read(input_size * num_record)
        plain = np.frombuffer(raw, np.uint8)
        return plain.astype(np.float32) / 255


def read_label(file):
    with io.open(file, 'r') as stream:
        magic = stream.read(4)
        num_record = stream.read(4)
        raw = stream.read(num_record)
        return np.frombuffer(raw, np.uint8)


def read_datasets():

    train_data = read_data(train_data_file)
    train_label = read_label(train_label_file)
    test_data = read_data(test_data_file)
    test_label = read_data(test_label_file)


    return Datasets(train = Dataset(train_data,train_label), test = Dataset(test_data,test_label))


x = tf.placeholder(tf.float32, [None, input_size], name="x")
label = tf.placeholder(tf.float32, [None, 1], name="label")

with tf.name_scope("layer1"):
    w1 = tf.Variable(tf.truncated_normal([input_size, hidden_dim], stddev=0.1), name="w1")
    b1 = tf.Variable(tf.zeros([hidden_dim]), name="b1")
    layer1_out = tf.sigmoid(tf.matmul(x, w1) + b1, "l1o")

with tf.name_scope("layer2"):
    w2 = tf.Variable(tf.truncated_normal([hidden_dim, output_size], stddev=0.1), name="w2")
    b2 = tf.Variable(tf.zeros([output_size]), name="b2")
    layer2_out = tf.sigmoid(tf.matmul(layer1_out, w2) + b2, "l2o")
with tf.name_scope("loss"):
    cross_entropy = tf.nn.softmax_cross_entropy_with_logits(labels=label, logits=layer2_out, name="cross_entropy")

cross_entropy = tf.reduce_mean(cross_entropy)

with tf.name_scope("sgd"):
    train_step = tf.train.AdamOptimizer(1e-4).minimize(cross_entropy)

with tf.name_scope("accuracy"):
    correct_prediction = tf.equal(tf.argmax(layer2_out, 1), label)
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

train_writer = tf.summary.FileWriter("/home/harper/tftemp")
train_writer.add_graph(tf.get_default_graph())

mnist = read_datasets()

with tf.Session() as sess:
    sess.run(tf.global_variables_initializer())

    for i in range(2000):
        batch = mnist.train.next_batch(50)
        if i % 100 == 0:
            train_accuracy = accuracy.eval(feed_dict={x: batch[0], label: batch[1]})
            print('step %d, training accuracy %g' % (i, train_accuracy))
        train_step.run(feed_dict={x: batch[0], label: batch[1]})

    print(
        'test accuracy %g' % accuracy.eval(feed_dict={x: mnist.test.images, label: mnist.test.labels}))

train_writer.close()
