/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *     
 *******************************************************************************/
package edu.uchicago.cs.encsel.app

import java.io.File

import edu.uchicago.cs.encsel.persist.Persistence

object Rescan extends App {

  if (args.length == 0) {
    System.out.println("This application rescans a file and replace entries that already exist")
    System.exit(0)
  }

  var target = new File(args(0)).toURI()

  var datas = Persistence.get.load()
  var filtered = datas.filter { !_.origin.equals(target) }.toList

  System.out.println("Found and remove %d entries".format(datas.size - filtered.size))
  Persistence.get.clean()
  Persistence.get.save(filtered)

  new DataCollector().collect(target)
}