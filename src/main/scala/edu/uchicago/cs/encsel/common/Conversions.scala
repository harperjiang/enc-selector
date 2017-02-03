package edu.uchicago.cs.encsel.common

import java.util.concurrent.Callable

object Conversions {
  implicit def funToRunnable(fun: () => Unit): Runnable = new Runnable() { def run() = fun() }
  implicit def funToCallable[T](fun: () => T): Callable[T] = new Callable[T]() { def call(): T = fun() }
}