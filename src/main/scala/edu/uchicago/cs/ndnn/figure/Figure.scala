/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 */

package edu.uchicago.cs.ndnn.figure

import javafx.application.Application
import javafx.scene.chart.{LineChart, NumberAxis, XYChart}
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

import scala.collection.JavaConversions._

/**
  * Created by harper on 5/10/17.
  */

object FigureApp {
  var root: Group = null
  var width: Int = 0
  var height: Int = 0
  var title: String = ""
}


object LineFigure extends Application {

  protected var _title: String = ""

  protected var xaxis: NumberAxis = _

  protected var yaxis: NumberAxis = _

  protected var lineChart: LineChart[Number, Number] = _

  protected def getXAxis: NumberAxis = {
    if (xaxis == null)
      xaxis = new NumberAxis()
    xaxis
  }

  protected def getYAxis: NumberAxis = {
    if (yaxis == null)
      yaxis = new NumberAxis()
    yaxis
  }

  protected def getLineChart: LineChart[Number, Number] = {
    if (null == lineChart)
      lineChart = new LineChart(getXAxis, getYAxis)
    return lineChart
  }

  def title(title: String): LineFigure = {
    this._title = title
    this
  }

  def xLabel(label: String): LineFigure = {
    getXAxis.setLabel(label)
    this
  }

  def yLabel(label: String): LineFigure = {
    getYAxis.setLabel(label)
    this
  }

  def addPlot(data: Seq[(Number, Number)], legend: String): LineFigure = {

    val plotData = new XYChart.Series[Number, Number]()
    plotData.setName(legend)
    plotData.getData.addAll(data.map(point => new XYChart.Data[Number, Number](point._1, point._2)))

    getLineChart.getData.add(plotData)
    this
  }

  def show(width: Int = 600, height: Int = 400): Unit = {
    FigureApp.root = new Group(getLineChart)
    FigureApp.width = width
    FigureApp.height = height
    FigureApp.title = _title

  }


  override def start(stage: Stage): Unit = {
    //Creating a scene object
    val scene: Scene = new Scene(FigureApp.root, FigureApp.width, FigureApp.height)
    //Setting title to the Stage
    stage.setTitle(_title)
    //Adding scene to the stage
    stage.setScene(scene)
    stage.show()
  }

}
