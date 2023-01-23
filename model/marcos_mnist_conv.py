# -*- coding: utf-8 -*-
"""Marcos MNIST Conv.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1o9WGJJVSPOTkFH4-uUbRWXSvGFr5DCVT
"""

# Commented out IPython magic to ensure Python compatibility.
# %matplotlib inline

import numpy as np
from random import shuffle, seed
seed(42)
import matplotlib.pyplot as plt
plt.rcParams['figure.figsize'] = (10,10)

from keras.datasets import mnist
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Input, Conv2D, MaxPooling2D
from keras.utils import np_utils
from keras import optimizers

from keras.layers.core import Flatten

mnist_classes = 10

# the data, shuffled and split between train and test sets
(X_train, y_train), (X_test, y_test) = mnist.load_data()
print("X_train original shape", X_train.shape)
print("y_train original shape", y_train.shape)

print("X_test original shape", X_test.shape)
print("y_test original shape", y_test.shape)

# 80% para treino e 20% para validação
nData = X_train.shape[0]
nTrain = int(nData * 0.8)

randomIdx = list(range(nData))   #randomly select indexes
shuffle(randomIdx)
trainIdx = randomIdx[:nTrain]
valIdx = randomIdx[nTrain:]

# Split the data
X_val, y_val = X_train[valIdx], y_train[valIdx]
X_train, y_train = X_train[trainIdx], y_train[trainIdx]

print("X_train shape", X_train.shape)
print("y_train shape", y_train.shape)

print("X_val shape", X_val.shape)
print("y_val shape", y_val.shape)

img_rows, img_cols = 28, 28

#The first dimension refers to the number of images
X_train = X_train.reshape(X_train.shape[0], img_rows, img_cols, 1)
X_val = X_val.reshape(X_val.shape[0], img_rows, img_cols, 1)
X_test = X_test.reshape(X_test.shape[0], img_rows, img_cols, 1)

X_train = X_train.astype('float32')
X_val = X_val.astype('float32')
X_test = X_test.astype('float32')

X_train /= 255
X_val /= 255
X_test /= 255

print("Training matrix shape", X_train.shape)
print("Validation matrix shape", X_val.shape)
print("Testing matrix shape", X_test.shape)

Y_train = np_utils.to_categorical(y_train, mnist_classes)
Y_val = np_utils.to_categorical(y_val, mnist_classes)
Y_test = np_utils.to_categorical(y_test, mnist_classes)

# Define your model here
model = Sequential()

#Conv layer with 24 filters of size 5x5 and ReLU activation
model.add(Conv2D(24, kernel_size=(6, 6), activation='relu', input_shape=(28, 28, 1)))
model.add(Dropout(0.25))

model.add(Conv2D(48, kernel_size=(5, 5), activation='relu'))
model.add(Dropout(0.25))

model.add(Conv2D(64, kernel_size=(4, 4), activation='relu'))
model.add(Dropout(0.25))

#Max pooling of size 2x2
model.add(MaxPooling2D(pool_size=(2, 2)))

#Flatten operation
model.add(Flatten())

model.add(Dense(250))
model.add(Activation('relu'))
model.add(Dropout(0.25))

#FC layer with 10 neurons and softmax activation
model.add(Dense(mnist_classes, activation='softmax'))

sgd = optimizers.SGD(learning_rate=0.01) #lr = learning rate
model.compile(loss='categorical_crossentropy', optimizer=sgd, metrics=['accuracy'])

model.fit(X_train, Y_train,
          batch_size=128, epochs=8, verbose=1,
          validation_data=(X_val, Y_val))

score = model.evaluate(X_test, Y_test, verbose=1)
print('Test loss:', score[0])
print('Test accuracy (NOT NORMALIZED):', score[1])

import tensorflow as tf

converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model.
with open('model.tflite', 'wb') as f:
  f.write(tflite_model)

try:
    from google.colab import files
    files.download('mnist.tflite')
except:
    print("Skip downloading")