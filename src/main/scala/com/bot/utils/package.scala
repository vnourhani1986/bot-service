package com.bot

package object utils {

  def splitColumns1[T](list: List[T]): List[List[T]] = {
    list.sliding(1, 1).toList
  }

  def splitColumns2[T](list: List[T]): List[List[T]] = {
    val list1 = list.sliding(1, 2).flatten.toList
    val list2 = list.drop(1).sliding(1, 2).flatten.toList
    list1.zip(list2).map(x => List(x._1, x._2))
  }
//  def splitFlexible[T](list: List[T]): List[List[T]] = {
//    val list1 = list.map(_.)
//  }
}
